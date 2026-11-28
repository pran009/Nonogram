package app.nonogram.puzzle.logic

import app.nonogram.puzzle.model.Rarity

/**
 * Every coin and XP number in the game lives here, so the economy can be tuned in one place.
 * Pure functions only — no Android, so it is fully unit-tested.
 */
object Rewards {

    /** Coins for each correct row or column, paid once per line per puzzle. */
    const val LINE = 1

    /** Coins for solving a puzzle, by board size. */
    fun solveCoins(size: Int): Int = when {
        size <= 5 -> 10
        size <= 10 -> 25
        else -> 50
    }

    /** Extra coins for a solve with zero mistakes (only when mistake checking is on). */
    const val MISTAKE_PENALTY = 1
    const val NO_MISTAKE_BONUS = 10

    /** Extra coins for solving the daily puzzle (once per day). */
    const val DAILY_BONUS = 30

    /** Coins for winning a Challenge, by the rarity of the card earned. */
    fun challengeCoins(rarity: Rarity): Int = when (rarity) {
        Rarity.COMMON -> 10
        Rarity.RARE -> 25
        Rarity.EPIC -> 50
        Rarity.LEGENDARY -> 100
    }

    /** Extra coins when a Challenge reveals a card for the first time. */
    const val NEW_CARD_BONUS = 20

    /** One-time bonus for solving every puzzle in a pack, by board size. */
    fun packBonus(size: Int): Int = when {
        size <= 5 -> 100
        size <= 10 -> 250
        else -> 500
    }

    // ---- Player levels --------------------------------------------------------------------

    /** Total XP needed to reach [level]. Level 1 = 0, 2 = 100, 3 = 300, 4 = 600, 5 = 1000 ... */
    fun xpForLevel(level: Int): Int = 50 * level * (level - 1)

    fun levelFor(xp: Int): Int {
        var level = 1
        while (xpForLevel(level + 1) <= xp) level++
        return level
    }

    /** Coins awarded on reaching [level]. */
    fun levelUpBonus(level: Int): Int = level * 20

    // ---- Hints ----------------------------------------------------------------------------

    /** Coin part of the "coins + ad" hint option. */
    const val HINT_COINS = 50

    /**
     * The two ways to unlock a hint, given the base ad count [baseAds]
     * (1 in normal play; Challenge raises it per hint after the grace games).
     *  - Option 1: [HINT_COINS] coins + [baseAds] ads
     *  - Option 2: [baseAds] + 1 ads, no coins
     */
    data class HintPrice(val coins: Int, val adsWithCoins: Int, val adsOnly: Int)

    fun hintPrice(baseAds: Int): HintPrice {
        val ads = baseAds.coerceAtLeast(1)
        return HintPrice(coins = HINT_COINS, adsWithCoins = ads, adsOnly = ads + 1)
    }

    // ---- Free-coins ad ladder -------------------------------------------------------------

    /** Coins for each ad in one ladder. */
    val LADDER = listOf(10, 20, 25, 30, 40)

    /** Ladders per day. */
    const val LADDERS_PER_DAY = 2

    /** Wait after finishing a ladder before the next one unlocks. */
    const val LADDER_COOLDOWN_MS = 60 * 60 * 1000L
}

/** Persisted state of the free-coins ladder. Immutable; every change returns a new copy. */
data class LadderState(
    val day: Long = 0,           // local epoch day this state belongs to
    val laddersDone: Int = 0,    // completed ladders today
    val step: Int = 0,           // index into Rewards.LADDER for the next ad
    val unlockAtMs: Long = 0,    // next ladder is locked until this time
)

sealed interface LadderStatus {
    /** The next ad pays [reward] coins; it is step [step] (0-based) of ladder [ladder] (0-based). */
    data class Available(val reward: Int, val step: Int, val ladder: Int) : LadderStatus
    data class CoolingDown(val remainingMs: Long) : LadderStatus
    data object DoneForToday : LadderStatus
}

object FreeCoinLadder {

    /** Resets the state when the local day has changed. */
    fun forDay(state: LadderState, today: Long): LadderState =
        if (state.day == today) state else LadderState(day = today)

    fun status(state: LadderState, today: Long, nowMs: Long): LadderStatus {
        val s = forDay(state, today)
        return when {
            s.laddersDone >= Rewards.LADDERS_PER_DAY -> LadderStatus.DoneForToday
            nowMs < s.unlockAtMs -> LadderStatus.CoolingDown(s.unlockAtMs - nowMs)
            else -> LadderStatus.Available(Rewards.LADDER[s.step], s.step, s.laddersDone)
        }
    }

    /**
     * Records one fully watched ad. Returns the coins earned and the new state, or null if no
     * ad should have been offered right now (so callers can never pay out by mistake).
     */
    fun onAdWatched(state: LadderState, today: Long, nowMs: Long): Pair<Int, LadderState>? {
        val s = forDay(state, today)
        val st = status(s, today, nowMs) as? LadderStatus.Available ?: return null
        val nextStep = s.step + 1
        val next = if (nextStep >= Rewards.LADDER.size) {
            val done = s.laddersDone + 1
            s.copy(laddersDone = done, step = 0,
                unlockAtMs = if (done < Rewards.LADDERS_PER_DAY) nowMs + Rewards.LADDER_COOLDOWN_MS else 0)
        } else {
            s.copy(step = nextStep)
        }
        return st.reward to next
    }
}
