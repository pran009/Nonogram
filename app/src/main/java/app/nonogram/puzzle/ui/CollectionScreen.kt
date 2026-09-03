package app.nonogram.puzzle.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.model.CardDeck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    store: ProgressStore,
    refreshKey: Int,
    onBack: () -> Unit,
) {
    val unmasked = remember(refreshKey) { store.unmaskedCards() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collection", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text("${unmasked.size} of ${CardDeck.size} collected",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Win Challenge games to reveal cards. Better scores unlock rarer cards.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { unmasked.size.toFloat() / CardDeck.size },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(CardDeck.cards, key = { it.number }) { card ->
                    CardFace(card, unmasked = card.number in unmasked)
                }
            }
        }
    }
}
