package app.nonogram.puzzle.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.nonogram.puzzle.data.Fact
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.ui.theme.LocalBoardColors

/**
 * The surprise card after solving a themed puzzle: photo, About, Fun fact and the Secret.
 * The Secret is revealed by a no-mistake solve, or by watching 2 ads ([onWatchAds]).
 */
@Composable
fun FactCardDialog(
    puzzle: Puzzle,
    fact: Fact,
    secretUnlocked: Boolean,
    justUnlocked: Boolean,
    adProgress: Pair<Int, Int>?,
    adMessage: String?,
    onWatchAds: () -> Unit,
    onContinue: () -> Unit,
) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
    val secretGlow = remember { Animatable(if (justUnlocked) 0f else 1f) }
    LaunchedEffect(secretUnlocked) {
        if (secretUnlocked && secretGlow.value < 1f) {
            CoinSounds.coin(6)
            secretGlow.animateTo(1f, tween(600))
        }
    }

    Dialog(
        onDismissRequest = { if (adProgress == null) onContinue() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Cap the height so the card (and its Continue button) never runs under the system bars.
        val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.86f).dp
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = maxHeight)
                .graphicsLayer {
                    alpha = appear.value.coerceIn(0f, 1f)
                    val s = 0.8f + 0.2f * appear.value
                    scaleX = s; scaleY = s
                },
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                FactContent(puzzle, fact, secretUnlocked, justUnlocked, adProgress, adMessage, onWatchAds, secretGlow.value)
                Button(
                    onClick = onContinue, enabled = adProgress == null,
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                ) { Text("Continue") }
            }
        }
    }
}

/** Photo, About, Fun fact and Secret. Shared by the post-solve dialog and the solved-puzzle page. */
@Composable
fun FactContent(
    puzzle: Puzzle,
    fact: Fact,
    secretUnlocked: Boolean,
    justUnlocked: Boolean,
    adProgress: Pair<Int, Int>?,
    adMessage: String?,
    onWatchAds: () -> Unit,
    secretAlpha: Float = 1f,
) {
    Column {
        if (fact.image != 0) {
            Image(
                painterResource(fact.image),
                contentDescription = puzzle.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(210.dp),
            )
        } else {
            val colors = LocalBoardColors.current
            Box(Modifier.fillMaxWidth().height(170.dp).background(colors.boardBackground), contentAlignment = Alignment.Center) {
                PuzzleThumbnail(puzzle, colors.solvedFill, colors.boardBackground, Modifier.size(130.dp))
            }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (fact.credit.isNotEmpty()) {
                Text("Photo: ${fact.credit}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column {
                Text("You discovered", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(puzzle.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Text(fact.about, style = MaterialTheme.typography.bodyMedium)

            FactSection("💡  Fun fact", MaterialTheme.colorScheme.secondaryContainer) {
                Text(fact.funFact, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            FactSection(if (secretUnlocked) "✨  Secret" else "🔒  Secret", CoinGold.copy(alpha = 0.18f)) {
                if (secretUnlocked) {
                    Column(Modifier.graphicsLayer { alpha = secretAlpha }) {
                        if (justUnlocked) {
                            Text("Unlocked!", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CoinGold)
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(fact.secret, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Solve this puzzle with no mistakes to reveal the secret.",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        FilledTonalButton(onClick = onWatchAds, enabled = adProgress == null, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (adProgress != null) "Playing ad ${adProgress.first + 1} of ${adProgress.second}…" else "Watch 2 ads to unlock")
                        }
                        Text("Or come back later and solve it again with no mistakes.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        adMessage?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun FactSection(title: String, background: androidx.compose.ui.graphics.Color, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(14.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

/**
 * Optional mistake shield, shown on the board. Tapping it plays an ad; after that, mistakes in
 * this puzzle stop costing coins. It pulses gently until the player makes their first move.
 */
@Composable
fun ShieldBar(
    active: Boolean,
    busy: Boolean,
    highlight: Boolean,
    message: String?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (active) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.width(6.dp))
                Text("Shield on: mistakes cost no coins", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        } else {
            val pulse = rememberInfiniteTransition(label = "shield")
            val glow by pulse.animateFloat(
                initialValue = 0.35f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "glow",
            )
            OutlinedButton(
                onClick = onTap,
                enabled = !busy,
                border = BorderStroke(if (highlight) 2.dp else 1.dp,
                    CoinGold.copy(alpha = if (highlight) glow else 0.6f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp),
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp), tint = CoinGold)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (busy) "Loading ad…" else "Tap to play without losing coins (ad)",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        message?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
