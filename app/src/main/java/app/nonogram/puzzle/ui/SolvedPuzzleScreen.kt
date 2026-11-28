package app.nonogram.puzzle.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.ads.AdManager
import app.nonogram.puzzle.data.Facts
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.ui.theme.LocalBoardColors

/**
 * What a solved pack puzzle opens to: the finished picture, best time, the fact card (for the
 * themed packs, with its Secret) and a Replay button. Replaying works as before: one ad, no coins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolvedPuzzleScreen(
    puzzle: Puzzle,
    title: String,
    store: ProgressStore,
    adManager: AdManager,
    onBack: () -> Unit,
    onReplay: () -> Unit,
) {
    val context = LocalContext.current
    val colors = LocalBoardColors.current
    val fact = remember(puzzle.id) { Facts.of(puzzle.id) }
    val best = remember(puzzle.id) { store.bestTimeSeconds(puzzle.id) }
    var secretUnlocked by remember { mutableStateOf(store.isSecretUnlocked(puzzle.id)) }
    var justUnlocked by remember { mutableStateOf(false) }
    var adProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var adMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PuzzleThumbnail(
                    puzzle, colors.solvedFill, colors.boardBackground,
                    Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.boardBackground)
                        .padding(6.dp),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Solved", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(puzzle.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (best != null) {
                        Text("Best time ${formatTime(best)}", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (fact != null) {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    FactContent(
                        puzzle = puzzle,
                        fact = fact,
                        secretUnlocked = secretUnlocked,
                        justUnlocked = justUnlocked,
                        adProgress = adProgress,
                        adMessage = adMessage,
                        onWatchAds = {
                            val act = context.findActivity()
                            if (act != null) {
                                adProgress = 0 to 2; adMessage = null
                                adManager.showStrictRewardedAds(act, 2, onProgress = { shown, total -> adProgress = shown to total }) { ok ->
                                    adProgress = null
                                    if (ok) {
                                        store.unlockSecret(puzzle.id)
                                        justUnlocked = true
                                        secretUnlocked = true
                                    } else {
                                        adMessage = "The secret unlocks after both ads are watched to the end."
                                    }
                                }
                            }
                        },
                    )
                }
            }

            Button(onClick = onReplay, enabled = adProgress == null, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Replay")
            }
            Text(
                "Replays start with a short ad and don't earn coins." +
                    if (fact != null && !secretUnlocked) " Replay with no mistakes to reveal the secret." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
