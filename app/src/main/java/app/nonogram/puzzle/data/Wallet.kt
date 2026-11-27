package app.nonogram.puzzle.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import app.nonogram.puzzle.logic.Rewards

/**
 * The player's coins and XP, observable by Compose so every counter updates instantly.
 * One instance for the whole app (created in MainActivity), backed by [ProgressStore].
 */
class Wallet(private val store: ProgressStore) {

    var coins by mutableIntStateOf(store.coins)
        private set
    var xp by mutableIntStateOf(store.xp)
        private set

    val level: Int get() = Rewards.levelFor(xp)

    /** Progress (0..1) from the current level to the next. */
    val levelProgress: Float
        get() {
            val lo = Rewards.xpForLevel(level)
            val hi = Rewards.xpForLevel(level + 1)
            return ((xp - lo).toFloat() / (hi - lo)).coerceIn(0f, 1f)
        }

    /** A level reached by [earn], with the bonus coins already credited. */
    data class LevelUp(val level: Int, val bonus: Int)

    /**
     * Credits coins earned by playing. Gameplay coins also count as XP; crossing one or more
     * level thresholds pays the level-up bonus on top. Returns the highest level reached, if any.
     */
    fun earn(amount: Int): LevelUp? {
        if (amount <= 0) return null
        val before = level
        xp += amount
        store.xp = xp
        var bonus = 0
        for (l in (before + 1)..level) bonus += Rewards.levelUpBonus(l)
        coins += amount + bonus
        store.coins = coins
        return if (level > before) LevelUp(level, bonus) else null
    }

    /** Credits coins that don't count as XP (ads, doubling). */
    fun gift(amount: Int) {
        if (amount <= 0) return
        coins += amount
        store.coins = coins
    }

    /** Spends coins if the balance allows. Returns false (and changes nothing) otherwise. */
    fun spend(amount: Int): Boolean {
        if (amount < 0 || coins < amount) return false
        coins -= amount
        store.coins = coins
        return true
    }
}
