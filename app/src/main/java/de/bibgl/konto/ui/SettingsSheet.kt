package de.bibgl.konto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.bibgl.konto.data.OpeningHours
import de.bibgl.konto.data.ThemeMode

/** Auswaehlbare Vorlaufzeiten in Tagen. */
private val REMINDER_OPTIONS = listOf(1, 2, 3, 5, 7, 14)

private val THEME_OPTIONS = listOf(
    ThemeMode.SYSTEM to "System",
    ThemeMode.LIGHT to "Hell",
    ThemeMode.DARK to "Dunkel",
)

private fun dayPhrase(days: Int) = if (days == 1) "1 Tag" else "$days Tage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    state: UiState,
    onDismiss: () -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onReminderDaysChanged: (Int) -> Unit,
    onOverviewBranchChanged: (String) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        ) {
            Text("Einstellungen", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Erinnerungen", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Täglicher Check aller Konten im Hintergrund, Meldung vor Ablauf der Frist",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.notificationsEnabled,
                    onCheckedChange = onNotificationsChanged,
                )
            }

            if (state.notificationsEnabled) {
                Spacer(Modifier.height(24.dp))
                Text("Erinnern ab", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tage vor Ablauf der Frist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                // Nur die Zahlen: "7 Tage" o.ä. sprengt auf schmalen Geräten die
                // Segmentbreite und bricht um. Die Einheit steht in der Überschrift.
                // Das Häkchen-Icon ist abgeschaltet (icon = {}), damit die Segmente
                // auch zu sechst schmal bleiben; die Auswahl zeigt die Füllfarbe.
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    REMINDER_OPTIONS.forEachIndexed { index, days ->
                        SegmentedButton(
                            selected = state.reminderDays == days,
                            onClick = { onReminderDaysChanged(days) },
                            shape = SegmentedButtonDefaults.itemShape(index, REMINDER_OPTIONS.size),
                            icon = {},
                            label = { Text(days.toString()) },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Meldung ${dayPhrase(state.reminderDays)} vor Fristende. " +
                        "Überfällige Medien werden immer gemeldet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Bücherei in der Übersicht", style = MaterialTheme.typography.titleMedium)
            Text(
                "Oben in der Übersicht steht, ob diese Bücherei gerade geöffnet hat",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Column(Modifier.selectableGroup()) {
                OpeningHours.branches.forEach { branch ->
                    val selected = branch.id == state.overviewBranchId
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { onOverviewBranchChanged(branch.id) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Klick laeuft ueber die ganze Zeile, daher onClick = null.
                        RadioButton(selected = selected, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(branch.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                branch.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Darstellung", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                THEME_OPTIONS.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = state.themeMode == mode,
                        onClick = { onThemeModeChanged(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, THEME_OPTIONS.size),
                        label = { Text(label) },
                    )
                }
            }

            if (supportsDynamicColor) {
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Dynamische Farben", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Farben passend zum Hintergrundbild. Aus: die grünen Farben der App",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.dynamicColor,
                        onCheckedChange = onDynamicColorChanged,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Die App liest die Website der Stadtbücherei aus — es gibt keine " +
                    "offizielle Schnittstelle. Wenn die Bibliothek ihre Seite umbaut, " +
                    "kann die Anzeige zeitweise fehlschlagen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
