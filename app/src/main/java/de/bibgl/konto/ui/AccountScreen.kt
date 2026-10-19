package de.bibgl.konto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Toys
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil.compose.AsyncImage
import de.bibgl.konto.data.Account
import de.bibgl.konto.data.LibraryClient
import de.bibgl.konto.data.Loan
import de.bibgl.konto.data.OpeningHours
import de.bibgl.konto.data.Table
import de.bibgl.konto.data.WatchItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountScreen(
    state: UiState,
    onRefresh: () -> Unit,
    onRenew: (String) -> Unit,
    onRenewAll: () -> Unit,
    onRemoveFromWatchlist: (WatchItem) -> Unit,
    onOpenSettings: () -> Unit,
    onLogoutAll: () -> Unit,
    onMessageShown: () -> Unit,
    onConfirmFees: () -> Unit,
    onDismissFees: () -> Unit,
    onSwitchProfile: (String) -> Unit,
    onAddProfile: () -> Unit,
    onRenameProfile: (String, String) -> Unit,
    onRemoveProfile: (String) -> Unit,
    onSectionToggled: (String, Boolean) -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    var menuOpen by remember { mutableStateOf(false) }
    var switcherOpen by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf(false) }
    var logoutAll by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val active = state.activeProfile

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            onMessageShown()
        }
    }

    // Die Bibliothek verlangt fuer manche Verlaengerungen Gebuehren. Die werden
    // erst im Bestaetigungsdialog der Website sichtbar - also hier nachfragen,
    // bevor die App sie im Namen des Nutzers akzeptiert.
    val pendingFees = state.feeConfirmation
    if (pendingFees != null) {
        AlertDialog(
            onDismissRequest = onDismissFees,
            title = { Text("Verlängerung kostet Gebühren") },
            text = {
                Text(
                    if (pendingFees.bulk) {
                        "Für diese ${pendingFees.copyIds.size} Medien fallen " +
                            "${pendingFees.feeText} an Verlängerungs- und Säumnisgebühren an. " +
                            "Trotzdem verlängern?"
                    } else {
                        "Für diese Verlängerung fallen ${pendingFees.feeText} an " +
                            "Verlängerungs- und Säumnisgebühren an. Trotzdem verlängern?"
                    }
                )
            },
            confirmButton = { TextButton(onClick = onConfirmFees) { Text("Verlängern") } },
            dismissButton = { TextButton(onClick = onDismissFees) { Text("Abbrechen") } },
        )
    }

    if (renaming && active != null) {
        RenameDialog(
            current = active.label,
            onDismiss = { renaming = false },
            onConfirm = { newLabel ->
                renaming = false
                onRenameProfile(active.id, newLabel)
            },
        )
    }

    if (removing && active != null) {
        AlertDialog(
            onDismissRequest = { removing = false },
            title = { Text("Konto entfernen") },
            text = {
                Text(
                    "\"${active.label}\" wird aus der App entfernt und die gespeicherten " +
                        "Zugangsdaten werden gelöscht. Das Bibliothekskonto selbst bleibt " +
                        "unverändert."
                )
            },
            confirmButton = {
                TextButton(onClick = { removing = false; onRemoveProfile(active.id) }) {
                    Text("Entfernen")
                }
            },
            dismissButton = { TextButton(onClick = { removing = false }) { Text("Abbrechen") } },
        )
    }

    if (logoutAll) {
        AlertDialog(
            onDismissRequest = { logoutAll = false },
            title = { Text("Alle Konten abmelden") },
            text = {
                Text(
                    "Alle ${state.profiles.size} hinterlegten Ausweise werden entfernt " +
                        "und ihre Zugangsdaten gelöscht."
                )
            },
            confirmButton = {
                TextButton(onClick = { logoutAll = false; onLogoutAll() }) { Text("Abmelden") }
            },
            dismissButton = { TextButton(onClick = { logoutAll = false }) { Text("Abbrechen") } },
        )
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        ProfileSwitcherTitle(
                            label = active?.label ?: "Mein Konto",
                            onClick = { switcherOpen = true },
                        )
                        ProfileSwitcherMenu(
                            expanded = switcherOpen,
                            state = state,
                            onDismiss = { switcherOpen = false },
                            onSwitch = { id -> switcherOpen = false; onSwitchProfile(id) },
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.loading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Einstellungen") },
                                leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
                                onClick = { menuOpen = false; onOpenSettings() },
                            )
                            DropdownMenuItem(
                                text = { Text("Konto hinzufügen") },
                                leadingIcon = { Icon(Icons.Outlined.PersonAdd, null) },
                                onClick = { menuOpen = false; onAddProfile() },
                            )
                            if (active != null) {
                                DropdownMenuItem(
                                    text = { Text("Konto umbenennen") },
                                    leadingIcon = { Icon(Icons.Outlined.DriveFileRenameOutline, null) },
                                    onClick = { menuOpen = false; renaming = true },
                                )
                                DropdownMenuItem(
                                    text = { Text("Dieses Konto entfernen") },
                                    leadingIcon = { Icon(Icons.Outlined.PersonRemove, null) },
                                    onClick = { menuOpen = false; removing = true },
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Über die App") },
                                leadingIcon = { Icon(Icons.Outlined.Info, null) },
                                onClick = { menuOpen = false; showAbout = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Alle Konten abmelden") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                                onClick = { menuOpen = false; logoutAll = true },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        val pullState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            // Expressive: sich verformende Ladeanzeige statt des Kreises.
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullState,
                    isRefreshing = state.loading,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            },
        ) {
            val account = state.account
            if (account == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (state.loading) LoadingIndicator()
                    else Text(state.error ?: "Keine Daten")
                }
            } else {
                AccountContent(
                    account = account,
                    state = state,
                    onRenew = onRenew,
                    onRenewAll = onRenewAll,
                    onRemoveFromWatchlist = onRemoveFromWatchlist,
                    onSectionToggled = onSectionToggled,
                )
            }
        }
    }
}

// Schluessel der aufklappbaren Karten - werden im Store gemerkt, nicht umbenennen.
private const val S_LOANS = "loans"
private const val S_PICKUP = "pickup"
private const val S_RESERVATIONS = "reservations"
private const val S_FEES = "fees"
private const val S_HOURS = "opening_hours"
private const val S_WATCHLIST = "watchlist"
private const val S_CARD = "library_card"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountContent(
    account: Account,
    state: UiState,
    onRenew: (String) -> Unit,
    onRenewAll: () -> Unit,
    onRemoveFromWatchlist: (WatchItem) -> Unit,
    onSectionToggled: (String, Boolean) -> Unit,
) {
    val today = LocalDate.now()
    val loans = account.loansByDueDate
    val now = rememberNow()

    // Jede Karte merkt sich, ob sie auf- oder zugeklappt ist. Solange der Nutzer
    // sie nie angetippt hat, gilt der Standard der jeweiligen Karte.
    fun expanded(key: String, default: Boolean) = state.expandedSections[key] ?: default
    fun toggle(key: String, default: Boolean): () -> Unit =
        { onSectionToggled(key, !expanded(key, default)) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val error = state.error
        if (error != null) {
            item { ErrorBanner(error) }
        }

        item(key = "overview") {
            OverviewCard(account, today, now, OpeningHours.branch(state.overviewBranchId))
        }

        if (!account.readyForPickup.isEmpty) {
            item(key = S_PICKUP) {
                CollapsibleCard(
                    icon = Icons.Outlined.Inventory2,
                    title = "Abholbereit",
                    count = account.readyForPickup.rows.size,
                    expanded = expanded(S_PICKUP, true),
                    onToggle = toggle(S_PICKUP, true),
                ) {
                    TableContent(account.readyForPickup)
                }
            }
        }

        item(key = S_LOANS) {
            CollapsibleCard(
                icon = Icons.Outlined.MenuBook,
                title = "Ausgeliehen",
                count = loans.size,
                expanded = expanded(S_LOANS, true),
                onToggle = toggle(S_LOANS, true),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
            ) {
                if (loans.isEmpty()) {
                    EmptyText("Nichts ausgeliehen", Modifier.padding(horizontal = 8.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        loans.forEach { loan ->
                            key(loan.copyId) {
                                LoanCard(
                                    loan = loan,
                                    today = today,
                                    busy = loan.copyId in state.renewing,
                                    onRenew = { onRenew(loan.copyId) },
                                )
                            }
                        }
                        val renewable = account.renewableLoans
                        if (renewable.size > 1) {
                            FilledTonalButton(
                                onClick = onRenewAll,
                                shapes = ButtonDefaults.shapes(),
                                enabled = state.renewing.isEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Outlined.Autorenew, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Alle ${renewable.size} verlängerbaren verlängern")
                            }
                        }
                    }
                }
            }
        }

        if (!account.reservations.isEmpty) {
            item(key = S_RESERVATIONS) {
                CollapsibleCard(
                    icon = Icons.Outlined.Schedule,
                    title = "Vorgemerkt",
                    count = account.reservations.rows.size,
                    expanded = expanded(S_RESERVATIONS, true),
                    onToggle = toggle(S_RESERVATIONS, true),
                ) {
                    TableContent(account.reservations)
                }
            }
        }

        if (account.watchlist.isNotEmpty()) {
            item(key = S_WATCHLIST) {
                WatchlistCard(
                    items = account.watchlist,
                    total = account.watchlistTotal,
                    removing = state.removingWatch,
                    onRemove = onRemoveFromWatchlist,
                    expanded = expanded(S_WATCHLIST, false),
                    onToggle = toggle(S_WATCHLIST, false),
                )
            }
        }

        // Ohne offene Gebuehren ist die Karte uninteressant und startet zugeklappt.
        val feesDefault = account.fees.hasOpenFees
        item(key = S_FEES) {
            CollapsibleCard(
                icon = Icons.Outlined.Euro,
                title = "Gebühren",
                subtitle = "${account.fees.open} offen",
                subtitleColor = if (account.fees.hasOpenFees) MaterialTheme.colorScheme.error
                else null,
                expanded = expanded(S_FEES, feesDefault),
                onToggle = toggle(S_FEES, feesDefault),
            ) {
                FeesContent(account)
            }
        }

        item(key = S_HOURS) {
            OpeningHoursCard(
                now = now,
                expanded = expanded(S_HOURS, false),
                onToggle = toggle(S_HOURS, false),
            )
        }

        item(key = S_CARD) {
            LibraryCardCard(
                account = account,
                today = today,
                expanded = expanded(S_CARD, false),
                onToggle = toggle(S_CARD, false),
            )
        }

        item { FooterNote(account) }
    }
}

// ------------------------------------------------------- Konto-Umschalter

@Composable
private fun ProfileSwitcherTitle(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = "Konto wechseln",
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/**
 * Liste der hinterlegten Ausweise. Zu jedem steht der zuletzt geladene Stand,
 * damit man ohne Umschalten sieht, wo etwas ansteht.
 */
@Composable
private fun ProfileSwitcherMenu(
    expanded: Boolean,
    state: UiState,
    onDismiss: () -> Unit,
    onSwitch: (String) -> Unit,
) {
    val today = LocalDate.now()
    val dark = MaterialTheme.colorScheme.surface.luminanceIsDark()

    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        state.profiles.forEach { profile ->
            val preview = state.previews[profile.id]
            val isActive = profile.id == state.activeProfileId
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            profile.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        Text(
                            profileSummary(preview, today),
                            style = MaterialTheme.typography.bodySmall,
                            color = profileSummaryColor(preview, today, dark),
                        )
                    }
                },
                leadingIcon = {
                    if (isActive) Icon(Icons.Default.Check, contentDescription = "aktiv")
                    else Icon(Icons.Outlined.Person, contentDescription = null)
                },
                onClick = { onSwitch(profile.id) },
            )
        }
    }
}

private fun profileSummary(account: Account?, today: LocalDate): String {
    if (account == null) return "noch nicht geladen"
    val loans = account.loans
    if (loans.isEmpty()) {
        return if (account.fees.hasOpenFees) "nichts ausgeliehen · ${account.fees.open} offen"
        else "nichts ausgeliehen"
    }
    val days = account.loansByDueDate.firstOrNull()?.daysLeft(today)
    val base = when {
        days == null -> "${loans.size} ${mediaWord(loans.size)}"
        days < 0L -> "${loans.size} ${mediaWord(loans.size)} · überfällig"
        days == 0L -> "${loans.size} ${mediaWord(loans.size)} · heute fällig"
        days == 1L -> "${loans.size} ${mediaWord(loans.size)} · morgen fällig"
        else -> "${loans.size} ${mediaWord(loans.size)} · noch $days Tage"
    }
    return if (account.fees.hasOpenFees) "$base · ${account.fees.open}" else base
}

@Composable
private fun profileSummaryColor(account: Account?, today: LocalDate, dark: Boolean): Color {
    val days = account?.loansByDueDate?.firstOrNull()?.daysLeft(today)
    return when {
        account == null || days == null -> MaterialTheme.colorScheme.onSurfaceVariant
        days < 0L -> if (dark) DueColors.overdueDark else DueColors.overdue
        days <= 2L -> if (dark) DueColors.urgentDark else DueColors.urgent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun mediaWord(n: Int) = if (n == 1) "Medium" else "Medien"

@Composable
private fun RenameDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konto umbenennen") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Name") },
                singleLine = true,
                placeholder = { Text("z.B. Mia, Partner, eigener Ausweis") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value.trim()) },
                enabled = value.isNotBlank(),
            ) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

// ------------------------------------------------------------------ Bausteine

/** Name, Ausweisnummer und Gueltigkeit - laeuft der Ausweis bald ab, steht hier die Warnung. */
@Composable
private fun LibraryCardCard(
    account: Account,
    today: LocalDate,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val warn = cardExpiryWarning(account, today)
    val validity = warn ?: account.cardValidUntil?.let { "gültig bis ${account.cardValidUntilRaw}" }
    val validityColor = if (warn != null) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurfaceVariant

    // Zugeklappt steht die Gueltigkeit im Kopf, damit eine Ablauf-Warnung nicht
    // in der eingeklappten Karte verschwindet; aufgeklappt steht sie unter der Nummer.
    CollapsibleCard(
        icon = Icons.Outlined.CreditCard,
        title = "Ausweis",
        subtitle = if (expanded) null else validity ?: account.cardNumber,
        subtitleColor = if (validity != null) validityColor else null,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        if (account.patronName.isNotBlank()) {
            Text(account.patronName, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "Ausweis ${account.cardNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (validity != null) {
            Text(
                validity,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (warn != null) FontWeight.SemiBold else FontWeight.Normal,
                color = validityColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * Warnung zum Ausweisablauf, erst ab 60 Tagen vorher - sonst ist es Rauschen.
 * Null, solange kein Hinweis noetig ist.
 */
private fun cardExpiryWarning(account: Account, today: LocalDate): String? {
    val days = account.cardDaysLeft(today) ?: return null
    val date = account.cardValidUntilRaw
    return when {
        days > 60 -> null
        days < 0 -> "Ausweis abgelaufen ($date)"
        days == 0L -> "Ausweis läuft heute ab"
        days == 1L -> "Ausweis läuft morgen ab ($date)"
        else -> "Ausweis läuft in $days Tagen ab ($date)"
    }
}

@Composable
private fun OverviewCard(
    account: Account,
    today: LocalDate,
    now: LocalDateTime,
    branch: OpeningHours.Branch,
) {
    val next = account.loansByDueDate.firstOrNull()
    val libraryStatus = OpeningHours.status(branch, now)
    val cardWarning = cardExpiryWarning(account, today)

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            // Ob die in den Einstellungen gewaehlte Buecherei gerade offen hat -
            // beantwortet "kann ich jetzt hin?". Name oben, Status darunter.
            Row {
                val color = openingStatusColor(libraryStatus)
                Icon(
                    Icons.Outlined.LocalLibrary,
                    null,
                    Modifier
                        .padding(top = 1.dp)
                        .size(18.dp),
                    tint = color,
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        branch.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        OpeningHours.statusText(libraryStatus, now),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                    )
                }
            }
            if (cardWarning != null) {
                Spacer(Modifier.height(8.dp))
                NoticeLine(
                    icon = Icons.Outlined.CreditCard,
                    text = cardWarning,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // Die naechste Frist ist das Wichtigste auf der Karte und steht deshalb
            // gross oben links; die Gebuehren folgen klein darunter.
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (next != null) {
                    val days = next.daysLeft(today)
                    Column(Modifier.weight(1f)) {
                        Stat(
                            "Nächste Rückgabe",
                            countdownLabel(days),
                            valueColor = countdownColor(days),
                        )
                        Text(
                            next.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Stat("Ausgeliehen", account.loans.size.toString())
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Offene Gebühren",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    account.fees.open,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (account.fees.hasOpenFees) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Kurzer farbiger Hinweis mit Symbol, z.B. Oeffnungsstatus oder Ausweisablauf. */
@Composable
private fun NoticeLine(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(18.dp), tint = color)
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun Stat(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoanCard(loan: Loan, today: LocalDate, busy: Boolean, onRenew: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (loan.detailUrl != null) {
                    Modifier.clickable { uriHandler.openUri(loan.detailUrl) }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row {
                AsyncImage(
                    model = loan.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 54.dp, height = 78.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    if (loan.mediaType.isNotBlank()) {
                        MediaTypeLabel(loan.mediaType)
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(
                        loan.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 3,
                    )
                    if (loan.author.isNotBlank()) {
                        Text(
                            loan.author,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    CountdownChip(loan, today)
                    Text(
                        "Frist ${loan.dueDateRaw}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            RenewRow(loan, busy, onRenew)
        }
    }
}

/** Medienart wie von der Bibliothek geliefert ("Buch", "Tonie", "Spiel" ...), mit passendem Symbol. */
@Composable
private fun MediaTypeLabel(mediaType: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            mediaTypeIcon(mediaType),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            mediaType,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Die Bibliothek liefert die Medienart als Freitext. Reihenfolge beachten:
 * "Hörbuch" und "Bilderbuch" enthalten "buch", "Konsolenspiel" enthaelt "spiel".
 */
private fun mediaTypeIcon(mediaType: String): ImageVector {
    val t = mediaType.lowercase()
    return when {
        "hörbuch" in t || "hoerbuch" in t -> Icons.Outlined.Headphones
        "tonie" in t -> Icons.Outlined.Toys
        "bilderbuch" in t -> Icons.Outlined.AutoStories
        "konsole" in t || "videospiel" in t || "switch" in t || "playstation" in t ->
            Icons.Outlined.SportsEsports
        "spiel" in t -> Icons.Outlined.Extension
        "dvd" in t || "blu-ray" in t || "film" in t -> Icons.Outlined.Movie
        "cd" in t || "musik" in t -> Icons.Outlined.Album
        "zeitschrift" in t || "zeitung" in t -> Icons.Outlined.Newspaper
        "buch" in t || "roman" in t -> Icons.Outlined.MenuBook
        else -> Icons.Outlined.Category
    }
}

@Composable
private fun RenewRow(loan: Loan, busy: Boolean, onRenew: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            when {
                busy -> "wird verlängert…"
                loan.renewable == true && loan.renewToDate != null ->
                    "verlängerbar bis ${loan.renewToRaw}"
                loan.renewable == true -> "verlängerbar"
                loan.renewable == false -> loan.renewNote ?: "nicht verlängerbar"
                else -> "Status unbekannt"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (loan.renewable == false) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )

        // Bei unbekanntem Status trotzdem anbieten - der Server entscheidet ohnehin.
        if (loan.renewable != false && loan.extendTarget != null) {
            TextButton(onClick = onRenew, enabled = !busy, shapes = ButtonDefaults.shapes()) {
                if (busy) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Autorenew, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Verlängern")
                }
            }
        }
    }
}

@Composable
private fun CountdownChip(loan: Loan, today: LocalDate) {
    val days: Long? = loan.daysLeft(today)
    val color = countdownColor(days)
    Text(
        countdownLabel(days),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Ampelfarbe der Frist: rot ueberfaellig, orange <= 2 Tage, gelb <= 7, sonst gruen. */
@Composable
private fun countdownColor(days: Long?): Color {
    val dark = MaterialTheme.colorScheme.surface.luminanceIsDark()
    return when {
        days == null -> MaterialTheme.colorScheme.onSurfaceVariant
        days < 0L -> if (dark) DueColors.overdueDark else DueColors.overdue
        days <= 2L -> if (dark) DueColors.urgentDark else DueColors.urgent
        days <= 7L -> if (dark) DueColors.soonDark else DueColors.soon
        else -> if (dark) DueColors.okDark else DueColors.ok
    }
}

private fun countdownLabel(days: Long?): String = when {
    days == null -> "ohne Frist"
    days < 0L -> "${-days} ${dayWord(-days)} überfällig"
    days == 0L -> "heute fällig"
    days == 1L -> "morgen fällig"
    else -> "noch $days Tage"
}

private fun dayWord(n: Long) = if (n == 1L) "Tag" else "Tage"

/** Grobe Helligkeitsprobe, um die Ampelfarben an Hell/Dunkel anzupassen. */
private fun Color.luminanceIsDark(): Boolean = (red + green + blue) / 3f < 0.5f

/**
 * Karte mit Kopfzeile zum Auf- und Zuklappen. Die Unterzeile im Kopf bleibt
 * auch zugeklappt sichtbar - dort steht das Wichtigste der Karte.
 */
@Composable
private fun CollapsibleCard(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    count: Int? = null,
    subtitle: String? = null,
    subtitleColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = if (expanded) "Zuklappen" else "Aufklappen",
                        onClick = onToggle,
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, null)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        if (count != null) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                count.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(contentPadding), content = content)
            }
        }
    }
}

@Composable
private fun FeesContent(account: Account) {
    KeyValue("Offene Gebühren", account.fees.open, account.fees.hasOpenFees)
    KeyValue("Einzahlungen", account.fees.paid)
    KeyValue("Kompletter Saldo", account.fees.balance)
    if (!account.fees.table.isEmpty) {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        account.fees.table.asMaps().forEach { row ->
            Spacer(Modifier.height(8.dp))
            row.forEach { (k, v) -> KeyValue(k, v) }
        }
    }
    if (account.fees.hasOpenFees) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Bezahlen ist nur auf der Website der Stadtbücherei möglich.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Oeffnungszeiten aller Standorte. Im Kopf steht, ob die Hauptstelle im Forum
 * gerade geoeffnet hat; der Status laeuft minutengenau mit.
 */
@Composable
private fun OpeningHoursCard(now: LocalDateTime, expanded: Boolean, onToggle: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    // Der Status der Hauptstelle steht schon in der Uebersicht - hier nur die Standorte.
    CollapsibleCard(
        icon = Icons.Outlined.LocalLibrary,
        title = "Öffnungszeiten",
        subtitle = OpeningHours.branches.joinToString(" · ") { it.shortName },
        expanded = expanded,
        onToggle = onToggle,
    ) {
        OpeningHours.branches.forEachIndexed { index, branch ->
            if (index > 0) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
            }
            val status = OpeningHours.status(branch, now)
            Text(branch.name, style = MaterialTheme.typography.titleSmall)
            Text(
                branch.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                OpeningHours.statusText(status, now),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = openingStatusColor(status),
                modifier = Modifier.padding(vertical = 4.dp),
            )
            // Sonntags hat keine Stelle offen - die Zeile wuerde nur Platz kosten.
            DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }.forEach { day ->
                val isToday = day == now.dayOfWeek
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val weight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                    Text(
                        OpeningHours.dayName(day),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = weight,
                        color = if (isToday) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        OpeningHours.rangesText(branch.hours[day].orEmpty()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = weight,
                    )
                }
            }
        }
        // Die Zeiten sind fest hinterlegt - der Stand zeigt, wie alt sie sein koennen.
        Text(
            "Stand: ${OpeningHours.AS_OF}. Gesetzliche Feiertage sind berücksichtigt, " +
                "Sonderschließungen nicht.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "Aktuelle Zeiten auf der Website der Stadt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { uriHandler.openUri(OpeningHours.SOURCE_URL) },
        )
    }
}

/**
 * Uhrzeit in Deutschland fuer den Oeffnungsstatus. Wird nur beim Oeffnen der App
 * bzw. beim Zurueckkehren in sie neu bestimmt - bewusst ohne Timer.
 */
@Composable
private fun rememberNow(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now(OpeningHours.ZONE)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        now = LocalDateTime.now(OpeningHours.ZONE)
    }
    return now
}

/** Gruen offen, orange kurz vor Schluss, rot geschlossen - wie bei der Frist-Ampel. */
@Composable
private fun openingStatusColor(status: OpeningHours.Status): Color {
    val dark = MaterialTheme.colorScheme.surface.luminanceIsDark()
    return when {
        status is OpeningHours.Status.Open && status.closesSoon ->
            if (dark) DueColors.urgentDark else DueColors.urgent
        status is OpeningHours.Status.Open -> if (dark) DueColors.okDark else DueColors.ok
        else -> if (dark) DueColors.overdueDark else DueColors.overdue
    }
}

@Composable
private fun KeyValue(label: String, value: String, highlight: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Vormerkungen und Abholbereit haben je nach Bestand andere Spalten, deshalb
 * werden sie generisch als Label/Wert-Paare gezeigt statt als feste Tabelle.
 */
@Composable
private fun TableContent(table: Table) {
    table.asMaps().forEachIndexed { index, row ->
        if (index > 0) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
        }
        row.forEach { (k, v) ->
            if (k.equals("Cover", true) || k.isBlank()) return@forEach
            KeyValue(k, v)
        }
    }
}

@Composable
private fun WatchlistCard(
    items: List<WatchItem>,
    total: Int?,
    removing: Set<String>,
    onRemove: (WatchItem) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    // Die App liest nur die erste Seite der Merkliste; der Reiter nennt die Gesamtzahl.
    val count = maxOf(total ?: 0, items.size)
    val truncated = count > items.size
    var confirm by remember { mutableStateOf<WatchItem?>(null) }
    val uriHandler = LocalUriHandler.current

    // Auf der Website laesst sich das nicht rueckgaengig machen - also nachfragen.
    confirm?.let { item ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Von der Merkliste entfernen") },
            text = { Text("\"${item.title}\" von der Merkliste entfernen?") },
            confirmButton = {
                TextButton(onClick = { confirm = null; onRemove(item) }) { Text("Entfernen") }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Abbrechen") } },
        )
    }

    CollapsibleCard(
        icon = Icons.Outlined.Bookmarks,
        title = "Merkliste",
        count = count,
        expanded = expanded,
        onToggle = onToggle,
        contentPadding = PaddingValues(start = 16.dp, end = 4.dp, bottom = 8.dp),
    ) {
        items.forEach { item ->
            val url = item.url
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (url != null) Modifier.clickable { uriHandler.openUri(url) }
                            else Modifier
                        )
                        .padding(vertical = 6.dp),
                )
                if (item.mediaId in removing) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                } else if (item.removeTarget != null && item.mediaId.isNotEmpty()) {
                    IconButton(onClick = { confirm = item }) {
                        Icon(
                            Icons.Outlined.BookmarkRemove,
                            contentDescription = "${item.title} von der Merkliste entfernen",
                        )
                    }
                }
            }
        }
        Text(
            if (truncated) {
                "Die App zeigt die ersten ${items.size} von $count Einträgen. " +
                    "Alle Einträge siehst du auf der Website."
            } else {
                "Die App zeigt bis zu ${LibraryClient.WATCHLIST_PAGE_SIZE} Einträge der Merkliste."
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (truncated) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 12.dp)
                .then(
                    if (truncated) {
                        Modifier.clickable { uriHandler.openUri(LibraryClient.ACCOUNT_URL) }
                    } else Modifier
                )
                .padding(top = 8.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun EmptyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun ErrorBanner(text: String) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun FooterNote(account: Account) {
    val stamp = remember(account.fetchedAt) {
        if (account.fetchedAt == 0L) "" else android.text.format.DateFormat
            .format("dd.MM.yyyy HH:mm", account.fetchedAt).toString()
    }
    Text(
        if (stamp.isEmpty()) "" else "Stand: $stamp",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    )
}
