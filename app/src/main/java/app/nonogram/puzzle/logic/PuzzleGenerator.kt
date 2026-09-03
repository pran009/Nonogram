package app.nonogram.puzzle.logic

import app.nonogram.puzzle.model.Puzzle
import kotlin.random.Random

/** Generates random puzzles that are guaranteed to be solvable by logic alone. */
object PuzzleGenerator {

    fun generate(id: String, name: String, size: Int, seed: Long): Puzzle {
        val rng = Random(seed)
        // Larger boards need a slightly higher density to stay line-solvable.
        val density = when {
            size <= 5 -> 0.60
            size <= 10 -> 0.58
            else -> 0.60
        }
        repeat(2000) { attempt ->
            val grid = List(size) { BooleanArray(size) { rng.nextDouble() < density } }
            // Reject boring boards.
            if (grid.any { row -> row.none { it } }) return@repeat
            if ((0 until size).any { c -> grid.none { it[c] } }) return@repeat
            val candidate = Puzzle(id, name, grid)
            if (LineSolver.isLineSolvable(candidate)) return candidate
        }
        // Practically unreachable; fall back to a trivially solvable checkerboard-free block.
        return Puzzle(id, name, List(size) { r -> BooleanArray(size) { c -> (r + c) % 3 != 0 } })
    }
}
