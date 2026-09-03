package app.nonogram.puzzle.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.PuzzlePack
import app.nonogram.puzzle.data.Puzzles
import app.nonogram.puzzle.logic.PuzzleGenerator
import app.nonogram.puzzle.model.Puzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface Screen {
    data object Home : Screen
    data class Pack(val pack: PuzzlePack) : Screen
    data class Game(val puzzle: Puzzle, val title: String, val pack: PuzzlePack?) : Screen
    data class Generating(val id: String, val name: String, val size: Int, val seed: Long) : Screen
}

/** Tiny back-stack based navigation; there are only three screens so a nav library is not worth it. */
@Composable
fun NonogramApp() {
    val context = LocalContext.current
    val store = remember { ProgressStore(context) }
    val stack = remember { mutableStateListOf<Screen>(Screen.Home) }
    // Bumped whenever we return to a list screen so cached progress values re-read.
    var refreshKey by remember { mutableIntStateOf(0) }

    fun pop() {
        if (stack.size > 1) { stack.removeAt(stack.lastIndex); refreshKey++ }
    }
    fun push(s: Screen) { stack.add(s) }
    fun replace(s: Screen) { stack[stack.lastIndex] = s }

    BackHandler(enabled = stack.size > 1) { pop() }

    when (val screen = stack.last()) {
        Screen.Home -> HomeScreen(
            store = store,
            refreshKey = refreshKey,
            onOpenPack = { push(Screen.Pack(it)) },
            onPlayDaily = { day -> push(Screen.Generating(dailyPuzzleId(day), "Daily puzzle", 10, 7_919L * day + 12_345L)) },
            onPlayRandom = { size, seed -> push(Screen.Generating("random-$size-$seed", "Random ${size}×$size", size, seed)) },
        )
        is Screen.Pack -> LevelsScreen(
            pack = screen.pack,
            store = store,
            refreshKey = refreshKey,
            onBack = { pop() },
            onPlay = { puzzle ->
                val index = screen.pack.puzzles.indexOf(puzzle) + 1
                push(Screen.Game(puzzle, "${screen.pack.title} · $index", screen.pack))
            },
        )
        is Screen.Game -> {
            val next = screen.pack?.let { pack ->
                val i = pack.puzzles.indexOf(screen.puzzle)
                pack.puzzles.getOrNull(i + 1)
            }
            GameScreen(
                puzzle = screen.puzzle,
                title = screen.title,
                store = store,
                nextPuzzle = next,
                onBack = { pop() },
                onNext = { nextPuzzle ->
                    val pack = screen.pack!!
                    val index = pack.puzzles.indexOf(nextPuzzle) + 1
                    replace(Screen.Game(nextPuzzle, "${pack.title} · $index", pack))
                },
            )
        }
        is Screen.Generating -> {
            LaunchedEffect(screen) {
                val puzzle = withContext(Dispatchers.Default) {
                    PuzzleGenerator.generate(screen.id, screen.name, screen.size, screen.seed)
                }
                replace(Screen.Game(puzzle, screen.name, null))
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Building a solvable puzzle…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Convenience for previews/tests: first puzzle of the starter pack. */
internal fun sampleStarter(): Puzzle = Puzzles.pack5.puzzles.first()
