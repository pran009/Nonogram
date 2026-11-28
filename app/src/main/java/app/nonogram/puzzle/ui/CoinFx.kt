package app.nonogram.puzzle.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.R
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Coin sound effects (synthesized for this game, see res/raw). */
object CoinSounds {
    private var pool: SoundPool? = null
    private var coinId = 0
    private var showerId = 0
    var enabled = true

    fun init(context: Context, enabled: Boolean) {
        this.enabled = enabled
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        pool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build().also {
            coinId = it.load(context.applicationContext, R.raw.coin, 1)
            showerId = it.load(context.applicationContext, R.raw.coins, 1)
        }
    }

    /** One coin landing. [index] raises the pitch a little so a run of coins rises like a cascade. */
    fun coin(index: Int = 0) {
        if (enabled) pool?.play(coinId, 0.55f, 0.55f, 1, 0, (1f + index * 0.05f).coerceAtMost(1.5f))
    }

    /** A shower of coins, for wins and rewards. */
    fun shower() {
        if (enabled) pool?.play(showerId, 0.8f, 0.8f, 2, 0, 1f)
    }
}

/** Coins flying from the board into the balance badge. */
class CoinFlights {
    internal var target by mutableStateOf<Offset?>(null)
    internal val active = mutableStateListOf<Flight>()
    private var nextId = 0L

    internal data class Flight(val id: Long, val from: Offset, val delayMs: Long, val index: Int)

    /** Sends up to 8 coins (one per coin earned) from [from], in root coordinates. */
    fun launch(from: Offset, amount: Int) {
        repeat(amount.coerceIn(1, 8)) { i -> active += Flight(nextId++, from, i * 90L, i) }
    }

    companion object {
        /** How long after [launch] the first coin lands; the badge waits this long before counting up. */
        const val ARRIVAL_MS = 650
    }
}

@Composable
fun rememberCoinFlights() = remember { CoinFlights() }

/** Marks the badge that flying coins should land on. */
fun Modifier.coinFlightTarget(flights: CoinFlights) =
    onGloballyPositioned { flights.target = it.boundsInRoot().center }

/** Full-screen host: draws flying coins on top of [content] (including its top bar). */
@Composable
fun CoinFlightHost(flights: CoinFlights, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        content()
        val target = flights.target
        if (target != null) {
            for (f in flights.active) key(f.id) {
                FlyingCoin(f, target) { flights.active.remove(f) }
            }
        }
    }
}

@Composable
private fun FlyingCoin(f: CoinFlights.Flight, target: Offset, onDone: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val jitter = remember { Offset(Random.nextFloat() * 140f - 70f, Random.nextFloat() * 50f - 25f) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(f.delayMs)
        visible = true
        progress.animateTo(1f, tween(CoinFlights.ARRIVAL_MS, easing = FastOutSlowInEasing))
        CoinSounds.coin(f.index)
        onDone()
    }
    if (!visible) return
    val sizeDp = 26.dp
    val half = with(LocalDensity.current) { sizeDp.toPx() / 2f }
    CoinIcon(
        Modifier
            .size(sizeDp)
            .graphicsLayer {
                val p = progress.value
                val q = 1f - p
                val start = f.from + jitter
                // Pops up first, then swoops across to the badge.
                val ctrl = Offset(start.x, target.y + (start.y - target.y) * 0.15f)
                val x = q * q * start.x + 2 * q * p * ctrl.x + p * p * target.x
                val y = q * q * start.y + 2 * q * p * ctrl.y + p * p * target.y
                translationX = x - half
                translationY = y - half
                val s = if (p < 0.15f) 0.6f + p / 0.15f * 0.7f else 1.3f - 0.65f * (p - 0.15f) / 0.85f
                scaleY = s
                scaleX = s * abs(cos(p * 3f * PI.toFloat())).coerceAtLeast(0.2f) // spinning coin
            },
    )
}

/** A burst of coins that jump up and fall back down, each time [trigger] changes (0 = never). */
@Composable
fun CoinBurst(trigger: Int, modifier: Modifier = Modifier, count: Int = 18) {
    if (trigger == 0) return
    val progress = remember(trigger) { Animatable(0f) }
    val parts = remember(trigger) {
        List(count) {
            Particle(
                angle = (-160 + Random.nextFloat() * 140f) * PI.toFloat() / 180f,
                speed = 0.55f + Random.nextFloat() * 0.5f,
                spin = 6f + Random.nextFloat() * 10f,
                scale = 0.75f + Random.nextFloat() * 0.5f,
            )
        }
    }
    LaunchedEffect(trigger) { progress.animateTo(1f, tween(1400, easing = LinearEasing)) }
    val rim = Color(0xFFB9761A)
    val shine = Color(0xFFFFE3A3)
    Canvas(modifier) {
        val p = progress.value
        if (p >= 1f) return@Canvas
        val t = p * 1.4f
        val origin = Offset(size.width / 2f, size.height * 0.8f)
        val v = size.height * 2.2f
        val g = size.height * 4.5f
        val r0 = 9.dp.toPx()
        val alpha = 1f - p * p * p
        for (pt in parts) {
            val x = origin.x + cos(pt.angle) * v * pt.speed * t * 1.3f
            val y = origin.y + sin(pt.angle) * v * pt.speed * t + 0.5f * g * t * t
            val h = r0 * pt.scale
            val w = h * abs(cos(t * pt.spin)).coerceAtLeast(0.15f)
            drawOval(CoinGold, Offset(x - w, y - h), Size(2 * w, 2 * h), alpha = alpha)
            drawOval(rim, Offset(x - w, y - h), Size(2 * w, 2 * h), alpha = alpha, style = Stroke(1.5.dp.toPx()))
            drawOval(shine, Offset(x - w * 0.45f, y - h * 0.6f), Size(w * 0.5f, h * 0.5f), alpha = alpha * 0.8f)
        }
    }
}

private class Particle(val angle: Float, val speed: Float, val spin: Float, val scale: Float)
