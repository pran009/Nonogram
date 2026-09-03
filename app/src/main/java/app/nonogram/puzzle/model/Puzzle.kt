package app.nonogram.puzzle.model

enum class CellState { EMPTY, FILLED, CROSSED }

/**
 * An immutable nonogram definition. [solution] is row-major: solution[row][col].
 * Clues are derived once at construction time.
 */
class Puzzle(
    val id: String,
    val name: String,
    val solution: List<BooleanArray>,
) {
    val rows: Int = solution.size
    val cols: Int = solution.first().size

    val rowClues: List<List<Int>> = solution.map { cluesOf(it.toList()) }
    val colClues: List<List<Int>> = (0 until cols).map { c -> cluesOf(solution.map { it[c] }) }

    val filledCount: Int = solution.sumOf { row -> row.count { it } }

    fun isFilled(row: Int, col: Int): Boolean = solution[row][col]

    /** Encodes the solution as one string per row using '#' and '.' — used for thumbnails and persistence. */
    fun toRowStrings(): List<String> = solution.map { row -> row.joinToString("") { if (it) "#" else "." } }

    override fun equals(other: Any?): Boolean = other is Puzzle && other.id == id
    override fun hashCode(): Int = id.hashCode()

    companion object {
        /** Run-length clue list for a line. A fully empty line yields an empty list (rendered as "0"). */
        fun cluesOf(line: List<Boolean>): List<Int> {
            val out = ArrayList<Int>()
            var run = 0
            for (cell in line) {
                if (cell) run++ else if (run > 0) { out.add(run); run = 0 }
            }
            if (run > 0) out.add(run)
            return out
        }

        /** Builds a puzzle from ASCII art. '#', 'X', 'x', '1' are filled; anything else is empty. */
        fun fromArt(id: String, name: String, art: List<String>): Puzzle {
            require(art.isNotEmpty()) { "Puzzle $id has no rows" }
            val width = art.first().length
            require(art.all { it.length == width }) { "Puzzle $id has ragged rows" }
            val grid = art.map { row -> BooleanArray(width) { c -> row[c] in "#Xx1" } }
            return Puzzle(id, name, grid)
        }
    }
}
