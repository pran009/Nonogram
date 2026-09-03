package app.nonogram.puzzle.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sin

private data class Star(val x: Float, val y: Float, val size: Float, val phase: Float)

/** A vivid, animated Challenge entry: shifting gradient with twinkling star sparkles. */
@Composable
fun ChallengeTile(
    collected: Int,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "challenge")
    val shift by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000), RepeatMode.Reverse), label = "shift",
    )
    val twinkle by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Restart), label = "twinkle",
    )

    val stars = remember {
        listOf(
            Star(0.12f, 0.30f, 3.5f, 0f), Star(0.22f, 0.68f, 2.5f, 1.1f),
            Star(0.40f, 0.20f, 3f, 2.2f), Star(0.55f, 0.75f, 2f, 0.6f),
            Star(0.70f, 0.35f, 3.5f, 1.7f), Star(0.83f, 0.60f, 2.5f, 3.0f),
            Star(0.90f, 0.25f, 3f, 2.5f), Star(0.33f, 0.50f, 2f, 4.0f),
            Star(0.63f, 0.15f, 2.5f, 0.3f), Star(0.48f, 0.90f, 2f, 1.9f),
        )
    }

    val c1 = Color(0xFF6D28D9); val c2 = Color(0xFFDB2777); val c3 = Color(0xFFF59E0B)
    val start = lerp3(c1, c2, c3, shift)
    val end = lerp3(c3, c1, c2, shift)

    Box(
        modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.linearGradient(listOf(start, end)))
            for (s in stars) {
                val a = (0.35f + 0.65f * ((sin(twinkle + s.phase) + 1f) / 2f))
                val cx = s.x * size.width
                val cy = s.y * size.height
                val r = s.size
                rotate(degrees = 45f, pivot = Offset(cx, cy)) {
                    drawRect(Color.White.copy(alpha = a), topLeft = Offset(cx - r, cy - r * 0.35f), size = androidx.compose.ui.geometry.Size(r * 2, r * 0.7f))
                    drawRect(Color.White.copy(alpha = a), topLeft = Offset(cx - r * 0.35f, cy - r), size = androidx.compose.ui.geometry.Size(r * 0.7f, r * 2))
                }
            }
        }
        Row(
            Modifier.fillMaxSize().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).padding(0.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Challenge", style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text("Random puzzles · win collectible cards", style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f))
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$collected", style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text("of $total", style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

private fun lerp(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f,
)

private fun lerp3(a: Color, b: Color, c: Color, t: Float): Color =
    if (t < 0.5f) lerp(a, b, t * 2f) else lerp(b, c, (t - 0.5f) * 2f)
