package app.nonogram.puzzle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import app.nonogram.puzzle.model.CellState
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.ui.theme.BoardColors
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** Pixel geometry of the board inside the canvas. */
private class Geometry(
    val cell: Float,
    val gridLeft: Float,
    val gridTop: Float,
    val clueW: Float,
    val clueH: Float,
    val rows: Int,
    val cols: Int,
) {
    val gridW get() = cell * cols
    val gridH get() = cell * rows
    val left get() = gridLeft - clueW
    val top get() = gridTop - clueH
}

private fun computeGeometry(puzzle: Puzzle, width: Float, height: Float, density: Float): Geometry {
    val rowClueMax = max(1, puzzle.rowClues.maxOf { max(1, it.size) })
    val colClueMax = max(1, puzzle.colClues.maxOf { max(1, it.size) })
    val clueUnit = 0.62f // clue number slot relative to a cell
    val pad = 6f * density
    val availW = width - pad * 2
    val availH = height - pad * 2
    val cell = min(availW / (puzzle.cols + rowClueMax * clueUnit), availH / (puzzle.rows + colClueMax * clueUnit))
    val clueW = rowClueMax * clueUnit * cell
    val clueH = colClueMax * clueUnit * cell
    val totalW = clueW + cell * puzzle.cols
    val totalH = clueH + cell * puzzle.rows
    val left = (width - totalW) / 2f
    val top = (height - totalH) / 2f
    return Geometry(cell, left + clueW, top + clueH, clueW, clueH, puzzle.rows, puzzle.cols)
}

/**
 * The interactive nonogram board.
 *  - one finger: tap or drag to paint (drag locks to the row/column of the first move)
 *  - two fingers: pinch to zoom, drag to pan
 *  - double tap: reset zoom
 */
@Composable
fun BoardCanvas(
    puzzle: Puzzle,
    cells: List<CellState>,
    rowDone: List<Boolean>,
    colDone: List<Boolean>,
    hintCell: Int?,
    mistakeCell: Int?,
    solved: Boolean,
    colors: BoardColors,
    onBegin: (Int) -> Pair<CellState, CellState>?,
    onContinue: (Int, CellState, CellState) -> Unit,
    onEnd: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val geo = remember(puzzle, widthPx, heightPx) { computeGeometry(puzzle, widthPx, heightPx, density) }
        val textCache = remember(geo) { HashMap<String, TextLayoutResult>() }

        var scale by remember(puzzle) { mutableFloatStateOf(1f) }
        var offset by remember(puzzle) { mutableStateOf(Offset.Zero) }
        var activeRow by remember { mutableStateOf(-1) }
        var activeCol by remember { mutableStateOf(-1) }

        fun clampOffset(o: Offset, s: Float): Offset {
            if (s <= 1f) return Offset.Zero
            val minX = widthPx - widthPx * s
            val minY = heightPx - heightPx * s
            return Offset(o.x.coerceIn(minX, 0f), o.y.coerceIn(minY, 0f))
        }

        fun cellAt(p: Offset): Int? {
            val bx = (p.x - offset.x) / scale
            val by = (p.y - offset.y) / scale
            val c = floor((bx - geo.gridLeft) / geo.cell).toInt()
            val r = floor((by - geo.gridTop) / geo.cell).toInt()
            if (r !in 0 until geo.rows || c !in 0 until geo.cols) return null
            return r * geo.cols + c
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(puzzle, geo) {
                    var lastTapTime = 0L
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downTime = System.currentTimeMillis()
                        var brush: Pair<CellState, CellState>? = null
                        var lastCell: Int? = null
                        var startCell: Int? = null
                        var lockAxis = 0 // 0 = free, 1 = horizontal, 2 = vertical
                        var multiTouch = false
                        var moved = false

                        cellAt(down.position)?.let { i ->
                            brush = onBegin(i)
                            lastCell = i
                            startCell = i
                            activeRow = i / geo.cols; activeCol = i % geo.cols
                        }
                        do {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size >= 2) {
                                if (!multiTouch) {
                                    multiTouch = true
                                    if (brush != null) onCancel()
                                    brush = null
                                    activeRow = -1; activeCol = -1
                                }
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                val centroid = event.calculateCentroid()
                                val newScale = (scale * zoom).coerceIn(1f, 4f)
                                val effZoom = newScale / scale
                                var o = Offset(
                                    centroid.x - (centroid.x - offset.x) * effZoom + pan.x,
                                    centroid.y - (centroid.y - offset.y) * effZoom + pan.y,
                                )
                                scale = newScale
                                offset = clampOffset(o, newScale)
                                event.changes.forEach { it.consume() }
                            } else if (pressed.size == 1 && !multiTouch) {
                                val change = pressed[0]
                                val b = brush
                                val s = startCell
                                if (b != null && s != null) {
                                    val hit = cellAt(change.position)
                                    if (hit != null && hit != lastCell) {
                                        moved = true
                                        val hr = hit / geo.cols; val hc = hit % geo.cols
                                        val sr = s / geo.cols; val sc = s % geo.cols
                                        if (lockAxis == 0 && hit != s) {
                                            lockAxis = if (abs(hc - sc) >= abs(hr - sr)) 1 else 2
                                        }
                                        val target = when (lockAxis) {
                                            1 -> sr * geo.cols + hc
                                            2 -> hr * geo.cols + sc
                                            else -> hit
                                        }
                                        if (target != lastCell) {
                                            // Fill every cell between lastCell and target so fast drags don't skip.
                                            val from = lastCell ?: target
                                            val fr = from / geo.cols; val fc = from % geo.cols
                                            val tr = target / geo.cols; val tc = target % geo.cols
                                            val steps = max(abs(tr - fr), abs(tc - fc))
                                            for (k in 1..steps) {
                                                val r = fr + (tr - fr) * k / steps
                                                val c = fc + (tc - fc) * k / steps
                                                onContinue(r * geo.cols + c, b.first, b.second)
                                            }
                                            lastCell = target
                                            activeRow = tr; activeCol = tc
                                        }
                                    }
                                }
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })

                        if (brush != null) onEnd()
                        activeRow = -1; activeCol = -1
                        if (!moved && !multiTouch) {
                            if (downTime - lastTapTime < 300 && scale > 1f) {
                                scale = 1f; offset = Offset.Zero
                            }
                            lastTapTime = downTime
                        }
                    }
                }
        ) {
            translate(offset.x, offset.y) {
                scale(scale, pivot = Offset.Zero) {
                    drawBoard(
                        geo, puzzle, cells, rowDone, colDone, hintCell, mistakeCell, solved,
                        activeRow, activeCol, colors, textMeasurer, textCache, density,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBoard(
    geo: Geometry,
    puzzle: Puzzle,
    cells: List<CellState>,
    rowDone: List<Boolean>,
    colDone: List<Boolean>,
    hintCell: Int?,
    mistakeCell: Int?,
    solved: Boolean,
    activeRow: Int,
    activeCol: Int,
    colors: BoardColors,
    textMeasurer: TextMeasurer,
    textCache: HashMap<String, TextLayoutResult>,
    density: Float,
) {
    val cell = geo.cell
    val corner = 6f * density

    // Backgrounds
    drawRoundRect(colors.clueBackground, Offset(geo.left, geo.top), Size(geo.clueW + geo.gridW, geo.clueH + geo.gridH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner))
    drawRect(colors.boardBackground, Offset(geo.gridLeft, geo.gridTop), Size(geo.gridW, geo.gridH))

    // Crosshair highlight
    if (!solved && activeRow >= 0) {
        drawRect(colors.crosshair, Offset(geo.left, geo.gridTop + activeRow * cell), Size(geo.clueW + geo.gridW, cell))
        drawRect(colors.crosshair, Offset(geo.gridLeft + activeCol * cell, geo.top), Size(cell, geo.clueH + geo.gridH))
    }

    // Cells
    val inset = cell * 0.08f
    val crossStroke = max(1.5f * density, cell * 0.09f)
    for (r in 0 until geo.rows) for (c in 0 until geo.cols) {
        val i = r * geo.cols + c
        val x = geo.gridLeft + c * cell
        val y = geo.gridTop + r * cell
        when (cells[i]) {
            CellState.FILLED -> drawRect(
                if (solved) colors.solvedFill else colors.filled,
                Offset(x + inset, y + inset), Size(cell - inset * 2, cell - inset * 2),
            )
            CellState.CROSSED -> if (!solved) {
                val m = cell * 0.3f
                drawLine(colors.cross, Offset(x + m, y + m), Offset(x + cell - m, y + cell - m), crossStroke)
                drawLine(colors.cross, Offset(x + m, y + cell - m), Offset(x + cell - m, y + m), crossStroke)
            }
            CellState.EMPTY -> Unit
        }
        if (hintCell == i && !solved) {
            drawRect(colors.hint, Offset(x, y), Size(cell, cell))
        }
        if (mistakeCell == i && !solved) {
            drawRect(colors.mistake, Offset(x, y), Size(cell, cell), style = Stroke(2.5f * density))
        }
    }

    // Grid lines (thin every cell, thick every 5 and on the border)
    if (!solved) {
        val thin = max(1f, 0.8f * density)
        val thick = max(1.5f, 1.8f * density)
        for (c in 0..geo.cols) {
            val x = geo.gridLeft + c * cell
            val isThick = c % 5 == 0 || c == geo.cols
            drawLine(if (isThick) colors.gridThick else colors.gridLine, Offset(x, geo.top), Offset(x, geo.gridTop + geo.gridH), if (isThick) thick else thin)
        }
        for (r in 0..geo.rows) {
            val y = geo.gridTop + r * cell
            val isThick = r % 5 == 0 || r == geo.rows
            drawLine(if (isThick) colors.gridThick else colors.gridLine, Offset(geo.left, y), Offset(geo.gridLeft + geo.gridW, y), if (isThick) thick else thin)
        }
    } else {
        drawRect(colors.gridThick, Offset(geo.gridLeft, geo.gridTop), Size(geo.gridW, geo.gridH), style = Stroke(1.8f * density))
    }

    // Clues
    val fontPx = cell * 0.44f
    val style = TextStyle(fontSize = TextUnit(fontPx / density, TextUnitType.Sp), fontWeight = FontWeight.SemiBold)
    fun layoutOf(text: String, color: Color): TextLayoutResult =
        textCache.getOrPut("$text|${color.value}") { textMeasurer.measure(text, style.copy(color = color)) }

    val slot = cell * 0.62f
    for (r in 0 until geo.rows) {
        val clues = puzzle.rowClues[r].ifEmpty { listOf(0) }
        val color = if (rowDone[r] || solved) colors.clueDone else colors.clueText
        val cy = geo.gridTop + r * cell + cell / 2
        for ((k, n) in clues.asReversed().withIndex()) {
            val layout = layoutOf(n.toString(), color)
            val cx = geo.gridLeft - slot * (k + 0.5f)
            drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, cy - layout.size.height / 2f))
        }
    }
    for (c in 0 until geo.cols) {
        val clues = puzzle.colClues[c].ifEmpty { listOf(0) }
        val color = if (colDone[c] || solved) colors.clueDone else colors.clueText
        val cx = geo.gridLeft + c * cell + cell / 2
        for ((k, n) in clues.asReversed().withIndex()) {
            val layout = layoutOf(n.toString(), color)
            val cy = geo.gridTop - slot * (k + 0.5f)
            drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, cy - layout.size.height / 2f))
        }
    }
}

/** Small static preview of a solution, used on level cards and the win dialog. */
@Composable
fun PuzzleThumbnail(puzzle: Puzzle, color: Color, background: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cell = min(size.width / puzzle.cols, size.height / puzzle.rows)
        val left = (size.width - cell * puzzle.cols) / 2
        val top = (size.height - cell * puzzle.rows) / 2
        drawRect(background, Offset(left, top), Size(cell * puzzle.cols, cell * puzzle.rows))
        for (r in 0 until puzzle.rows) for (c in 0 until puzzle.cols) {
            if (puzzle.solution[r][c]) drawRect(color, Offset(left + c * cell, top + r * cell), Size(cell + 0.5f, cell + 0.5f))
        }
    }
}
