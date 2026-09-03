package app.nonogram.puzzle.data

import android.content.Context
import android.content.SharedPreferences
import app.nonogram.puzzle.model.CellState

/** Small SharedPreferences wrapper for progress, settings, and in-progress boards. */
class ProgressStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("nonogram_progress", Context.MODE_PRIVATE)

    // ---- Settings -------------------------------------------------------------------------

    var checkMistakes: Boolean
        get() = prefs.getBoolean(KEY_CHECK_MISTAKES, true)
        set(value) = prefs.edit().putBoolean(KEY_CHECK_MISTAKES, value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    /** Set true by the future "Remove ads" purchase. When true, no ads are ever shown and
     *  hints are always free. */
    var adsRemoved: Boolean
        get() = prefs.getBoolean(KEY_ADS_REMOVED, false)
        set(value) = prefs.edit().putBoolean(KEY_ADS_REMOVED, value).apply()

    /** Hints the player can spend without watching an ad. New players get a few for free. */
    var hintCredits: Int
        get() = prefs.getInt(KEY_HINT_CREDITS, FREE_STARTER_HINTS)
        set(value) = prefs.edit().putInt(KEY_HINT_CREDITS, value.coerceAtLeast(0)).apply()

    // ---- Completion -----------------------------------------------------------------------

    fun isCompleted(puzzleId: String): Boolean = prefs.getBoolean("done:$puzzleId", false)

    fun bestTimeSeconds(puzzleId: String): Long? =
        prefs.getLong("best:$puzzleId", -1L).takeIf { it >= 0 }

    fun markCompleted(puzzleId: String, seconds: Long) {
        val best = bestTimeSeconds(puzzleId)
        prefs.edit()
            .putBoolean("done:$puzzleId", true)
            .putLong("best:$puzzleId", if (best == null || seconds < best) seconds else best)
            .remove("state:$puzzleId")
            .remove("time:$puzzleId")
            .apply()
    }

    fun completedCount(ids: Collection<String>): Int = ids.count { isCompleted(it) }

    var randomSolvedCount: Int
        get() = prefs.getInt(KEY_RANDOM_SOLVED, 0)
        set(value) = prefs.edit().putInt(KEY_RANDOM_SOLVED, value).apply()

    // ---- Challenge Mode / card collection -------------------------------------------------

    /** Card numbers (1..39) the player has unmasked. */
    fun unmaskedCards(): Set<Int> =
        prefs.getStringSet(KEY_CARDS, emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()

    fun isCardUnmasked(number: Int): Boolean = number.toString() in prefs.getStringSet(KEY_CARDS, emptySet())!!

    /** Records a card as unmasked. Returns true if it was newly unmasked. */
    fun unmaskCard(number: Int): Boolean {
        val current = prefs.getStringSet(KEY_CARDS, emptySet())!!.toMutableSet()
        val added = current.add(number.toString())
        if (added) prefs.edit().putStringSet(KEY_CARDS, current).apply()
        return added
    }

    var challengeGamesPlayed: Int
        get() = prefs.getInt(KEY_CHALLENGE_GAMES, 0)
        set(value) = prefs.edit().putInt(KEY_CHALLENGE_GAMES, value).apply()

    var challengeBestScore: Int
        get() = prefs.getInt(KEY_CHALLENGE_BEST, 0)
        set(value) = prefs.edit().putInt(KEY_CHALLENGE_BEST, value).apply()

    // ---- In-progress boards ---------------------------------------------------------------

    class SavedBoard(val cells: List<CellState>, val seconds: Long, val mistakes: Int)

    fun saveBoard(puzzleId: String, cells: List<CellState>, seconds: Long, mistakes: Int) {
        val encoded = buildString(cells.size) {
            for (c in cells) append(
                when (c) {
                    CellState.EMPTY -> '.'
                    CellState.FILLED -> '#'
                    CellState.CROSSED -> 'x'
                }
            )
        }
        prefs.edit()
            .putString("state:$puzzleId", encoded)
            .putLong("time:$puzzleId", seconds)
            .putInt("mist:$puzzleId", mistakes)
            .apply()
    }

    fun loadBoard(puzzleId: String, expectedSize: Int): SavedBoard? {
        val encoded = prefs.getString("state:$puzzleId", null) ?: return null
        if (encoded.length != expectedSize) return null
        val cells = encoded.map {
            when (it) {
                '#' -> CellState.FILLED
                'x' -> CellState.CROSSED
                else -> CellState.EMPTY
            }
        }
        return SavedBoard(cells, prefs.getLong("time:$puzzleId", 0L), prefs.getInt("mist:$puzzleId", 0))
    }

    fun clearBoard(puzzleId: String) {
        prefs.edit().remove("state:$puzzleId").remove("time:$puzzleId").remove("mist:$puzzleId").apply()
    }

    fun hasSavedBoard(puzzleId: String): Boolean = prefs.contains("state:$puzzleId")

    private companion object {
        const val KEY_CHECK_MISTAKES = "settings.checkMistakes"
        const val KEY_HAPTICS = "settings.haptics"
        const val KEY_RANDOM_SOLVED = "stats.randomSolved"
        const val KEY_ADS_REMOVED = "billing.adsRemoved"
        const val KEY_HINT_CREDITS = "hints.credits"
        const val FREE_STARTER_HINTS = 3
        const val KEY_CARDS = "challenge.cards"
        const val KEY_CHALLENGE_GAMES = "challenge.games"
        const val KEY_CHALLENGE_BEST = "challenge.best"
    }
}
