# Datenschutzerklärung der Stadtbücherei-GL-App

Stand: 30. Oktober 2026

Die Stadtbücherei-GL-App ist eine inoffizielle App für das Bibliothekskonto der
Stadtbücherei Bergisch Gladbach. Sie wird nicht von der Stadtbücherei oder der
Stadt Bergisch Gladbach herausgegeben.

## Verantwortlich

Daniel Grünfeld
51469 Bergisch Gladbach
E-Mail: info@klggr.de
Impressum: [kollege-gruenfeld.de/impressum.php](https://kollege-gruenfeld.de/impressum.php)

## Kurz gesagt

- Die App hat **keinen eigenen Server**. Deine Daten gehen nicht an mich als
  Entwickler und nicht an Dritte, sondern nur an die Stadtbücherei, wenn die
  App dein Konto abruft.
- Die App enthält **keine Werbung, keine Analyse- oder Tracking-Dienste** und
  keine Absturzberichte an den Entwickler.
- Alle Verbindungen sind **verschlüsselt (HTTPS)**.
- Die einzige weitere Verbindung sind die **Cover-Bilder**, die die Stadtbücherei
  bei Amazon einbindet (siehe unten).

## Welche Daten die App auf deinem Gerät speichert

| Daten | Wozu | Wie gespeichert |
|---|---|---|
| Ausweisnummer und Passwort | Anmeldung bei der Stadtbücherei | verschlüsselt (AES-256, Schlüssel im Android-Keystore) |
| Kontodaten: Name, Ausweisnummer, Gültigkeit des Ausweises, E-Mail-Adresse, Ausleihen mit Fristen, Vormerkungen, Abholbereites, Merkliste, Gebühren | Anzeige in der App, auch ohne Netz; Erinnerungen an Fristen | im geschützten Speicherbereich der App |
| Einstellungen (z.B. Darstellung, Erinnerungen, selbst gewählte Kontonamen) | deine Einstellungen merken | im geschützten Speicherbereich der App |
| Cover-Bilder | schnellere Anzeige | Zwischenspeicher der App |

Auf den Speicherbereich der App können andere Apps nicht zugreifen. Die App
nimmt an der Android-Datensicherung nicht teil, die Daten landen also auch nicht
in einer Cloud-Sicherung.

## Übermittlung an die Stadtbücherei

Die App ruft dein Bibliothekskonto auf der Website der Stadtbücherei
(`www.stadtbuecherei-gl.de`) ab – so, als würdest du dich dort im Browser
anmelden. Das passiert:

- wenn du dich anmeldest oder die Ansicht aktualisierst,
- wenn du Medien verlängerst oder von der Merkliste entfernst,
- im Hintergrund höchstens einmal am Tag je Konto, um dich an Fristen zu
  erinnern – nur, wenn du die Erinnerungen nicht abgeschaltet hast.

Dabei werden deine Ausweisnummer und dein Passwort an die Stadtbücherei gesendet,
ausschließlich verschlüsselt und nur an diese Adresse. Für die Daten in deinem
Bibliothekskonto ist die Stadtbücherei verantwortlich; es gilt deren
Datenschutzerklärung auf [stadtbuecherei-gl.de](https://www.stadtbuecherei-gl.de).

Rechtsgrundlage ist Art. 6 Abs. 1 lit. b DSGVO: Die Übermittlung ist nötig,
damit die App die Funktionen bieten kann, für die du sie nutzt.

## Cover-Bilder (Amazon)

Die Stadtbücherei verlinkt Cover-Bilder, die über die ISBN von Servern von
Amazon geladen werden. Die App lädt diese Bilder direkt von dort. Amazon erhält
dabei technisch bedingt deine IP-Adresse, den Zeitpunkt und die Adresse des
Bildes. Aus der Adresse geht die ISBN hervor, also welcher Titel angezeigt wird.
Weitere Daten (Name, Ausweisnummer o.Ä.) gehen nicht an Amazon. Andere
Bildquellen fragt die App nicht an.

Rechtsgrundlage ist Art. 6 Abs. 1 lit. f DSGVO: berechtigtes Interesse an einer
übersichtlichen Anzeige mit Covern, wie sie auch die Website der Stadtbücherei
bietet. Informationen zum Datenschutz bei Amazon:
[amazon.de – Datenschutzerklärung](https://www.amazon.de/gp/help/customer/display.html?nodeId=201909010).

## Links

Tippst du auf einen Titel oder einen Link (z.B. zur Website der Stadtbücherei),
öffnet sich die Seite in deinem Browser. Dort gelten die Datenschutzbestimmungen
des jeweiligen Anbieters.

## Berechtigungen

- **Internet und Netzwerkstatus** – zum Abrufen deines Kontos.
- **Benachrichtigungen** – für Erinnerungen an Fristen und den Ablauf des
  Ausweises. Du kannst sie in der App oder in den Android-Einstellungen
  abschalten.

## Google Play

Wenn du die App über Google Play installierst, verarbeitet Google dabei Daten in
eigener Verantwortung, z.B. für Download und Updates. Hast du in Android dem
Teilen von Nutzungs- und Diagnosedaten zugestimmt, erhalte ich über Google Play
anonyme, zusammengefasste Statistiken (z.B. Installationszahlen, Abstürze). Sie
lassen keinen Rückschluss auf dich oder dein Bibliothekskonto zu. Mehr dazu:
[Datenschutzerklärung von Google](https://policies.google.com/privacy).

## Speicherdauer und Löschen

Die Daten bleiben auf deinem Gerät, bis du

- ein Konto in der App entfernst (Menü → „Dieses Konto entfernen“),
- alle Konten abmeldest (Menü → „Alle Konten abmelden“) oder
- die App deinstallierst.

Dann werden die Zugangsdaten und die gespeicherten Kontodaten gelöscht. Dein
Bibliothekskonto bei der Stadtbücherei bleibt davon unberührt.

## Deine Rechte

Du hast nach der DSGVO das Recht auf Auskunft, Berichtigung, Löschung,
Einschränkung der Verarbeitung, Datenübertragbarkeit und Widerspruch. Da ich als
Entwickler keine Daten von dir erhalte, kann ich dazu in der Regel nur
bestätigen, dass mir keine Daten vorliegen. Anfragen zu deinem
Bibliothekskonto richtest du an die Stadtbücherei.

Du kannst dich außerdem bei einer Datenschutz-Aufsichtsbehörde beschweren, etwa
bei der Landesbeauftragten für Datenschutz und Informationsfreiheit
Nordrhein-Westfalen ([ldi.nrw.de](https://www.ldi.nrw.de)).

## Änderungen

Ändert sich die App so, dass andere Daten verarbeitet werden, passe ich diese
Erklärung an. Die jeweils aktuelle Fassung steht im
[Repository der App](https://github.com/klggr81/Stadtbuecherei-GL-App/blob/main/docs/DATENSCHUTZ.md).
