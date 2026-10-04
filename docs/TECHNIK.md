# Technische Details

Hintergrund zur [Stadtbücherei-GL-App](../README.md): wie man sie selbst baut und wie
das Auslesen der Bibliotheksseite funktioniert.

---

## Selbst bauen

Es wird kein Android Studio gebraucht — GitHub baut den APK:

```bash
git clone https://github.com/daGrue11/Stadtbuecherei-GL-App.git
cd Stadtbuecherei-GL-App
```

Im eigenen Fork unter **Actions** den Workflow *Android APK bauen* starten und
den APK anschließend unter *Artifacts* herunterladen. Ohne hinterlegten
Signaturschlüssel wird mit einem Debug-Schlüssel signiert — der APK läuft, kann
aber keine offizielle Version aus den Releases überschreiben.

Mit Android Studio: Projektordner öffnen, Gradle synchronisieren, `Run`.

## Wie es funktioniert

Die Stadtbücherei nutzt **OCLC OPEN** auf DotNetNuke (ASP.NET WebForms). Eine
offizielle Schnittstelle gibt es nicht, die App liest die Website aus. Das
Wesentliche steckt in [`LibraryClient.kt`](https://github.com/daGrue11/Stadtbuecherei-GL-App/blob/main/app/src/main/java/de/bibgl/konto/data/LibraryClient.kt):

- **Login** ist ein WebForms-Postback auf `/Login` mit `__VIEWSTATE` und
  `__EVENTVALIDATION` aus dem Formular.
- **Das ganze Konto** steht danach in einer einzigen Seite `/Mein-Konto` — alle
  Reiter sind serverseitig gerendert, es braucht keinen Klick pro Reiter.
- **Verlängerbarkeit** steht *nicht* im HTML, sondern wird per AJAX von
  `PatronAccountService.asmx/IsCatalogueCopyExtendable` nachgeladen. Die App ruft
  denselben JSON-Endpunkt auf und bekommt pro Medium den Status, eine Begründung
  und das neue Fristdatum.
- **Verlängern ist zweistufig.** Der Klick auf „Verlängern" verlängert noch
  nichts, sondern öffnet den Dialog „Verlängerung bestätigen", der anfallende
  Verlängerungs- und Säumnisgebühren nennt. Erst dessen Button führt sie aus.
  Fallen Gebühren an, fragt die App vorher nach, statt sie stillschweigend zu
  akzeptieren.
- **Element-IDs** werden über ihre Endung angesprochen (`[id$=lblFeeTotalData]`),
  weil die Präfixe eine wechselnde Modulnummer enthalten — und immer auf das
  jeweilige Panel eingegrenzt, weil die Seite ausgeblendete Dialoge mit
  denselben ID-Endungen enthält.
- **Erfolg einer Verlängerung** wird daran gemessen, ob sich die Frist
  tatsächlich verschoben hat, nicht an den Meldungstexten der Seite.
- **Merkliste** zeigt die Seite in Seiten zu je 10 Einträgen. Die App lädt das
  Konto mit `pagesize=20` (eine der Größen, die die Seite selbst anbietet) und
  liest die Gesamtzahl aus dem Reiter; stehen mehr Titel auf der Merkliste,
  weist sie darauf hin. Entfernen ist ein Postback des Links „von der Merkliste
  entfernen", danach wird neu geladen und geprüft, ob der Titel wirklich weg ist.
- **Mehrere Konten** bekommen je einen eigenen HTTP-Client mit eigenem
  Cookie-Jar; die Seite kennt nur eine Anmeldung pro Sitzung.
- **Sitzungen** wirft die Seite nach kurzer Zeit weg. Schlägt ein Abruf oder
  eine Aktion fehl, meldet sich die App genau einmal neu an und versucht es
  erneut, bevor sie einen Fehler zeigt.

Weil die App eine Website ausliest, kann ein Umbau durch die Bibliothek die
Anzeige stören. Die Selektoren sind bewusst so gewählt, dass sie Änderungen an
Layout und Modulnummern überstehen — aber eine Garantie ist das nicht.

## Benachrichtigungen

Ein WorkManager-Job ([`DueDateWorker.kt`](https://github.com/daGrue11/Stadtbuecherei-GL-App/blob/main/app/src/main/java/de/bibgl/konto/work/DueDateWorker.kt))
lädt einmal täglich — nur mit Netz — jedes hinterlegte Konto und prüft zwei
Dinge. Die Regeln selbst stehen in
[`Notifications.kt`](https://github.com/daGrue11/Stadtbuecherei-GL-App/blob/main/app/src/main/java/de/bibgl/konto/work/Notifications.kt):

- **Rückgabe-Erinnerung:** Medien, die innerhalb der eingestellten Tage
  (1–14, Standard 5) fällig oder schon überfällig sind. Höchstens eine Meldung
  pro Konto und Tag.
- **Ausweisablauf:** 30, 14 und 7 Tage vor Ablauf sowie am Ablauftag, jeweils
  nur einmal. War das Handy an einem dieser Tage aus oder ohne Netz, kommt die
  Meldung am nächsten Tag nach. Nach dem Ablauf kommt keine Meldung mehr. Der
  rote Hinweis in der App erscheint schon ab 60 Tagen.

**Uhrzeit:** Meldungen kommen frühestens um 9 Uhr und nie nachts. WorkManager
garantiert keine feste Uhrzeit — ein Tagesrhythmus verschiebt sich, und ohne Netz
würde er den Lauf auch nachts nachholen. Deshalb läuft der Job stündlich, prüft
aber nur zwischen 9 und 20 Uhr und jedes Konto höchstens einmal pro Tag; die
übrigen Läufe enden sofort ohne Netzzugriff. Normalerweise kommt die Meldung also
kurz nach 9 Uhr; ist dann kein Netz da, beim nächsten stündlichen Versuch bis
20 Uhr, sonst am nächsten Morgen.

Jedes Konto hat eigene Meldungen, die sich nicht gegenseitig überschreiben; bei
mehreren Konten steht der Kontoname im Titel. Antippen öffnet die App direkt beim
betroffenen Konto. Scheitert das Laden eines Kontos, versucht es der nächste
stündliche Lauf erneut — die übrigen Konten werden trotzdem geprüft. Abschalten
lässt sich alles in den Einstellungen der App oder im System.

## Technik

Kotlin · Jetpack Compose (Material 3) · OkHttp · Jsoup · WorkManager ·
EncryptedSharedPreferences · minSdk 26 · Gebaut mit GitHub Actions

## Signierung und Releases

Android installiert eine neue Version nur über eine vorhandene, wenn beide mit
demselben Schlüssel signiert sind. Ohne feste Konfiguration erzeugt Gradle bei
jedem CI-Lauf einen neuen Zufallsschlüssel — die GitHub-Runner werden jedes Mal
frisch aufgesetzt. Updates schlagen dann mit „App nicht installiert" fehl.

Deshalb liegt ein fester Schlüssel in den Repository-Secrets:

| Secret | Inhalt |
|---|---|
| `KEYSTORE_BASE64` | der PKCS12-Keystore, base64-kodiert |
| `KEYSTORE_PASSWORD` | Passwort für Store und Schlüssel (Alias `bibgl`) |

Der Workflow schreibt daraus `app/keystore.jks`, bevor Gradle läuft. Die Datei
ist in `.gitignore` ausgeschlossen und darf nicht ins Repository.

Fehlt das Secret — etwa in einem Fork — wird mit dem Debug-Schlüssel signiert.
Der APK läuft, kann aber keine offizielle Version aus den Releases überschreiben.

### Eine Version veröffentlichen

Versionsnummern folgen [Semantic Versioning](https://semver.org/lang/de/) mit
immer drei Stellen (`MAJOR.MINOR.PATCH`): Fehlerbehebung → `1.8.1`, neue
Funktion → `1.9.0`, inkompatible Änderung → `2.0.0`. Was sich geändert hat,
steht für Nutzer in [`CHANGELOG.md`](../CHANGELOG.md); Änderungen sammeln sich
dort unter `[Unreleased]`.

1. `versionCode` und `versionName` in [`app/build.gradle.kts`](../app/build.gradle.kts)
   erhöhen. `versionCode` muss streng steigen, sonst verweigert Android das Update.
2. In `CHANGELOG.md` `## [Unreleased]` in `## [1.8.1] - JJJJ-MM-TT` umbenennen,
   darüber ein neues, leeres `## [Unreleased]` anlegen und unten die
   Vergleichslinks ergänzen.
3. Committen (`Release 1.8.1`) und pushen.
4. Annotierten Tag setzen und pushen:

```bash
git tag -a v1.8.1 -m "Stadtbücherei-GL-App 1.8.1"
git push origin v1.8.1
```

Der Tag-Push baut den APK und legt den GitHub-Release samt Datei an. Die
Release-Notes kommen aus dem passenden Abschnitt in `CHANGELOG.md`. Vorab-Checks
brechen ab, wenn der Tag nicht zum `versionName` im Build passt, keine drei
Stellen hat oder der Changelog-Abschnitt fehlt — sonst driften Dateiname,
App-Version und Beschreibung auseinander.

Bereits veröffentlichte Versionen werden nie nachträglich geändert; ein Fehler
in 1.8.1 wird mit 1.8.2 behoben. Die Tags `v1.3` bis `v1.8` stammen aus der Zeit
vor dieser Regel und haben nur zwei Stellen.

> Nur Release-Dateien sind ohne GitHub-Login herunterladbar. Die Artefakte eines
> normalen Builds sind es nicht — deshalb der Umweg über Tags.
