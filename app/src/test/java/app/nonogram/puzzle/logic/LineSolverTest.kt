package app.nonogram.puzzle.logic

import app.nonogram.puzzle.data.Puzzles
import app.nonogram.puzzle.model.CellState
import app.nonogram.puzzle.model.Puzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LineSolverTest {

    private fun line(s: String) = s.map {
        when (it) { '#' -> CellState.FILLED; 'x' -> CellState.CROSSED; else -> CellState.EMPTY }
    }.toTypedArray()

    private fun str(a: Array<CellState>) = a.joinToString("") {
        when (it) { CellState.FILLED -> "#"; CellState.CROSSED -> "x"; else -> "." }
    }

    @Test fun cluesOfComputesRuns() {
        assertEquals(listOf(2, 1), Puzzle.cluesOf(listOf(true, true, false, true)))
        assertEquals(emptyList<Int>(), Puzzle.cluesOf(listOf(false, false)))
    }

    @Test fun fullLineIsForced() {
        assertEquals("#####", str(LineSolver.solveLine(listOf(5), line("....."))!!))
        assertEquals("##x##", str(LineSolver.solveLine(listOf(2, 2), line("....."))!!))
    }

    @Test fun overlapDeduction() {
        // A 3-block in 5 cells must cover the middle cell.
        assertEquals("..#..", str(LineSolver.solveLine(listOf(3), line("....."))!!))
    }

    @Test fun emptyClueCrossesEverything() {
        assertEquals("xxx", str(LineSolver.solveLine(emptyList(), line("..."))!!))
    }

    @Test fun contradictionReturnsNull() {
        assertNull(LineSolver.solveLine(listOf(3), line("#.x..")))
        assertNull(LineSolver.solveLine(emptyList(), line("..#")))
    }

    @Test fun usesKnownCells() {
        // Known cross at index 2 forces the 2-block to the right side.
        assertEquals("xxx##", str(LineSolver.solveLine(listOf(2), line("x.x.."))!!))
    }

    @Test fun everyBuiltInPuzzleIsLineSolvable() {
        val failures = Puzzles.packs.flatMap { it.puzzles }.filterNot { LineSolver.isLineSolvable(it) }
        assertTrue("Not line-solvable: " + failures.joinToString { "${it.id}(${it.name})" }, failures.isEmpty())
    }

    @Test fun puzzleIdsAreUnique() {
        val ids = Puzzles.packs.flatMap { it.puzzles }.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun puzzlesMatchPackSize() {
        for (pack in Puzzles.packs) for (p in pack.puzzles) {
            assertEquals("${p.id} rows", pack.size, p.rows)
            assertEquals("${p.id} cols", pack.size, p.cols)
        }
    }

    @Test fun generatorProducesSolvablePuzzlesDeterministically() {
        for (size in listOf(5, 10, 15)) {
            val a = PuzzleGenerator.generate("t", "t", size, 42L)
            val b = PuzzleGenerator.generate("t", "t", size, 42L)
            assertEquals(a.toRowStrings(), b.toRowStrings())
            assertTrue(LineSolver.isLineSolvable(a))
        }
    }

    @Test fun solvingFromPartialBoardMakesProgress() {
        val p = Puzzles.pack5.puzzles.first()
        val result = LineSolver.solve(p)
        assertTrue(result.solved)
        for (r in 0 until p.rows) for (c in 0 until p.cols) {
            assertEquals(p.solution[r][c], result.grid[r][c] == CellState.FILLED)
        }
    }
}
