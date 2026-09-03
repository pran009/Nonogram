package app.nonogram.puzzle.logic

import app.nonogram.puzzle.model.CellState
import app.nonogram.puzzle.model.Puzzle

/**
 * Pure logic solver. It only uses line-by-line deduction (the same reasoning a human uses),
 * which makes it perfect for two jobs:
 *  1. Verifying that a puzzle can be solved without guessing.
 *  2. Producing a hint that is a genuine logical next step from the player's current board.
 */
object LineSolver {

    /**
     * Deduces everything that can be known about one line.
     * Returns null if the known cells contradict the clues, otherwise a new line where every cell
     * that is FILLED in all valid placements is FILLED and every cell that is empty in all valid
     * placements is CROSSED. Unknown cells stay EMPTY.
     */
    fun solveLine(clues: List<Int>, line: Array<CellState>): Array<CellState>? {
        val n = line.size
        if (clues.isEmpty()) {
            if (line.any { it == CellState.FILLED }) return null
            return Array(n) { CellState.CROSSED }
        }
        val canFill = BooleanArray(n)
        val canEmpty = BooleanArray(n)
        val memo = HashMap<Int, Boolean>()

        // Can clues[ci..] be placed inside line[pos..] without contradicting known cells?
        fun feasible(ci: Int, pos: Int): Boolean {
            val key = ci * 64 + pos
            memo[key]?.let { return it }
            val result: Boolean
            if (ci == clues.size) {
                result = (pos until n).none { line[it] == CellState.FILLED }
            } else {
                var found = false
                val len = clues[ci]
                var start = pos
                while (start + len <= n) {
                    if (start > pos && line[start - 1] == CellState.FILLED) break
                    var ok = true
                    for (i in start until start + len) if (line[i] == CellState.CROSSED) { ok = false; break }
                    val after = start + len
                    if (ok && after < n && line[after] == CellState.FILLED) ok = false
                    if (ok && feasible(ci + 1, if (after < n) after + 1 else after)) { found = true; break }
                    start++
                }
                result = found
            }
            memo[key] = result
            return result
        }

        if (!feasible(0, 0)) return null

        // Enumerate placements, pruned so every branch explored leads to a valid full placement.
        val placement = IntArray(clues.size)
        fun enumerate(ci: Int, pos: Int) {
            if (ci == clues.size) {
                var i = 0
                for (b in clues.indices) {
                    val s = placement[b]
                    while (i < s) { canEmpty[i] = true; i++ }
                    for (k in s until s + clues[b]) canFill[k] = true
                    i = s + clues[b]
                }
                while (i < n) { canEmpty[i] = true; i++ }
                return
            }
            val len = clues[ci]
            var start = pos
            while (start + len <= n) {
                if (start > pos && line[start - 1] == CellState.FILLED) break
                var ok = true
                for (i in start until start + len) if (line[i] == CellState.CROSSED) { ok = false; break }
                val after = start + len
                if (ok && after < n && line[after] == CellState.FILLED) ok = false
                if (ok) {
                    val next = if (after < n) after + 1 else after
                    if (feasible(ci + 1, next)) { placement[ci] = start; enumerate(ci + 1, next) }
                }
                start++
            }
        }
        enumerate(0, 0)

        return Array(n) { i ->
            when {
                canFill[i] && !canEmpty[i] -> CellState.FILLED
                canEmpty[i] && !canFill[i] -> CellState.CROSSED
                else -> line[i]
            }
        }
    }

    class Result(val grid: Array<Array<CellState>>, val contradiction: Boolean) {
        val solved: Boolean get() = !contradiction && grid.all { row -> row.none { it == CellState.EMPTY } }
    }

    /**
     * Propagates line deductions until nothing changes. Starts from [initial] (row-major) or an empty grid.
     */
    fun solve(puzzle: Puzzle, initial: Array<Array<CellState>>? = null): Result {
        val rows = puzzle.rows
        val cols = puzzle.cols
        val grid = Array(rows) { r -> Array(cols) { c -> initial?.get(r)?.get(c) ?: CellState.EMPTY } }
        val dirtyRows = BooleanArray(rows) { true }
        val dirtyCols = BooleanArray(cols) { true }
        var progress = true
        while (progress) {
            progress = false
            for (r in 0 until rows) {
                if (!dirtyRows[r]) continue
                dirtyRows[r] = false
                val solved = solveLine(puzzle.rowClues[r], grid[r]) ?: return Result(grid, true)
                for (c in 0 until cols) if (solved[c] != grid[r][c]) {
                    grid[r][c] = solved[c]; dirtyCols[c] = true; progress = true
                }
            }
            for (c in 0 until cols) {
                if (!dirtyCols[c]) continue
                dirtyCols[c] = false
                val column = Array(rows) { r -> grid[r][c] }
                val solved = solveLine(puzzle.colClues[c], column) ?: return Result(grid, true)
                for (r in 0 until rows) if (solved[r] != grid[r][c]) {
                    grid[r][c] = solved[r]; dirtyRows[r] = true; progress = true
                }
            }
        }
        return Result(grid, false)
    }

    /** True when the puzzle can be completed using line logic alone (no guessing). */
    fun isLineSolvable(puzzle: Puzzle): Boolean = solve(puzzle).solved
}
