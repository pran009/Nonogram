package app.nonogram.puzzle.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.Puzzles
import app.nonogram.puzzle.model.CardDeck
import app.nonogram.puzzle.ui.theme.LocalBoardColors

private const val STEP_COUNT = 5
private const val PRACTICE_STEP = 2

/**
 * First-launch walkthrough. Five short steps, the third of which is a real, playable practice
 * board. Skippable at any point; also reachable later from the home screen.
 */
@Composable
fun TutorialScreen(store: ProgressStore, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    // Practice board: plain in-memory game, never saved.
    val practice = remember { GameController(Puzzles.tutorial, store, persist = false, checkMistakesOverride = true) }

    fun finish() { store.tutorialSeen = true; onDone() }
    BackHandler { if (step > 0) step-- else finish() }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Step ${step + 1} of $STEP_COUNT", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                if (step < STEP_COUNT - 1) TextButton(onClick = { finish() }) { Text("Skip") }
            }

            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (step) {
                    0 -> WelcomeStep()
                    1 -> CluesStep()
                    PRACTICE_STEP -> PracticeStep(practice)
                    3 -> ToolsStep()
                    else -> ChallengeStep()
                }
            }

            // Page dots
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                repeat(STEP_COUNT) { i ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == step) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) TextButton(onClick = { step-- }) { Text("Back") }
                Spacer(Modifier.weight(1f))
                val isLast = step == STEP_COUNT - 1
                Button(onClick = { if (isLast) finish() else step++ }) {
                    Text(
                        when {
                            isLast -> "Let's play"
                            step == PRACTICE_STEP && !practice.solved -> "Skip practice"
                            else -> "Next"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Spacer(Modifier.height(10.dp))
    Text(body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun WelcomeStep() {
    val colors = LocalBoardColors.current
    PuzzleThumbnail(
        Puzzles.pack5.puzzles.first(), colors.filled, colors.boardBackground,
        Modifier.size(140.dp).clip(RoundedCornerShape(16.dp)).background(colors.boardBackground).padding(14.dp),
    )
    Spacer(Modifier.height(24.dp))
    StepTitle(
        "Welcome to Nonogram",
        "Every puzzle hides a picture. Use the number clues to work out which squares to fill, and the picture appears. No guessing needed — every puzzle can be solved by logic.",
    )
}

/** Static illustration: a 6-cell row whose clue is "3 1". */
@Composable
private fun CluesStep() {
    val colors = LocalBoardColors.current
    val row = listOf(true, true, true, false, true, false)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("3 1", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 12.dp))
        Row(Modifier.background(colors.gridThick).padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (filled in row) {
                Box(Modifier.size(34.dp).background(if (filled) colors.filled else colors.boardBackground))
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    StepTitle(
        "Reading the clues",
        "Each row and column has numbers. \"3 1\" means a run of 3 filled squares, then at least one empty square, then a run of 1 — in that order.",
    )
}

@Composable
private fun PracticeStep(practice: GameController) {
    val colors = LocalBoardColors.current
    val puzzle = practice.puzzle
    Text(
        if (practice.solved) "Solved! Nice work." else "Try it: tap squares to fill them",
        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
        color = if (practice.solved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(12.dp))
    BoardCanvas(
        puzzle = puzzle,
        cells = practice.cells,
        rowDone = (0 until puzzle.rows).map { practice.rowDone(it) },
        colDone = (0 until puzzle.cols).map { practice.colDone(it) },
        hintCell = practice.hintCell,
        mistakeCell = practice.mistakeCell,
        solved = practice.solved,
        colors = colors,
        onBegin = { practice.beginStroke(it) },
        onContinue = { i, brush, original -> practice.continueStroke(i, brush, original) },
        onEnd = { practice.endStroke() },
        onCancel = { practice.cancelStroke() },
        modifier = Modifier.size(260.dp).clip(RoundedCornerShape(12.dp)),
    )
    Spacer(Modifier.height(12.dp))
    Text(
        "Rows with 4 must be fully filled. A greyed-out clue means that line is done.",
        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ToolsStep() {
    StepTitle("Your tools", "Everything you need is at the bottom of the board.")
    Spacer(Modifier.height(20.dp))
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ToolLine(Icons.Default.Square, "Fill", "Tap or drag to fill squares.")
        ToolLine(Icons.Default.Close, "Cross", "Mark squares you know are empty.")
        ToolLine(Icons.AutoMirrored.Filled.Undo, "Undo", "Take back your last move.")
        ToolLine(Icons.Default.Lightbulb, "Hint", "Reveals one square you can work out from the board.")
        ToolLine(Icons.Default.ZoomIn, "Zoom", "Pinch with two fingers on big boards; double-tap to reset.")
    }
}

@Composable
private fun ToolLine(icon: ImageVector, name: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChallengeStep() {
    Box(
        Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF8B5CF6)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
    }
    Spacer(Modifier.height(20.dp))
    StepTitle(
        "Challenge Mode",
        "Take on random puzzles of any difficulty and win collectible cards. Solve faster with fewer mistakes to unlock rarer cards.",
    )
    Spacer(Modifier.height(20.dp))
    // Locked cards only: the deck stays a surprise.
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (n in listOf(39, 20, 1)) {
            CardFace(CardDeck.card(n), unmasked = false, modifier = Modifier.width(72.dp), showNumber = false)
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("${CardDeck.size} cards to collect", style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}
