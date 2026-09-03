package app.nonogram.puzzle.logic

/**
 * Challenge Mode difficulties and the deterministic scoring scheme.
 *
 * score = base
 *       + secondsUnderPar * underRate
 *       - secondsOverPar * overRate
 *       - errors * errorPenalty
 *       - hints * hintPenalty
 *       + (errors == 0 ? nilBonus : 0)
 * floored at 0.
 */
enum class ChallengeDifficulty(
    val code: String,
    val label: String,
    val size: Int,
    val base: Int,
    val parSeconds: Int,
    val underRate: Double,
    val overRate: Double,
    val errorPenalty: Int,
    val nilBonus: Int,
    val hintPenalty: Int,
) {
    EASY("E", "Easy", 5, base = 100, parSeconds = 20, underRate = 3.0, overRate = 2.0, errorPenalty = 2, nilBonus = 20, hintPenalty = 15),
    HARD("D", "Difficult", 10, base = 150, parSeconds = 150, underRate = 1.0, overRate = 1.0, errorPenalty = 4, nilBonus = 40, hintPenalty = 25),
    EXPERT("X", "Very Difficult", 15, base = 200, parSeconds = 420, underRate = 0.5, overRate = 0.5, errorPenalty = 5, nilBonus = 60, hintPenalty = 40);

    companion object {
        /** Deterministic random pick for a given seed, so a round is reproducible. */
        fun random(seed: Long): ChallengeDifficulty {
            val idx = ((seed xor (seed ushr 17)) and Long.MAX_VALUE) % entries.size
            return entries[idx.toInt()]
        }
    }
}

object ChallengeScoring {

    fun score(
        difficulty: ChallengeDifficulty,
        seconds: Long,
        mistakes: Int,
        hints: Int,
    ): Int {
        var s = difficulty.base.toDouble()
        val diff = difficulty.parSeconds - seconds
        s += if (diff >= 0) diff * difficulty.underRate else diff * difficulty.overRate
        s -= mistakes * difficulty.errorPenalty
        s -= hints * difficulty.hintPenalty
        if (mistakes == 0) s += difficulty.nilBonus
        return s.toInt().coerceAtLeast(0)
    }

    /**
     * How many rewarded ads must be watched to unlock the next hint.
     * Grace: the player's first [GRACE_GAMES] Challenge games use a flat 1 ad per hint.
     * After that, Easy stays at 1 ad; harder boards charge one more ad per hint used
     * (1st hint = 1 ad, 2nd = 2 ads, 3rd = 3 ads ...).
     */
    fun hintAdCost(difficulty: ChallengeDifficulty, hintNumber: Int, gamesPlayed: Int): Int {
        if (gamesPlayed < GRACE_GAMES) return 1
        if (difficulty == ChallengeDifficulty.EASY) return 1
        return hintNumber.coerceAtLeast(1)
    }

    const val GRACE_GAMES = 3
}
