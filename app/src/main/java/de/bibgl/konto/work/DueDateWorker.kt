package de.bibgl.konto.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.bibgl.konto.data.AccountRepository
import de.bibgl.konto.data.Store
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Prueft im Hintergrund Rueckgabefristen und Ausweisablauf und erinnert
 * rechtzeitig. Laeuft nur, wenn Zugangsdaten hinterlegt sind und der Nutzer
 * Benachrichtigungen nicht abgeschaltet hat.
 *
 * WorkManager garantiert keine Uhrzeit: Ein Tagesrhythmus verschiebt sich durch
 * Doze und Zeitumstellung, und ohne Netz holt er den Lauf nach, sobald wieder Netz
 * da ist - auch nachts. Deshalb laeuft der Job stuendlich, meldet aber nur
 * zwischen 9 und 20 Uhr und prueft jedes Konto hoechstens einmal pro Tag. Die
 * Laeufe ausserhalb des Fensters enden sofort, ohne Netzzugriff.
 */
class DueDateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val store = Store(applicationContext)
        if (!store.notificationsEnabled) return Result.success()

        val now = LocalTime.now()
        if (now.isBefore(WINDOW_START) || !now.isBefore(WINDOW_END)) return Result.success()

        val profiles = store.profiles
        if (profiles.isEmpty()) return Result.success()

        val repo = AccountRepository(store)
        val today = LocalDate.now().toEpochDay()
        // Bei mehreren Ausweisen gehoert der Kontoname in die Meldung.
        val showProfile = profiles.size > 1

        profiles.forEach { profile ->
            if (store.lastCheckedDay(profile.id) == today) return@forEach

            val account = try {
                repo.load(profile.id)
            } catch (e: Exception) {
                // Netz weg oder Seite gerade kaputt: Der naechste stuendliche Lauf
                // versucht es erneut. Die anderen Ausweise werden trotzdem geprueft.
                return@forEach
            }

            Notifications.notifyDueSoon(
                applicationContext, profile, account, store.reminderDays, showProfile,
            )

            // Ausweisablauf nur an den Stichtagen melden, jeden hoechstens einmal.
            // Wurde ein Stichtag verpasst (Handy aus, kein Netz), kommt die Meldung
            // am naechsten Tag nach - aber nie nach dem Ablauf.
            val until = account.cardValidUntilRaw
            val stage = account.cardDaysLeft()?.let { cardReminderStage(it) }
            if (stage != null && store.cardReminderStage(profile.id, until) != stage) {
                val notified = Notifications.notifyCardExpiry(
                    applicationContext, profile, account, showProfile,
                )
                if (notified) store.setCardReminderStage(profile.id, until, stage)
            }

            store.setLastCheckedDay(profile.id, today)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "due_date_check"

        /** Tage vor Ablauf des Ausweises, an denen gewarnt wird (0 = am Ablauftag). */
        private val CARD_REMINDER_DAYS = listOf(0, 7, 14, 30)

        /** Fruehestens um 9 Uhr melden, spaetestens bis 20 Uhr nachholen. */
        private val WINDOW_START = LocalTime.of(9, 0)
        private val WINDOW_END = LocalTime.of(20, 0)

        /**
         * Stichtag, zu dem bei [daysLeft] Resttagen gewarnt wird: der naechste
         * noch nicht unterschrittene aus [CARD_REMINDER_DAYS], z.B. 14 bei 10
         * Tagen. Null, wenn der Ausweis noch zu lange gilt oder schon abgelaufen ist.
         */
        private fun cardReminderStage(daysLeft: Long): Int? =
            if (daysLeft < 0) null else CARD_REMINDER_DAYS.firstOrNull { it >= daysLeft }

        /** Plant den stuendlichen Check, ausgerichtet auf die volle Stunde. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DueDateWorker>(1, TimeUnit.HOURS)
                .setInitialDelay(delayToNextFullHour(), TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun delayToNextFullHour(): Long {
            val now = LocalDateTime.now()
            val target = now.truncatedTo(ChronoUnit.HOURS).plusHours(1)
            return Duration.between(now, target).toMinutes().coerceAtLeast(1)
        }
    }
}
