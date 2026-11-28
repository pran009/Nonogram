package app.nonogram.puzzle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.ads.AdManager
import app.nonogram.puzzle.ads.ConsentManager
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.Wallet
import app.nonogram.puzzle.logic.Rewards
import app.nonogram.puzzle.data.PuzzlePack
import app.nonogram.puzzle.data.Puzzles
import app.nonogram.puzzle.ui.theme.LocalBoardColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Days since epoch in the device's local time zone; used to seed the daily puzzle. */
fun todayEpochDay(): Long {
    val tz = TimeZone.getDefault()
    val now = System.currentTimeMillis()
    return (now + tz.getOffset(now)) / 86_400_000L
}

fun dailyPuzzleId(epochDay: Long) = "daily-$epochDay"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    store: ProgressStore,
    wallet: Wallet,
    adManager: AdManager,
    consent: ConsentManager,
    refreshKey: Int,
    onOpenPack: (PuzzlePack) -> Unit,
    onPlayDaily: (epochDay: Long) -> Unit,
    onPlayRandom: (size: Int, seed: Long) -> Unit,
    onPlayChallenge: () -> Unit,
    onOpenCollection: () -> Unit,
    onShowTutorial: () -> Unit,
) {
    val context = LocalContext.current
    var showFreeCoins by remember { mutableStateOf(false) }
    var lockedPack by remember { mutableStateOf<PuzzlePack?>(null) }
    var unlockedKey by remember { mutableIntStateOf(0) }
    var randomSize by remember { mutableIntStateOf(10) }
    var checkMistakes by remember(refreshKey) { mutableStateOf(store.checkMistakes) }
    var haptics by remember(refreshKey) { mutableStateOf(store.hapticsEnabled) }
    var sound by remember(refreshKey) { mutableStateOf(store.soundEnabled) }
    var confirmExit by remember(refreshKey) { mutableStateOf(store.confirmExit) }
    val epochDay = remember(refreshKey) { todayEpochDay() }
    val dailyDone = remember(refreshKey) { store.isCompleted(dailyPuzzleId(epochDay)) }
    val dailyStarted = remember(refreshKey) { store.hasSavedBoard(dailyPuzzleId(epochDay)) }
    val dateLabel = remember(epochDay) {
        SimpleDateFormat("EEEE, d MMM", Locale.getDefault()).format(Date(epochDay * 86_400_000L - TimeZone.getDefault().rawOffset))
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoMark()
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Nonogram", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Picture logic puzzles", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CoinBadge(wallet.coins, onClick = { showFreeCoins = true })
            }
            Spacer(Modifier.height(14.dp))
            LevelBar(wallet)
            Spacer(Modifier.height(10.dp))
            FreeCoinsTile(onClick = { showFreeCoins = true })
            Spacer(Modifier.height(16.dp))

            val collected = remember(refreshKey) { store.unmaskedCards().size }
            ChallengeTile(
                collected = collected,
                total = app.nonogram.puzzle.model.CardDeck.size,
                onClick = onPlayChallenge,
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.TextButton(
                onClick = onOpenCollection,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("View card collection ($collected/${app.nonogram.puzzle.model.CardDeck.size})") }

            Spacer(Modifier.height(16.dp))

            SectionTitle("Puzzle packs")
            for (pack in Puzzles.packs) {
                val done = remember(refreshKey, pack.id) { store.completedCount(pack.puzzles.map { it.id }) }
                val locked = remember(refreshKey, unlockedKey, pack.id) { pack.price > 0 && !store.isPackUnlocked(pack.id) }
                PackCard(pack, done, locked, onClick = { if (locked) lockedPack = pack else onOpenPack(pack) })
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(10.dp))
            SectionTitle("Endless")
            Card(
                onClick = { onPlayDaily(epochDay) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Daily puzzle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            when {
                                dailyDone -> "$dateLabel · solved"
                                dailyStarted -> "$dateLabel · in progress"
                                else -> "$dateLabel · 10×10"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    if (dailyDone) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    else Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Casino, null)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Random puzzle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            val solvedRandom = remember(refreshKey) { store.randomSolvedCount }
                            Text(
                                if (solvedRandom > 0) "Always solvable by logic · $solvedRandom solved" else "Always solvable by logic",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (s in listOf(5, 10, 15)) {
                            FilterChip(selected = randomSize == s, onClick = { randomSize = s }, label = { Text("${s}×$s") })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onPlayRandom(randomSize, System.nanoTime()) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Play ${randomSize}×$randomSize", maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Settings")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingRow(
                        title = "Check mistakes",
                        subtitle = "Wrong fills are marked immediately and counted",
                        checked = checkMistakes,
                        onChange = { checkMistakes = it; store.checkMistakes = it },
                    )
                    SettingRow(
                        title = "Sound effects",
                        subtitle = "Coin sounds when you earn coins",
                        checked = sound,
                        onChange = { sound = it; store.soundEnabled = it; CoinSounds.enabled = it },
                    )
                    SettingRow(
                        title = "Vibration",
                        subtitle = "Light feedback when marking cells",
                        checked = haptics,
                        onChange = { haptics = it; store.hapticsEnabled = it },
                    )
                    SettingRow(
                        title = "Confirm before exiting",
                        subtitle = "Ask before closing the app with the back button",
                        checked = confirmExit,
                        onChange = { confirmExit = it; store.confirmExit = it },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("How to play")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HowToLine("The numbers on each row and column tell you the lengths of the runs of filled cells, in order, with at least one empty cell between runs.")
                    HowToLine("Tap a cell to fill it. Drag to fill a whole line. Switch to Cross to mark cells you know are empty.")
                    HowToLine("Pinch with two fingers to zoom in on large boards. Double-tap to zoom back out.")
                    HowToLine("Stuck? The hint button reveals one cell you can deduce from the current board.")
                    HowToLine("Earn coins for every correct line and every solved puzzle. Spend them on hints and new puzzle packs.")
                    androidx.compose.material3.OutlinedButton(onClick = onShowTutorial, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Show tutorial")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle("Help & feedback")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    if (consent.privacyOptionsRequired) {
                        LinkRow(Icons.Default.PrivacyTip, "Privacy settings", "Change your ad consent choices") {
                            context.findActivity()?.let { consent.showPrivacyOptions(it) }
                        }
                    }
                    LinkRow(Icons.Default.Email, "Send feedback", "Report a problem or suggest a feature") { sendFeedback(context) }
                    LinkRow(Icons.Default.Star, "Rate Nonogram", "Enjoying it? A rating helps a lot") { openStorePage(context) }
                    LinkRow(Icons.Default.Share, "Share with friends", "Invite someone to play") {
                        shareText(context, "Try Nonogram — picture logic puzzles with Challenge Mode and collectible cards: $STORE_URL")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showFreeCoins) {
        FreeCoinsDialog(store, wallet, adManager, context.findActivity(), onDismiss = { showFreeCoins = false })
    }
    lockedPack?.let { pack ->
        UnlockPackDialog(
            pack = pack,
            coins = wallet.coins,
            onUnlock = {
                if (wallet.spend(pack.price)) {
                    CoinSounds.shower()
                    store.unlockPack(pack.id)
                    unlockedKey++
                    lockedPack = null
                    onOpenPack(pack)
                }
            },
            onGetCoins = { lockedPack = null; showFreeCoins = true },
            onDismiss = { lockedPack = null },
        )
    }
}

@Composable
private fun LevelBar(wallet: Wallet) {
    val lo = Rewards.xpForLevel(wallet.level)
    val hi = Rewards.xpForLevel(wallet.level + 1)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Level ${wallet.level}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { wallet.levelProgress },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Text("${wallet.xp - lo}/${hi - lo} XP", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FreeCoinsTile(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CoinGold.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Free coins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Watch short ads to earn coins", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LogoMark() {
    val colors = LocalBoardColors.current
    val heart = remember { Puzzles.pack5.puzzles.first() }
    PuzzleThumbnail(
        heart, colors.filled, colors.boardBackground,
        Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.boardBackground)
            .padding(6.dp),
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackCard(pack: PuzzlePack, done: Int, locked: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${pack.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(pack.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (locked) {
                    Text("${pack.size}×${pack.size} · ${pack.puzzles.size} puzzles",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CoinIcon(Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${pack.price} to unlock", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text("${pack.size}×${pack.size} · $done of ${pack.puzzles.size} solved",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (pack.puzzles.isEmpty()) 0f else done.toFloat() / pack.puzzles.size },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            if (locked) Icon(Icons.Default.Lock, "Locked", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            else Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun HowToLine(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
