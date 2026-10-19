package de.bibgl.konto.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Oeffnungszeiten der Stadtbuechereien, fest hinterlegt.
 *
 * Quelle: [SOURCE_URL] (Stand [AS_OF]). Die Seite ist reines HTML ohne
 * Schnittstelle und aendert sich selten - deshalb kein Abruf zur Laufzeit.
 * Beim Aktualisieren der Zeiten auch [AS_OF] anpassen - die App zeigt es an.
 * Gesetzliche Feiertage in NRW gelten als geschlossen; Sonderschliessungen
 * (z.B. zwischen den Jahren) kennt die App nicht.
 */
object OpeningHours {

    const val SOURCE_URL = "https://www.bergischgladbach.de/oeffnungszeiten-standorte-kontakt.aspx"

    /** Wann die Zeiten zuletzt mit [SOURCE_URL] abgeglichen wurden. */
    const val AS_OF = "Oktober 2026"

    val ZONE: ZoneId = ZoneId.of("Europe/Berlin")

    data class Range(val from: LocalTime, val to: LocalTime)

    data class Branch(
        /** Stabile Kennung - wird als Einstellung gespeichert, nicht aendern. */
        val id: String,
        val name: String,
        /** Kurzname fuer Aufzaehlungen, z.B. "Forum". */
        val shortName: String,
        val address: String,
        val hours: Map<DayOfWeek, List<Range>>,
    ) {
        fun rangesOn(date: LocalDate): List<Range> =
            if (isHoliday(date)) emptyList() else hours[date.dayOfWeek].orEmpty()
    }

    private fun r(from: String, to: String) = Range(LocalTime.parse(from), LocalTime.parse(to))

    val branches: List<Branch> = listOf(
        Branch(
            id = "forum",
            name = "Stadtbücherei im Forum",
            shortName = "Forum",
            address = "Hauptstr. 250",
            hours = mapOf(
                DayOfWeek.MONDAY to listOf(r("12:00", "19:00")),
                DayOfWeek.TUESDAY to listOf(r("12:00", "19:00")),
                DayOfWeek.WEDNESDAY to listOf(r("09:00", "13:00")),
                DayOfWeek.THURSDAY to listOf(r("12:00", "19:00")),
                DayOfWeek.FRIDAY to listOf(r("12:00", "19:00")),
                DayOfWeek.SATURDAY to listOf(r("10:00", "13:00")),
            ),
        ),
        Branch(
            id = "bensberg",
            name = "Stadtteilbücherei Bensberg",
            shortName = "Bensberg",
            address = "Schlossstr. 46-48",
            hours = mapOf(
                DayOfWeek.TUESDAY to listOf(r("13:00", "18:00")),
                DayOfWeek.WEDNESDAY to listOf(r("10:00", "13:00")),
                DayOfWeek.THURSDAY to listOf(r("13:00", "18:00")),
                DayOfWeek.FRIDAY to listOf(r("13:00", "18:00")),
                DayOfWeek.SATURDAY to listOf(r("10:00", "13:00")),
            ),
        ),
        Branch(
            id = "paffrath",
            name = "Stadtteilbücherei Paffrath",
            shortName = "Paffrath",
            address = "Borngasse 86 (in der IGP)",
            hours = mapOf(
                DayOfWeek.MONDAY to listOf(r("10:00", "11:30"), r("15:00", "17:00")),
                DayOfWeek.TUESDAY to listOf(r("10:00", "11:30"), r("15:00", "17:00")),
                DayOfWeek.WEDNESDAY to listOf(r("10:00", "11:30")),
                DayOfWeek.THURSDAY to listOf(r("10:00", "11:30"), r("15:00", "17:00")),
                DayOfWeek.FRIDAY to listOf(r("10:00", "11:30")),
            ),
        ),
    )

    /** Hauptstelle - Standard fuer den Status in der Uebersicht. */
    val main: Branch get() = branches.first()

    /** Standort zur gespeicherten Kennung; unbekannt oder leer = Hauptstelle. */
    fun branch(id: String?): Branch = branches.firstOrNull { it.id == id } ?: main

    sealed interface Status {
        /** Geoeffnet bis [until]; [closesSoon] ab 30 Minuten vor Schluss. */
        data class Open(val until: LocalTime, val closesSoon: Boolean) : Status

        /** Geschlossen; [next] ist die naechste Oeffnung, null wenn keine in Sicht. */
        data class Closed(val next: LocalDateTime?, val holiday: Boolean) : Status
    }

    fun status(branch: Branch, now: LocalDateTime): Status {
        val today = now.toLocalDate()
        val time = now.toLocalTime()
        branch.rangesOn(today).firstOrNull { time >= it.from && time < it.to }?.let {
            return Status.Open(it.to, closesSoon = time.plusMinutes(30) >= it.to)
        }
        // Naechste Oeffnung suchen - spaeter heute oder in den kommenden Tagen.
        val next = (0L..14L).asSequence()
            .map { today.plusDays(it) }
            .flatMap { day -> branch.rangesOn(day).map { day.atTime(it.from) } }
            .firstOrNull { it > now }
        return Status.Closed(next, holiday = isHoliday(today))
    }

    /*
     * Der Status steht in zwei Teilen: ein kurzes Etikett ("Geschlossen") und
     * der Zeitpunkt ("Öffnet heute 12:00"). Als ein Satz war er zu lang - auf
     * schmalen Geraeten brach "Uhr" allein in die naechste Zeile. Die UI setzt
     * beide Teile nebeneinander und bricht nur zwischen ihnen um.
     */

    /** "Geöffnet", "Schließt bald", "Geschlossen" oder "Feiertag". */
    fun statusLabel(status: Status): String = when (status) {
        is Status.Open -> if (status.closesSoon) "Schließt bald" else "Geöffnet"
        is Status.Closed -> if (status.holiday) "Feiertag" else "Geschlossen"
    }

    /** "bis 19:00" / "Öffnet morgen 12:00"; null, wenn keine Oeffnung in Sicht. */
    fun statusDetail(status: Status, now: LocalDateTime): String? = when (status) {
        is Status.Open -> "bis ${fmt(status.until)}"
        is Status.Closed -> status.next?.let {
            "Öffnet ${dayPhrase(it.toLocalDate(), now.toLocalDate())} ${fmt(it.toLocalTime())}"
        }
    }

    private fun dayPhrase(day: LocalDate, today: LocalDate): String = when (day) {
        today -> "heute"
        today.plusDays(1) -> "morgen"
        else -> if (day < today.plusDays(7)) dayName(day.dayOfWeek)
        else "am ${day.dayOfMonth}.${day.monthValue}."
    }

    fun fmt(t: LocalTime): String = t.format(TIME)

    private val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /** Je Zeitfenster eine Zeile ("10:00–11:30", "15:00–17:00"); ohne Fenster "geschlossen". */
    fun rangeLines(ranges: List<Range>): List<String> =
        if (ranges.isEmpty()) listOf("geschlossen")
        else ranges.map { "${fmt(it.from)}–${fmt(it.to)}" }

    fun dayName(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Montag"
        DayOfWeek.TUESDAY -> "Dienstag"
        DayOfWeek.WEDNESDAY -> "Mittwoch"
        DayOfWeek.THURSDAY -> "Donnerstag"
        DayOfWeek.FRIDAY -> "Freitag"
        DayOfWeek.SATURDAY -> "Samstag"
        DayOfWeek.SUNDAY -> "Sonntag"
    }

    // ------------------------------------------------------------- Feiertage

    /** Gesetzliche Feiertage in Nordrhein-Westfalen. */
    fun isHoliday(date: LocalDate): Boolean {
        val fixed = (date.monthValue to date.dayOfMonth) in setOf(
            1 to 1, // Neujahr
            5 to 1, // Tag der Arbeit
            10 to 3, // Tag der Deutschen Einheit
            11 to 1, // Allerheiligen
            12 to 25, 12 to 26, // Weihnachten
        )
        if (fixed) return true
        val easter = easterSunday(date.year)
        return date in setOf(
            easter.minusDays(2), // Karfreitag
            easter.plusDays(1), // Ostermontag
            easter.plusDays(39), // Christi Himmelfahrt
            easter.plusDays(50), // Pfingstmontag
            easter.plusDays(60), // Fronleichnam
        )
    }

    /** Ostersonntag nach der Gaussschen Osterformel (gregorianisch). */
    internal fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }
}
