package de.bibgl.konto.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import de.bibgl.konto.MainActivity
import de.bibgl.konto.R
import de.bibgl.konto.data.Account
import de.bibgl.konto.data.Loan
import de.bibgl.konto.data.Profile
import java.time.LocalDate
import kotlin.math.absoluteValue

object Notifications {

    // Kanal-IDs sind in den Android-Einstellungen des Nutzers gespeichert - nicht umbenennen.
    private const val CHANNEL_DUE = "due_dates"
    private const val CHANNEL_CARD = "card_expiry"

    // Jedes Profil bekommt eigene Meldungen, damit sie sich nicht gegenseitig
    // ueberschreiben. Die ID leitet sich stabil aus der Profil-ID ab.
    private const val BASE_DUE = 1_000_000
    private const val BASE_CARD = 2_000_000

    private fun notificationId(base: Int, profileId: String): Int =
        base + (profileId.hashCode().absoluteValue % 100_000)

    /**
     * Fristen und Ausweisablauf in getrennten Kanaelen, damit sich beides in den
     * Android-Einstellungen einzeln abschalten laesst. Bis 1.11.0 lief die
     * Ausweis-Warnung mit im Kanal der Rueckgabe-Erinnerungen.
     */
    fun createChannel(context: Context) {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_DUE,
                "Rückgabe-Erinnerungen",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Erinnert, bevor Medien der Stadtbücherei fällig werden."
            },
            NotificationChannel(
                CHANNEL_CARD,
                "Ausweis-Ablauf",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Warnt, bevor ein Bibliotheksausweis abläuft."
            },
        )
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannels(channels)
    }

    private fun canNotify(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    private fun openAppIntent(context: Context, profileId: String): PendingIntent =
        PendingIntent.getActivity(
            context,
            profileId.hashCode(),
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // Antippen oeffnet die App direkt beim betroffenen Ausweis.
                .putExtra(MainActivity.EXTRA_PROFILE_ID, profileId),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    /**
     * Meldet faellige und ueberfaellige Medien eines Ausweises. Liefert true,
     * wenn etwas gemeldet wurde.
     *
     * @param showProfile blendet den Kontonamen ein; bei nur einem hinterlegten
     *   Ausweis waere er ueberfluessiges Rauschen.
     */
    fun notifyDueSoon(
        context: Context,
        profile: Profile,
        account: Account,
        reminderDays: Int,
        showProfile: Boolean,
    ): Boolean {
        val today = LocalDate.now()
        val relevant = account.loans
            .filter { (it.daysLeft(today) ?: Long.MAX_VALUE) <= reminderDays }
            .sortedBy { it.daysLeft(today) ?: Long.MAX_VALUE }
        if (relevant.isEmpty() || !canNotify(context)) return false

        // Bewusst knapp: oben wie dringend (nach dem dringendsten Medium), darunter
        // bei einem Medium dessen Titel, bei mehreren die Anzahl. Aufgeklappt
        // stehen dann alle Titel da, je mit Frist vorn, weil Android jede Zeile
        // hinten abschneidet.
        val core = "Rückgabe ${dueLabel(relevant.first(), today)}"
        val title = if (showProfile) "${profile.label}: $core" else core

        val overdue = relevant.count { it.isOverdue(today) }
        val dueSoon = relevant.size - overdue
        val count = if (overdue > 0 && dueSoon > 0) {
            "$overdue überfällig, $dueSoon bald fällig"
        } else {
            "${relevant.size} ${mediaWord(relevant.size)}"
        }
        // Bei einem Medium gleich der Titel - "1 Medium" sagt nichts.
        val text = if (relevant.size == 1) relevant.first().title else count

        val style: NotificationCompat.Style = if (relevant.size == 1) {
            NotificationCompat.BigTextStyle().bigText(relevant.first().title)
        } else {
            NotificationCompat.InboxStyle().also { inbox ->
                relevant.take(6).forEach { inbox.addLine("${lineLabel(it, today)}: ${it.title}") }
                if (relevant.size > 6) inbox.addLine("+ ${relevant.size - 6} weitere")
            }
        }

        fun builder(text: String) = NotificationCompat.Builder(context, CHANNEL_DUE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setContentIntent(openAppIntent(context, profile.id))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            // Ersetzt eine neue Meldung die vom Vortag, soll es nicht erneut piepen.
            .setOnlyAlertOnce(true)

        // Buchtitel sind persoenlich: Hat der Nutzer "vertrauliche Inhalte" auf dem
        // Sperrbildschirm ausgeblendet, zeigt Android dort die Fassung ohne Titel.
        val notification = builder(text)
            .setStyle(style)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(builder(count).build())
            .build()

        NotificationManagerCompat.from(context)
            .notify(notificationId(BASE_DUE, profile.id), notification)
        return true
    }

    /**
     * Warnt, dass ein Bibliotheksausweis bald oder heute ablaeuft. An welchen
     * Tagen das geschieht, entscheidet der Worker. Liefert true, wenn gewarnt wurde.
     *
     * Der Vorname steht immer dabei, damit klar ist, wessen Ausweis es ist -
     * auch wenn mehrere Ausweise (Familie) hinterlegt sind.
     */
    fun notifyCardExpiry(
        context: Context,
        profile: Profile,
        account: Account,
    ): Boolean {
        val days = account.cardDaysLeft() ?: return false
        if (days < 0 || !canNotify(context)) return false
        // "Erikas Ausweis läuft ab", die Frist darunter: Ein ganzer Satz im Titel
        // wuerde abgeschnitten, weil Android die Uhrzeit mit in die Zeile setzt.
        // Das genaue Datum steht in der Ausweis-Karte der App.
        val who = firstName(account) ?: profile.label
        val notification = NotificationCompat.Builder(context, CHANNEL_CARD)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${genitive(who)} Ausweis läuft ab")
            .setContentText(if (days == 0L) "heute" else "in $days ${dayWord(days)}")
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setContentIntent(openAppIntent(context, profile.id))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOnlyAlertOnce(true)
            .build()
        NotificationManagerCompat.from(context)
            .notify(notificationId(BASE_CARD, profile.id), notification)
        return true
    }

    /** Frist fuer den Titel: "Rückgabe heute", "Rückgabe in 3 Tagen", "Rückgabe überfällig". */
    private fun dueLabel(loan: Loan, today: LocalDate): String {
        val d = loan.daysLeft(today) ?: return "ohne Frist"
        return when {
            d < 0L -> "überfällig"
            d == 0L -> "heute"
            d == 1L -> "morgen"
            else -> "in $d Tagen"
        }
    }

    /** Kurze Frist fuer den Anfang einer Zeile, z.B. "heute" oder "2 Tage überfällig". */
    private fun lineLabel(loan: Loan, today: LocalDate): String {
        val d = loan.daysLeft(today) ?: return "ohne Frist"
        return when {
            d == 0L -> "heute"
            d == 1L -> "morgen"
            d > 1L -> "in $d Tagen"
            d == -1L -> "1 Tag überfällig"
            else -> "${-d} Tage überfällig"
        }
    }

    /**
     * Vorname aus dem Namen der Bibliothek ("Mustermann, Erika" -> "Erika").
     * Null, wenn der Name fehlt oder anders aufgebaut ist - dann gilt der
     * Kontoname aus der App.
     */
    private fun firstName(account: Account): String? =
        account.patronName.substringAfter(",", "").trim().ifEmpty { null }

    /** "Erika" -> "Erikas", "Jonas" -> "Jonas’" (Apostroph nach s, ß, x, z). */
    private fun genitive(name: String): String =
        if (name.last().lowercaseChar() in "sßxz") "$name’" else "${name}s"

    private fun dayWord(n: Long) = if (n == 1L) "Tag" else "Tagen"

    private fun mediaWord(n: Int) = if (n == 1) "Medium" else "Medien"
}
