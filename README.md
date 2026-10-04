# Stadtbücherei-GL-App

**Inoffizielle Android-App für die Stadtbücherei Bergisch Gladbach**

Zeigt welche Medien ausgeliehen wurden, wie lange es noch bis zur Rückgabe
dauert, welche sich verlängern lassen und benachrichtigt rechtzeitig, bevor eine
Frist abläuft.

> **Privates Projekt.** Nicht von der Stadtbücherei Bergisch Gladbach
> herausgegeben. Nutzung und Installation auf eigene Verantwortung.
---

## Screenshots

| Ausgeliehene Medien | Konto hinzufügen | Einstellungen |
|:---:|:---:|:---:|
| <img src="docs/screenshots/01-ausleihen.jpg" width="240" alt="Liste ausgeliehener Medien mit Countdown bis zur Rückgabe"> | <img src="docs/screenshots/02-konto-hinzufuegen.jpg" width="240" alt="Anmeldung mit Ausweisnummer und Passwort"> | <img src="docs/screenshots/03-einstellungen.jpg" width="240" alt="Einstellungen für Erinnerungen"> |


## Installation

### Empfohlen: mit Obtainium

[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="54">](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/klggr81/Stadtbuecherei-GL-App)

[Obtainium](https://github.com/ImranR98/Obtainium) installiert Apps direkt aus
GitHub-Releases und meldet neue Versionen automatisch.

1. Obtainium installieren (APK aus dessen
   [Releases](https://github.com/ImranR98/Obtainium/releases/latest))
2. Auf dem Handy den Badge oben antippen, alternativ in Obtainium *App
   hinzufügen* → `https://github.com/klggr81/Stadtbuecherei-GL-App`
3. *Installieren* antippen — Updates kommen künftig über Obtainium

### Manuell

1. **[APK herunterladen](https://github.com/klggr81/Stadtbuecherei-GL-App/releases/latest)** — unter *Assets* die Datei
   `Stadtbuecherei-GL-App-*.apk` antippen
2. Die Datei auf dem Android-Gerät öffnen
3. Android fragt, ob Apps aus dieser Quelle installiert werden dürfen → erlauben
4. Installieren, App öffnen, Ausweisnummer und Passwort der Stadtbücherei eingeben

Für spätere Versionen einfach den neuen APK installieren. Es wird als Update installiert. Die gespeicherten Konten bleiben erhalten.

> **Android wird abgeschottet.** Google verlangt künftig, dass sich die Entwickler
> *jeder* App bei Google registrieren — auch von Apps, die wie diese außerhalb
> des Play Stores installiert werden. Seit dem 30.09.2026 gilt das in ersten
> Ländern, weltweit soll es bis 2027 folgen. Kleine, freie Projekte wie dieses
> lassen sich dann nur noch umständlich oder gar nicht mehr installieren. Mehr
> dazu und wie man sich dagegen einsetzen kann: **[keepandroidopen.org](https://keepandroidopen.org/)**

### Voraussetzungen

- Android 8.0 oder neuer
- Ein gültiger Bibliotheksausweis der Stadtbücherei Bergisch Gladbach

**Getestet wurde ausschließlich unter Android 17 auf einem Pixel 10.** Ältere
Versionen sollten funktionieren, sind aber nicht ausprobiert.

## Was die App kann

| Funktion | Details |
|---|---|
| **Ausgeliehene Medien** | Titel, Verfasser, Medienart (Buch, Bilderbuch, Tonie, Spiel …), Zweigstelle, Cover. Antippen öffnet die Detailseite im Browser |
| **Countdown** | Farbige Ampel bis zur Rückgabe: rot überfällig, orange ≤ 2 Tage, gelb ≤ 7 Tage, grün ab 8 Tagen |
| **Verlängern** | Einzeln oder alle verlängerbaren Medien auf einmal. Nicht verlängerbare Medien zeigen den Grund („Die maximale Anzahl der Verlängerungen ist erreicht.") |
| **Gebühren** | Offene Gebühren, Einzahlungen, Saldo, Einzelposten |
| **Vormerkungen** | Vorbestellte Medien und was gerade abholbereit ist |
| **Merkliste** | Gemerkte Titel (bis zu 20, bei mehr ein Hinweis mit Link zur Website), Antippen öffnet die Detailseite, Entfernen per Lesezeichen-Symbol |
| **Ausweis** | Hinweis in der App ab 60 Tagen vor Ablauf, Benachrichtigung 30, 14 und 7 Tage vorher sowie am Ablauftag |
| **Erinnerungen** | Täglicher Hintergrund-Check, Benachrichtigung einstellbar: 1–14 Tage vor Fristende. Alle Meldungen kommen frühestens um 9 Uhr, nie nachts |
| **Mehrere Ausweise** | Konto-Umschalter in der Titelleiste, jedes Konto mit eigener Vorschau und eigenen Erinnerungen |

Ohne Netz zeigt die App den zuletzt geladenen Stand, damit die Fristen auch
unterwegs sichtbar bleiben.

## Datenschutz

Die App hat keinen Server und keine Analyse-Funktionen.

- Zugangsdaten werden **verschlüsselt auf dem Gerät** gespeichert
  (`EncryptedSharedPreferences`, Schlüssel im Android-Keystore)
- Sie werden **ausschließlich an `stadtbuecherei-gl.de`** gesendet — an
  niemanden sonst
- Es werden keinerlei Daten an den Autor oder Dritte übertragen
- Deinstallieren löscht alles

Die einzigen weiteren Netzwerkzugriffe sind die Cover-Bilder, die die
Bibliotheksseite selbst einbindet (u.a. von Amazon-Bildservern).

## Technische Details

Wie die App die Bibliotheksseite ausliest, wie man sie selbst baut und warum
manche Dinge so gelöst sind, steht in **[docs/TECHNIK.md](docs/TECHNIK.md)**.

## Lizenz

[PolyForm Noncommercial 1.0.0](LICENSE) — Nutzung, Änderung und Weitergabe
sind für nicht-kommerzielle Zwecke erlaubt, solange der Lizenztext bzw. dessen
URL und der Copyright-Hinweis erhalten bleiben. Kommerzielle Nutzung ist nicht
gestattet. Ohne Gewährleistung.
