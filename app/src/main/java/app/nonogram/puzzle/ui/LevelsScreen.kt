package app.nonogram.puzzle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.PuzzlePack
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.ui.theme.LocalBoardColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsScreen(
    pack: PuzzlePack,
    store: ProgressStore,
    refreshKey: Int,
    onBack: () -> Unit,
    onPlay: (Puzzle) -> Unit,
) {
    val boardColors = LocalBoardColors.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${pack.title} · ${pack.size}×${pack.size}", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(pack.puzzles, key = { it.id }) { puzzle ->
                val index = pack.puzzles.indexOf(puzzle) + 1
                val done = remember(refreshKey, puzzle.id) { store.isCompleted(puzzle.id) }
                val inProgress = remember(refreshKey, puzzle.id) { !done && store.hasSavedBoard(puzzle.id) }
                val best = remember(refreshKey, puzzle.id) { store.bestTimeSeconds(puzzle.id) }
                Card(onClick = { onPlay(puzzle) }) {
                    Column(Modifier.padding(10.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (done) boardColors.boardBackground else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) {
                                PuzzleThumbnail(puzzle, boardColors.solvedFill, boardColors.boardBackground,
                                    Modifier.fillMaxSize().padding(6.dp))
                            } else {
                                Text(
                                    "$index",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (done) puzzle.name else "Puzzle $index",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            when {
                                done && best != null -> "Best ${formatTime(best)}"
                                inProgress -> "In progress"
                                else -> "Not started"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
