package app.nonogram.puzzle.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nonogram.puzzle.model.Card
import app.nonogram.puzzle.model.CardDeck
import app.nonogram.puzzle.model.Rarity

private fun rarityBrush(rarity: Rarity): Brush = Brush.linearGradient(
    listOf(rarity.color.copy(alpha = 0.9f), rarity.color.copy(alpha = 0.45f))
)

/** A single collectible card. When [unmasked] is false it renders a locked silhouette. */
@Composable
fun CardFace(
    card: Card,
    unmasked: Boolean,
    modifier: Modifier = Modifier,
    showNumber: Boolean = true,
) {
    val surface = MaterialTheme.colorScheme.surface
    Box(
        modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (unmasked) surface else MaterialTheme.colorScheme.surfaceVariant)
            .border(2.5.dp, if (unmasked) rarityBrush(card.rarity) else Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)
            ), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (unmasked) {
            // Rarity wash behind the emoji.
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(card.rarity.glow, Color.Transparent, card.rarity.glow)
                        )
                    )
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (showNumber) {
                    Text("#${card.number}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
                Text(card.emoji, fontSize = 40.sp)
                Text(card.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp))
                Text(card.rarity.label.uppercase(), style = MaterialTheme.typography.labelSmall,
                    color = card.rarity.color, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("?", fontSize = 34.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                if (showNumber) {
                    Text("#${card.number}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}

/** Animated reveal: the card scales/fades in. Used in the win dialog. */
@Composable
fun CardReveal(card: Card, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(card.number) {
        alpha.animateTo(1f, tween(250))
    }
    LaunchedEffect(card.number) {
        scale.animateTo(1f, tween(450))
    }
    Box(
        modifier.graphicsLayer {
            this.alpha = alpha.value
            scaleX = scale.value
            scaleY = scale.value
        },
        contentAlignment = Alignment.Center,
    ) {
        CardFace(card, unmasked = true, modifier = Modifier.size(150.dp))
    }
}

@Composable
fun rememberCardForScore(score: Int): Card = remember(score) { CardDeck.cardForScore(score) }
