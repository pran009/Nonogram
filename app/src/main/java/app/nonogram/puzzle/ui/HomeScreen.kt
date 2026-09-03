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
import app.nonogram.puzzle.data.ProgressStore
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
    refreshKey: Int,
    onOpenPack: (PuzzlePack) -> Unit,
    onPlayDaily: (epochDay: Long) -> Unit,
    onPlayRandom: (size: Int, seed: Long) -> Unit,
) {
    val boardColors = LocalBoardColors.current
    var randomSize by remember { mutableIntStateOf(10) }
    var checkMistakes by remember(refreshKey) { mutableStateOf(store.checkMistakes) }
    var haptics by remember(refreshKey) { mutableStateOf(store.hapticsEnabled) }
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
                Column {
                    Text("Nonogram", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Picture logic puzzles", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(20.dp))

            SectionTitle("Puzzle packs")
            for (pack in Puzzles.packs) {
                val done = remember(refreshKey, pack.id) { store.completedCount(pack.puzzles.map { it.id }) }
                PackCard(pack, done, onClick = { onOpenPack(pack) })
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
                        Spacer(Modifier.weight(1f))
                        Button(onClick = { onPlayRandom(randomSize, System.nanoTime()) }) { Text("Play") }
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
                        title = "Vibration",
                        subtitle = "Light feedback when marking cells",
                        checked = haptics,
                        onChange = { haptics = it; store.hapticsEnabled = it },
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
                }
            }
            Spacer(Modifier.height(24.dp))
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
private fun PackCard(pack: PuzzlePack, done: Int, onClick: () -> Unit) {
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
                Text("${pack.size}×${pack.size} · $done of ${pack.puzzles.size} solved",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (pack.puzzles.isEmpty()) 0f else done.toFloat() / pack.puzzles.size },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
