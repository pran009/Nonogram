package app.nonogram.puzzle.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.ads.AdManager
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.PuzzlePack
import app.nonogram.puzzle.data.Wallet
import app.nonogram.puzzle.logic.FreeCoinLadder
import app.nonogram.puzzle.logic.LadderStatus
import app.nonogram.puzzle.logic.Rewards
import kotlinx.coroutines.delay

val CoinGold = Color(0xFFF2A93B)

@Composable
fun CoinIcon(modifier: Modifier = Modifier) {
    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = CoinGold, modifier = modifier)
}

/** Pill showing the coin balance. */
@Composable
fun CoinBadge(coins: Int, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinIcon(Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text("%,d".format(coins), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

/** Floating message ("+1", "Level 4!") that rises and fades out each time [tick] changes. */
@Composable
fun CoinToast(message: String?, tick: Int, modifier: Modifier = Modifier) {
    val alpha = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    LaunchedEffect(tick) {
        if (tick == 0 || message == null) return@LaunchedEffect
        alpha.snapTo(1f); rise.snapTo(0f)
        rise.animateTo(-40f, tween(1100))
        alpha.animateTo(0f, tween(300))
    }
    if (message != null) {
        Row(
            modifier
                .graphicsLayer { this.alpha = alpha.value; translationY = rise.value }
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoinIcon(Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(message, color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Hint purchase: spend coins (if chosen), then watch the ads. If the player closes an ad early,
 * the coins are refunded and no hint is given. [onDone] gets true when the hint was paid for.
 */
fun buyHint(
    activity: Activity,
    adManager: AdManager,
    wallet: Wallet,
    coins: Int,
    ads: Int,
    onProgress: (Int, Int) -> Unit,
    onDone: (Boolean) -> Unit,
) {
    if (coins > 0 && !wallet.spend(coins)) { onDone(false); return }
    adManager.showRewardedAds(activity, ads, onProgress) { ok ->
        if (!ok && coins > 0) wallet.gift(coins)
        onDone(ok)
    }
}

@Composable
fun HintDialog(
    price: Rewards.HintPrice,
    coins: Int,
    progress: Pair<Int, Int>?,
    clockPausedNote: Boolean,
    onCoinsAndAds: () -> Unit,
    onAdsOnly: () -> Unit,
    onDismiss: () -> Unit,
) {
    fun ads(n: Int) = if (n == 1) "1 ad" else "$n ads"
    AlertDialog(
        onDismissRequest = { if (progress == null) onDismiss() },
        title = { Text("Unlock a hint") },
        text = {
            if (progress != null) {
                Text("Playing ad ${progress.first + 1} of ${progress.second}…")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("A hint reveals one square you can work out from the board.")
                    Button(onClick = onCoinsAndAds, enabled = coins >= price.coins, modifier = Modifier.fillMaxWidth()) {
                        CoinIcon(Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${price.coins} coins + ${ads(price.adsWithCoins)}")
                    }
                    if (coins < price.coins) {
                        Text("You have $coins coins.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = onAdsOnly, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Watch ${ads(price.adsOnly)}")
                    }
                    if (clockPausedNote) {
                        Text("The clock is paused while you watch.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { if (progress == null) TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Coins section of a win dialog, including the "double your coins" offer. */
@Composable
fun WinCoinsSection(
    summary: GameRewards.WinSummary?,
    isReplay: Boolean,
    doubled: Boolean,
    doubleBusy: Boolean,
    doubleMessage: String?,
    onDouble: () -> Unit,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        if (isReplay || summary == null) {
            Text("Replay: no coins for solving a puzzle again.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        for ((label, amount) in summary.items) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("+$amount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                CoinIcon(Modifier.size(16.dp))
            }
        }
        summary.levelUp?.let {
            Spacer(Modifier.height(6.dp))
            Text("Level up! Level ${it.level} · +${it.bonus} coins", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))
        if (doubled) {
            Text("Doubled! +${summary.doubleable} more coins", fontWeight = FontWeight.Bold, color = CoinGold)
        } else {
            FilledTonalButton(onClick = onDouble, enabled = !doubleBusy, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (doubleBusy) "Loading ad…" else "Watch ad: +${summary.doubleable} more")
            }
        }
        doubleMessage?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The free-coins ad ladder: 10, 20, 25, 30, 40 coins; two ladders a day, one hour apart. */
@Composable
fun FreeCoinsDialog(
    store: ProgressStore,
    wallet: Wallet,
    adManager: AdManager,
    activity: Activity?,
    onDismiss: () -> Unit,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var ladder by remember { mutableStateOf(store.ladder) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }
    val today = todayEpochDay()
    val status = FreeCoinLadder.status(ladder, today, now)
    val currentStep = (status as? LadderStatus.Available)?.step ?: if (status is LadderStatus.DoneForToday) 5 else 0

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Free coins") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Each ad pays more than the last.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Rewards.LADDER.forEachIndexed { i, amount ->
                        val done = i < currentStep
                        val next = i == currentStep && status is LadderStatus.Available
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (done) MaterialTheme.colorScheme.surfaceVariant else CoinGold.copy(alpha = if (next) 1f else 0.35f))
                                .border(if (next) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(if (done) "✓" else "$amount", fontWeight = FontWeight.Bold,
                                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF3B2600))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    when (status) {
                        is LadderStatus.Available -> "Round ${status.ladder + 1} of ${Rewards.LADDERS_PER_DAY} today"
                        is LadderStatus.CoolingDown -> "Next round unlocks in ${formatTime(status.remainingMs / 1000)}"
                        LadderStatus.DoneForToday -> "That's all for today. Come back tomorrow!"
                    },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                message?.let { Spacer(Modifier.height(6.dp)); Text(it, fontWeight = FontWeight.SemiBold) }
            }
        },
        confirmButton = {
            if (status is LadderStatus.Available) {
                Button(enabled = !busy && activity != null, onClick = {
                    busy = true; message = null
                    adManager.showFreeCoinsAd(activity!!) { ok ->
                        busy = false
                        if (ok) {
                            val result = FreeCoinLadder.onAdWatched(store.ladder, todayEpochDay(), System.currentTimeMillis())
                            if (result != null) {
                                wallet.gift(result.first)
                                store.ladder = result.second
                                ladder = result.second
                                message = "+${result.first} coins!"
                            }
                        } else {
                            message = "No ad available right now. Please try again later."
                        }
                    }
                }) { Text(if (busy) "Loading ad…" else "Watch ad: +${status.reward}") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Close") } },
    )
}

@Composable
fun UnlockPackDialog(pack: PuzzlePack, coins: Int, onUnlock: () -> Unit, onGetCoins: () -> Unit, onDismiss: () -> Unit) {
    val enough = coins >= pack.price
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unlock ${pack.title}?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${pack.puzzles.size} new picture puzzles · ${pack.size}×${pack.size}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Price: ${pack.price}")
                    Spacer(Modifier.width(4.dp)); CoinIcon(Modifier.size(16.dp))
                    Text("   You have: $coins")
                }
                if (!enough) {
                    Text("You need ${pack.price - coins} more coins. Earn them by solving puzzles, or watch ads for free coins.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            if (enough) Button(onClick = onUnlock) { Text("Unlock") }
            else Button(onClick = onGetCoins) { Text("Get free coins") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
