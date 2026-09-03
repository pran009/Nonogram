package app.nonogram.puzzle.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.ads.AdManager
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.logic.ChallengeDifficulty
import app.nonogram.puzzle.logic.ChallengeScoring
import app.nonogram.puzzle.model.CardDeck
import app.nonogram.puzzle.model.CellState
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.ui.theme.LocalBoardColors
import kotlinx.coroutines.delay

private fun Context.activity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeGameScreen(
    puzzle: Puzzle,
    difficulty: ChallengeDifficulty,
    store: ProgressStore,
    adManager: AdManager,
    onBack: () -> Unit,
    onPlayAgain: () -> Unit,
    onOpenCollection: () -> Unit,
) {
    val controller = remember(puzzle.id) {
        GameController(puzzle, store, persist = false, checkMistakesOverride = true)
    }
    val haptics = LocalHapticFeedback.current
    val boardColors = LocalBoardColors.current
    val context = LocalContext.current
    val gamesPlayedAtStart = remember(puzzle.id) { store.challengeGamesPlayed }

    var showResult by remember { mutableStateOf(false) }
    var finalScore by remember { mutableIntStateOf(0) }
    var newlyUnmasked by remember { mutableStateOf(false) }

    // Hint ad flow state.
    var hintPromptCost by remember { mutableStateOf<Int?>(null) }
    var adProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(controller, controller.solved) {
        while (!controller.solved) { delay(1000); controller.tick() }
    }
    LaunchedEffect(controller.lastChangeTick) {
        if (controller.lastChangeTick != 0L && store.hapticsEnabled) {
            haptics.performHapticFeedback(
                if (controller.lastChangeWasMistake) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
        }
    }
    LaunchedEffect(controller.mistakeCell) {
        if (controller.mistakeCell != null) { delay(700); controller.clearMistakeFlash() }
    }
    LaunchedEffect(controller.solved) {
        if (controller.solved) {
            val score = ChallengeScoring.score(difficulty, controller.seconds, controller.mistakes, controller.hintsUsed)
            val card = CardDeck.cardForScore(score)
            finalScore = score
            newlyUnmasked = store.unmaskCard(card.number)
            store.challengeGamesPlayed = gamesPlayedAtStart + 1
            if (score > store.challengeBestScore) store.challengeBestScore = score
            delay(350)
            showResult = true
        }
    }

    fun requestHint() {
        val cost = ChallengeScoring.hintAdCost(difficulty, controller.hintsUsed + 1, gamesPlayedAtStart)
        hintPromptCost = cost
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenge · ${difficulty.label}", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(formatTime(controller.seconds), style = MaterialTheme.typography.titleMedium)
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${puzzle.rows}×${puzzle.cols}", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hints: ${controller.hintsUsed}", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Mistakes: ${controller.mistakes}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (controller.mistakes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val rowDone = (0 until puzzle.rows).map { controller.rowDone(it) }
            val colDone = (0 until puzzle.cols).map { controller.colDone(it) }

            BoardCanvas(
                puzzle = puzzle,
                cells = controller.cells,
                rowDone = rowDone,
                colDone = colDone,
                hintCell = controller.hintCell,
                mistakeCell = controller.mistakeCell,
                solved = controller.solved,
                colors = boardColors,
                onBegin = { controller.beginStroke(it) },
                onContinue = { i, brush, original -> controller.continueStroke(i, brush, original) },
                onEnd = { controller.endStroke() },
                onCancel = { controller.cancelStroke() },
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
            )

            Row(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SingleChoiceSegmentedButtonRow(Modifier.height(44.dp)) {
                    SegmentedButton(
                        selected = controller.mode == CellState.FILLED,
                        onClick = { controller.mode = CellState.FILLED },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        icon = { Icon(Icons.Default.Square, null, Modifier.size(16.dp)) },
                        label = { Text("Fill") },
                    )
                    SegmentedButton(
                        selected = controller.mode == CellState.CROSSED,
                        onClick = { controller.mode = CellState.CROSSED },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        icon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                        label = { Text("Cross") },
                    )
                }
                Row {
                    FilledTonalIconButton(onClick = { controller.undo() }, enabled = controller.canUndo && !controller.solved) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    FilledTonalIconButton(onClick = { requestHint() }, enabled = !controller.solved) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Hint")
                    }
                }
            }
        }
    }

    // Hint prompt: watch N ads to unlock.
    hintPromptCost?.let { cost ->
        val penalty = difficulty.hintPenalty
        AlertDialog(
            onDismissRequest = { if (adProgress == null) hintPromptCost = null },
            title = { Text("Unlock a hint") },
            text = {
                val prog = adProgress
                if (prog != null) {
                    Text("Loading ad ${prog.first} of ${prog.second}…")
                } else {
                    Text(
                        "Watch ${if (cost == 1) "1 ad" else "$cost ads"} to reveal one square." +
                            "\n\nHeads up: each hint lowers your Challenge score by $penalty points."
                    )
                }
            },
            confirmButton = {
                if (adProgress == null) {
                    Button(onClick = {
                        val act = context.activity()
                        if (act != null) {
                            adProgress = 0 to cost
                            adManager.showRewardedAds(
                                act, cost,
                                onProgress = { shown, total -> adProgress = shown to total },
                                onComplete = { adProgress = null; hintPromptCost = null; controller.hint() },
                            )
                        } else {
                            hintPromptCost = null
                        }
                    }) { Text(if (cost == 1) "Watch ad" else "Watch $cost ads") }
                }
            },
            dismissButton = {
                if (adProgress == null) TextButton(onClick = { hintPromptCost = null }) { Text("Cancel") }
            },
        )
    }

    // Result: reveal the card.
    if (showResult) {
        val card = remember(finalScore) { CardDeck.cardForScore(finalScore) }
        AlertDialog(
            onDismissRequest = { },
            title = { Text(if (newlyUnmasked) "New card!" else "Card earned") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CardReveal(card)
                    Spacer(Modifier.height(12.dp))
                    Text("Score $finalScore", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "${difficulty.label} · ${formatTime(controller.seconds)} · " +
                            "${controller.mistakes} mistakes · ${controller.hintsUsed} hints",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!newlyUnmasked) {
                        Text("Already in your collection", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = { Button(onClick = onPlayAgain) { Text("Play again") } },
            dismissButton = {
                Row {
                    TextButton(onClick = onOpenCollection) { Text("Collection") }
                    TextButton(onClick = onBack) { Text("Home") }
                }
            },
        )
    }

    DisposableEffect(Unit) { onDispose { } }
}
