package app.nonogram.puzzle.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.Puzzles
import app.nonogram.puzzle.data.Wallet
import app.nonogram.puzzle.logic.Rewards
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.model.Rarity

/**
 * Coin bookkeeping for one game. Pays each line once (remembered across undo, restart and
 * reopening for saved puzzles), totals the win reward, and pays nothing at all on a replay.
 */
class GameRewards(
    private val puzzle: Puzzle,
    private val wallet: Wallet,
    private val store: ProgressStore,
    /** Replaying an already-solved puzzle earns nothing. */
    val isReplay: Boolean,
    /** Challenge boards are never reopened, so their paid lines only live in memory. */
    private val persistLines: Boolean,
    /** Playing with "Check mistakes" off earns no coins at all. */
    val coinsOff: Boolean = false,
) {
    /** Mistake shield (watched an ad): mistakes don't cost coins. */
    var shielded = false
    private val paid: MutableSet<String> =
        if (persistLines) store.paidLines(puzzle.id).toMutableSet() else mutableSetOf()

    /** Latest floating message ("+2", "Level 4!"); [toastTick] changes every time one is posted. */
    var toast by mutableStateOf<String?>(null)
        private set
    var toastTick by mutableIntStateOf(0)
        private set
    /** Coins paid by the latest line reward, for the flying-coins animation. */
    var lastLineCoins = 0
        private set

    private fun post(message: String) { toast = message; toastTick++ }

    fun onLinesCorrect(lines: List<String>) {
        if (isReplay || coinsOff) return
        val fresh = lines.filter { it !in paid }
        if (fresh.isEmpty()) return
        paid += fresh
        if (persistLines) store.addPaidLines(puzzle.id, fresh)
        lastLineCoins = fresh.size * Rewards.LINE
        val levelUp = wallet.earn(fresh.size * Rewards.LINE)
        post("+${fresh.size * Rewards.LINE}" + (levelUp?.let { "  ·  Level ${it.level}!" } ?: ""))
    }

    /** Each caught mistake costs 1 coin, unless shielded (or the puzzle pays nothing anyway). */
    fun onMistake() {
        if (isReplay || coinsOff || shielded) return
        val taken = wallet.lose(Rewards.MISTAKE_PENALTY)
        lastLineCoins = 0
        if (taken > 0) post("−$taken")
    }

    /** What a win paid, for the win dialog. [doubleable] is what "Double coins" would add. */
    data class WinSummary(
        val items: List<Pair<String, Int>>,
        val levelUp: Wallet.LevelUp?,
        val doubleable: Int,
    ) {
        val total: Int get() = items.sumOf { it.second }
        /** Set once the win dialog has played its coin shower, so reopening it stays quiet. */
        var celebrated = false
        var doubleCelebrated = false
    }

    var doubled by mutableStateOf(false)
        private set

    /** Call after the puzzle is marked completed. Returns null on a replay. */
    fun onPuzzleSolved(mistakes: Int, checkMistakes: Boolean, isDaily: Boolean): WinSummary? {
        if (isReplay || coinsOff) return null
        val items = mutableListOf("Puzzle solved" to Rewards.solveCoins(puzzle.rows))
        if (checkMistakes && mistakes == 0) items += "No mistakes" to Rewards.NO_MISTAKE_BONUS
        if (isDaily) items += "Daily puzzle" to Rewards.DAILY_BONUS
        val doubleable = items.sumOf { it.second }

        val pack = Puzzles.packOf(puzzle.id)
        if (pack != null && !store.isPackBonusPaid(pack.id) && pack.puzzles.all { store.isCompleted(it.id) }) {
            store.markPackBonusPaid(pack.id)
            items += "${pack.title} pack complete" to Rewards.packBonus(pack.size)
        }
        val levelUp = wallet.earn(items.sumOf { it.second })
        return WinSummary(items, levelUp, doubleable)
    }

    fun onChallengeSolved(rarity: Rarity, newCard: Boolean): WinSummary {
        val items = mutableListOf("${rarity.label} card" to Rewards.challengeCoins(rarity))
        if (newCard) items += "New card" to Rewards.NEW_CARD_BONUS
        val levelUp = wallet.earn(items.sumOf { it.second })
        return WinSummary(items, levelUp, items.sumOf { it.second })
    }

    /** Credits the "Double coins" ad reward once. */
    fun applyDouble(summary: WinSummary) {
        if (doubled) return
        wallet.gift(summary.doubleable)
        doubled = true
    }
}
