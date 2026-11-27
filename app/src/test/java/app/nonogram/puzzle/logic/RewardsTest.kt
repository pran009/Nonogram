package app.nonogram.puzzle.logic

import app.nonogram.puzzle.data.Puzzles
import app.nonogram.puzzle.model.Rarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardsTest {

    @Test fun solveAndPackCoinsBySize() {
        assertEquals(10, Rewards.solveCoins(5))
        assertEquals(25, Rewards.solveCoins(10))
        assertEquals(50, Rewards.solveCoins(15))
        assertEquals(100, Rewards.packBonus(5))
        assertEquals(250, Rewards.packBonus(10))
        assertEquals(500, Rewards.packBonus(15))
    }

    @Test fun challengeCoinsByRarity() {
        assertEquals(10, Rewards.challengeCoins(Rarity.COMMON))
        assertEquals(25, Rewards.challengeCoins(Rarity.RARE))
        assertEquals(50, Rewards.challengeCoins(Rarity.EPIC))
        assertEquals(100, Rewards.challengeCoins(Rarity.LEGENDARY))
    }

    @Test fun levelCurve() {
        assertEquals(1, Rewards.levelFor(0))
        assertEquals(1, Rewards.levelFor(99))
        assertEquals(2, Rewards.levelFor(100))
        assertEquals(3, Rewards.levelFor(300))
        assertEquals(4, Rewards.levelFor(600))
        assertEquals(5, Rewards.levelFor(1000))
        assertEquals(80, Rewards.levelUpBonus(4))
    }

    @Test fun hintPrices() {
        // Normal play: 50 coins + 1 ad, or 2 ads.
        assertEquals(Rewards.HintPrice(50, 1, 2), Rewards.hintPrice(1))
        // Challenge 3rd hint after grace: 50 coins + 3 ads, or 4 ads.
        assertEquals(Rewards.HintPrice(50, 3, 4), Rewards.hintPrice(3))
    }

    @Test fun ladderPaysEscalatingAmountsThenCoolsDown() {
        val day = 100L
        var now = 1_000_000L
        var s = LadderState()
        val paid = mutableListOf<Int>()
        repeat(5) {
            val (coins, next) = FreeCoinLadder.onAdWatched(s, day, now)!!
            paid += coins; s = next
        }
        assertEquals(listOf(10, 20, 25, 30, 40), paid)
        // Ladder 2 locked for an hour.
        assertTrue(FreeCoinLadder.status(s, day, now) is LadderStatus.CoolingDown)
        assertNull(FreeCoinLadder.onAdWatched(s, day, now))
        now += Rewards.LADDER_COOLDOWN_MS
        assertEquals(LadderStatus.Available(10, 0, 1), FreeCoinLadder.status(s, day, now))
        repeat(5) { s = FreeCoinLadder.onAdWatched(s, day, now)!!.second }
        // Two ladders done: nothing more today, even much later.
        assertEquals(LadderStatus.DoneForToday, FreeCoinLadder.status(s, day, now + 10 * 3_600_000L))
        assertNull(FreeCoinLadder.onAdWatched(s, day, now))
        // Next day: fresh ladder.
        assertEquals(LadderStatus.Available(10, 0, 0), FreeCoinLadder.status(s, day + 1, now))
    }

    @Test fun ladderResumesMidwayAfterPartialWatching() {
        var s = LadderState()
        s = FreeCoinLadder.onAdWatched(s, 7, 0)!!.second
        s = FreeCoinLadder.onAdWatched(s, 7, 0)!!.second
        assertEquals(LadderStatus.Available(25, 2, 0), FreeCoinLadder.status(s, 7, 5_000))
    }

    @Test fun lockedPacksHaveTenPuzzlesAndPrices() {
        val paid = Puzzles.packs.filter { it.price > 0 }
        assertEquals(listOf(300, 400, 700), paid.map { it.price })
        paid.forEach { assertEquals(10, it.puzzles.size) }
        assertEquals("animals", Puzzles.packOf("a03")?.id)
    }
}
