package app.nonogram.puzzle.logic

import app.nonogram.puzzle.model.CardDeck
import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeScoringTest {

    @Test fun easyExamplesMatchTheAgreedScheme() {
        val e = ChallengeDifficulty.EASY
        assertEquals(156, ChallengeScoring.score(e, seconds = 8, mistakes = 0))   // 100 + 12*3 + 20
        assertEquals(120, ChallengeScoring.score(e, seconds = 20, mistakes = 0))  // 100 + 0 + 20
        assertEquals(64, ChallengeScoring.score(e, seconds = 35, mistakes = 3))   // 100 - 15*2 - 3*2
    }

    @Test fun scoreNeverGoesBelowZero() {
        assertEquals(0, ChallengeScoring.score(ChallengeDifficulty.EXPERT, seconds = 5000, mistakes = 50))
    }

    @Test fun cardOneNeedsAFlawlessFastExpertSolve() {
        val x = ChallengeDifficulty.EXPERT
        assertEquals(1, CardDeck.cardNumberForScore(ChallengeScoring.score(x, seconds = 140, mistakes = 0)))
        assertEquals(2, CardDeck.cardNumberForScore(ChallengeScoring.score(x, seconds = 141, mistakes = 0)))
    }

    @Test fun cardBandsCoverTheWholeRange() {
        assertEquals(39, CardDeck.cardNumberForScore(0))
        assertEquals(39, CardDeck.cardNumberForScore(9))
        assertEquals(38, CardDeck.cardNumberForScore(10))
        assertEquals(24, CardDeck.cardNumberForScore(150))
        assertEquals(13, CardDeck.cardNumberForScore(269))
        assertEquals(4, CardDeck.cardNumberForScore(359))
        assertEquals(3, CardDeck.cardNumberForScore(360))
        assertEquals(1, CardDeck.cardNumberForScore(400))
    }
}
