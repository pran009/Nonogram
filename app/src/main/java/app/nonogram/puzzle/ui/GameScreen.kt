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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.model.CellState
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.ui.theme.LocalBoardColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    puzzle: Puzzle,
    title: String,
    store: ProgressStore,
    nextPuzzle: Puzzle?,
    onBack: () -> Unit,
    onNext: (Puzzle) -> Unit,
) {
    val controller = remember(puzzle.id) { GameController(puzzle, store) }
    val haptics = LocalHapticFeedback.current
    val boardColors = LocalBoardColors.current
    var showRestart by remember { mutableStateOf(false) }
    var showWin by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(controller, controller.solved) {
        while (!controller.solved) {
            delay(1000)
            controller.tick()
        }
    }
    // Persist when leaving
    DisposableEffect(controller) { onDispose { controller.save() } }

    // Haptics on changes
    LaunchedEffect(controller.lastChangeTick) {
        if (controller.lastChangeTick == 0L) return@LaunchedEffect
        if (store.hapticsEnabled) {
            haptics.performHapticFeedback(
                if (controller.lastChangeWasMistake) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
        }
    }
    LaunchedEffect(controller.mistakeCell) {
        if (controller.mistakeCell != null) { delay(700); controller.clearMistakeFlash() }
    }
    LaunchedEffect(controller.solved) {
        if (controller.solved) { delay(350); showWin = true }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${puzzle.rows}×${puzzle.cols}", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (controller.checkMistakes) {
                    Text(
                        "Mistakes: ${controller.mistakes}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (controller.mistakes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text("Free play", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
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
                    FilledTonalIconButton(onClick = { controller.hint() }, enabled = !controller.solved) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Hint")
                    }
                    FilledTonalIconButton(onClick = { showRestart = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart")
                    }
                }
            }
        }
    }

    if (showRestart) {
        AlertDialog(
            onDismissRequest = { showRestart = false },
            title = { Text("Restart puzzle?") },
            text = { Text("Your progress on this puzzle will be cleared.") },
            confirmButton = { TextButton(onClick = { controller.restart(); showRestart = false }) { Text("Restart") } },
            dismissButton = { TextButton(onClick = { showRestart = false }) { Text("Cancel") } },
        )
    }

    if (showWin) {
        val best = store.bestTimeSeconds(puzzle.id)
        AlertDialog(
            onDismissRequest = { showWin = false },
            title = { Text("Solved: ${puzzle.name}") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    PuzzleThumbnail(
                        puzzle, boardColors.solvedFill, boardColors.boardBackground,
                        Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(boardColors.boardBackground),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Time ${formatTime(controller.seconds)}", style = MaterialTheme.typography.titleMedium)
                    if (best != null && best < controller.seconds) {
                        Text("Best ${formatTime(best)}", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val details = buildList {
                        if (controller.checkMistakes) add("${controller.mistakes} mistakes")
                        if (controller.hintsUsed > 0) add("${controller.hintsUsed} hints")
                    }
                    if (details.isNotEmpty()) {
                        Text(details.joinToString(" · "), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                if (nextPuzzle != null) {
                    Button(onClick = { showWin = false; onNext(nextPuzzle) }) { Text("Next puzzle") }
                } else {
                    Button(onClick = { showWin = false; onBack() }) { Text("Done") }
                }
            },
            dismissButton = { TextButton(onClick = { showWin = false }) { Text("Admire") } },
        )
    }
}
