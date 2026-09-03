package app.nonogram.puzzle.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.logic.LineSolver
import app.nonogram.puzzle.model.CellState
import app.nonogram.puzzle.model.Puzzle

/**
 * Holds all mutable state for one game: the board, brush mode, timer, mistakes, undo history.
 * It is plain Kotlin with Compose state holders so the UI recomposes on change.
 */
class GameController(
    val puzzle: Puzzle,
    private val store: ProgressStore,
) {
    val cellCount = puzzle.rows * puzzle.cols

    var cells by mutableStateOf(List(cellCount) { CellState.EMPTY })
        private set
    var mode by mutableStateOf(CellState.FILLED)
    var seconds by mutableLongStateOf(0L)
        private set
    var mistakes by mutableIntStateOf(0)
        private set
    var hintsUsed by mutableIntStateOf(0)
        private set
    var solved by mutableStateOf(false)
        private set
    var hintCell by mutableStateOf<Int?>(null)
        private set
    var mistakeCell by mutableStateOf<Int?>(null)
        private set
    var canUndo by mutableStateOf(false)
        private set

    /** Event counter the UI observes to trigger haptics. */
    var lastChangeTick by mutableLongStateOf(0L)
        private set
    var lastChangeWasMistake by mutableStateOf(false)
        private set

    val checkMistakes: Boolean get() = store.checkMistakes

    private val undoStack = ArrayDeque<List<Pair<Int, CellState>>>()
    private var stroke: MutableList<Pair<Int, CellState>>? = null

    init {
        store.loadBoard(puzzle.id, cellCount)?.let { saved ->
            cells = saved.cells
            seconds = saved.seconds
            mistakes = saved.mistakes
            checkSolved()
        }
    }

    fun index(row: Int, col: Int) = row * puzzle.cols + col

    /** True when a row's filled cells already match its clue, so the clue can be dimmed. */
    fun rowDone(row: Int): Boolean {
        val line = (0 until puzzle.cols).map { cells[index(row, it)] == CellState.FILLED }
        return Puzzle.cluesOf(line) == puzzle.rowClues[row]
    }

    fun colDone(col: Int): Boolean {
        val line = (0 until puzzle.rows).map { cells[index(it, col)] == CellState.FILLED }
        return Puzzle.cluesOf(line) == puzzle.colClues[col]
    }

    // ---- Strokes (tap + drag painting) ------------------------------------------------------

    /**
     * Starts a stroke at [i]. Returns the brush (new state) to apply to further cells in the
     * same stroke, together with the original state that dragged-over cells must match.
     */
    fun beginStroke(i: Int): Pair<CellState, CellState>? {
        if (solved) return null
        stroke = mutableListOf()
        val original = cells[i]
        val brush = if (original == mode) CellState.EMPTY else mode
        applyBrush(i, brush)
        return brush to original
    }

    fun continueStroke(i: Int, brush: CellState, original: CellState) {
        if (solved || stroke == null) return
        if (cells[i] != original) return
        applyBrush(i, brush)
    }

    fun endStroke() {
        val s = stroke ?: return
        stroke = null
        if (s.isNotEmpty()) {
            undoStack.addLast(s)
            if (undoStack.size > 200) undoStack.removeFirst()
            canUndo = true
        }
        save()
    }

    /** Reverts the very first press of a stroke (used when a second finger arrives for zooming). */
    fun cancelStroke() {
        val s = stroke ?: return
        stroke = null
        for ((i, prev) in s.asReversed()) setCellRaw(i, prev)
        recompute()
    }

    private fun applyBrush(i: Int, brush: CellState) {
        val prev = cells[i]
        var next = brush
        var mistake = false
        if (brush == CellState.FILLED && checkMistakes && !puzzle.solution[i / puzzle.cols][i % puzzle.cols]) {
            next = CellState.CROSSED
            mistake = true
        }
        if (next == prev) return
        stroke?.add(i to prev)
        setCellRaw(i, next)
        if (mistake) {
            mistakes++
            mistakeCell = i
        }
        hintCell = null
        lastChangeWasMistake = mistake
        lastChangeTick++
        recompute()
    }

    private fun setCellRaw(i: Int, state: CellState) {
        cells = cells.toMutableList().also { it[i] = state }
    }

    private fun recompute() {
        checkSolved()
        if (solved) onSolved()
    }

    private fun checkSolved() {
        var filled = 0
        for (i in 0 until cellCount) {
            if (cells[i] == CellState.FILLED) {
                if (!puzzle.solution[i / puzzle.cols][i % puzzle.cols]) { solved = false; return }
                filled++
            }
        }
        solved = filled == puzzle.filledCount
    }

    private fun onSolved() {
        // Tidy up: cross every remaining empty cell so the picture is clean.
        cells = cells.map { if (it == CellState.EMPTY) CellState.CROSSED else it }
        stroke = null
        hintCell = null
        mistakeCell = null
        store.markCompleted(puzzle.id, seconds)
        if (puzzle.id.startsWith("random-") || puzzle.id.startsWith("daily-")) {
            store.randomSolvedCount = store.randomSolvedCount + 1
        }
    }

    // ---- Undo / hint / restart --------------------------------------------------------------

    fun undo() {
        if (solved) return
        val s = undoStack.removeLastOrNull() ?: return
        for ((i, prev) in s.asReversed()) setCellRaw(i, prev)
        canUndo = undoStack.isNotEmpty()
        hintCell = null
        recompute()
        save()
    }

    /**
     * Gives a logical hint. Priority: fix a wrong cell, then reveal a cell that line logic can
     * deduce from the current board, then (only if the board needs guessing) reveal any cell.
     */
    fun hint() {
        if (solved) return
        val target: Int
        val value: CellState
        val wrong = (0 until cellCount).firstOrNull { i ->
            val want = puzzle.solution[i / puzzle.cols][i % puzzle.cols]
            (cells[i] == CellState.FILLED && !want) || (cells[i] == CellState.CROSSED && want)
        }
        if (wrong != null) {
            target = wrong
            value = if (puzzle.solution[wrong / puzzle.cols][wrong % puzzle.cols]) CellState.FILLED else CellState.CROSSED
        } else {
            val grid = Array(puzzle.rows) { r -> Array(puzzle.cols) { c -> cells[index(r, c)] } }
            val result = LineSolver.solve(puzzle, grid)
            var pick = -1
            if (!result.contradiction) {
                // Prefer a filled deduction; it feels more useful than a cross.
                for (i in 0 until cellCount) {
                    val v = result.grid[i / puzzle.cols][i % puzzle.cols]
                    if (cells[i] == CellState.EMPTY && v == CellState.FILLED) { pick = i; break }
                }
                if (pick < 0) for (i in 0 until cellCount) {
                    val v = result.grid[i / puzzle.cols][i % puzzle.cols]
                    if (cells[i] == CellState.EMPTY && v != CellState.EMPTY) { pick = i; break }
                }
            }
            if (pick < 0) pick = (0 until cellCount).firstOrNull { cells[it] == CellState.EMPTY } ?: return
            target = pick
            value = if (puzzle.solution[pick / puzzle.cols][pick % puzzle.cols]) CellState.FILLED else CellState.CROSSED
        }
        val prev = cells[target]
        undoStack.addLast(listOf(target to prev))
        canUndo = true
        setCellRaw(target, value)
        hintsUsed++
        hintCell = target
        lastChangeWasMistake = false
        lastChangeTick++
        recompute()
        save()
    }

    fun restart() {
        cells = List(cellCount) { CellState.EMPTY }
        undoStack.clear()
        canUndo = false
        stroke = null
        seconds = 0
        mistakes = 0
        hintsUsed = 0
        solved = false
        hintCell = null
        mistakeCell = null
        store.clearBoard(puzzle.id)
    }

    fun clearMistakeFlash() { mistakeCell = null }

    // ---- Timer & persistence ----------------------------------------------------------------

    fun tick() {
        if (solved) return
        seconds++
        if (seconds % 10 == 0L) save()
    }

    fun save() {
        if (solved) return
        store.saveBoard(puzzle.id, cells, seconds, mistakes)
    }
}

fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m >= 60) "%d:%02d:%02d".format(m / 60, m % 60, s) else "%02d:%02d".format(m, s)
}
