package app.nonogram.puzzle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nonogram.puzzle.ads.AdManager
import app.nonogram.puzzle.ads.BannerAd
import app.nonogram.puzzle.data.Facts
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.Wallet
import app.nonogram.puzzle.logic.Rewards
import app.nonogram.puzzle.model.CellState
import app.nonogram.puzzle.model.Puzzle
import app.nonogram.puzzle.ui.theme.LocalBoardColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    puzzle: Puzzle,
    title: String,
    store: ProgressStore,
    adManager: AdManager,
    wallet: Wallet,
    /** True when this puzzle was already solved before: no coins, and a replay ad first. */
    isReplay: Boolean,
    nextPuzzle: Puzzle?,
    onBack: () -> Unit,
    onNext: (Puzzle) -> Unit,
) {
    val rewards = remember(puzzle.id) {
        GameRewards(puzzle, wallet, store, isReplay, persistLines = true, coinsOff = !store.checkMistakes).also {
            it.shielded = store.puzzleMode(puzzle.id) == ProgressStore.MODE_SHIELD
        }
    }
    val flights = rememberCoinFlights()
    var flySource by remember { mutableStateOf<Offset?>(null) }
    LaunchedEffect(rewards.toastTick) {
        val from = flySource
        if (rewards.toastTick > 0 && from != null && rewards.lastLineCoins > 0) flights.launch(from, rewards.lastLineCoins)
    }
    val controller = remember(puzzle.id) {
        GameController(puzzle, store, onLinesCorrect = { rewards.onLinesCorrect(it) }, onMistake = { rewards.onMistake() })
    }
    val fact = remember(puzzle.id) { Facts.of(puzzle.id) }
    var showFact by remember { mutableStateOf(false) }
    var secretUnlocked by remember { mutableStateOf(store.isSecretUnlocked(puzzle.id)) }
    var secretJustUnlocked by remember { mutableStateOf(false) }
    var secretAdProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var secretAdMessage by remember { mutableStateOf<String?>(null) }
    // New, coin-earning puzzles with mistake checking ask how to play first.
    var modeDialog by remember {
        mutableStateOf(!isReplay && controller.checkMistakes && !controller.solved &&
            store.puzzleMode(puzzle.id) == ProgressStore.MODE_UNSET)
    }
    var modeBusy by remember { mutableStateOf(false) }
    var modeMessage by remember { mutableStateOf<String?>(null) }
    val haptics = LocalHapticFeedback.current
    val boardColors = LocalBoardColors.current
    val context = LocalContext.current
    val isDaily = puzzle.id.startsWith("daily-")
    var showRestart by remember { mutableStateOf(false) }
    var showWin by remember { mutableStateOf(false) }
    var hintCredits by remember { mutableStateOf(store.hintCredits) }
    var hintDialog by remember { mutableStateOf(false) }
    var hintProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var summary by remember { mutableStateOf<GameRewards.WinSummary?>(null) }
    var doubleBusy by remember { mutableStateOf(false) }
    var doubleMessage by remember { mutableStateOf<String?>(null) }

    // Replays start with one full-screen ad (and pay nothing).
    LaunchedEffect(puzzle.id) {
        if (isReplay) context.findActivity()?.let { adManager.showReplayAd(it) {} }
    }

    /** Every 2nd solved puzzle, "Next puzzle" shows one full-screen ad first. */
    fun goNext(next: Puzzle) {
        val act = context.findActivity()
        if (act != null && store.solvedSinceAd >= 2) {
            adManager.showInterstitial(act) { shown ->
                if (shown) store.solvedSinceAd = 0
                onNext(next)
            }
        } else {
            onNext(next)
        }
    }

    fun onHintClicked() {
        when {
            adManager.adsRemoved -> controller.hint()
            hintCredits > 0 -> {
                hintCredits -= 1
                store.hintCredits = hintCredits
                controller.hint()
            }
            else -> hintDialog = true
        }
    }

    fun purchaseHint(coins: Int, ads: Int) {
        val act = context.findActivity() ?: return
        hintProgress = 0 to ads
        buyHint(act, adManager, wallet, coins, ads, onProgress = { shown, total -> hintProgress = shown to total }) { ok ->
            hintProgress = null
            hintDialog = false
            if (ok) controller.hint()
        }
    }

    // Timer (paused while the hint dialog or its ads are open).
    val clockPaused = hintDialog || modeDialog
    LaunchedEffect(controller, controller.solved, clockPaused) {
        while (!controller.solved && !clockPaused) {
            delay(1000)
            controller.tick()
        }
    }
    DisposableEffect(controller) { onDispose { controller.save() } }

    LaunchedEffect(controller.lastChangeTick) {
        if (controller.lastChangeTick == 0L) return@LaunchedEffect
        if (store.hapticsEnabled) {
            haptics.performHapticFeedback(
                if (controller.lastChangeWasMistake) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
        }
    }
    LaunchedEffect(controller.mistakeCell) {
        if (controller.mistakeCell != null) { delay(700); controller.clearMistakeFlash() }
    }
    LaunchedEffect(controller.solved) {
        if (controller.solved) {
            if (summary == null) {
                summary = rewards.onPuzzleSolved(controller.mistakes, controller.checkMistakes, isDaily)
                store.solvedSinceAd = store.solvedSinceAd + 1
                if (fact != null && controller.checkMistakes && controller.mistakes == 0 && !secretUnlocked) {
                    store.unlockSecret(puzzle.id)
                    secretUnlocked = true
                    secretJustUnlocked = true
                }
            }
            delay(350)
            if (fact != null) showFact = true else showWin = true
        }
    }

    CoinFlightHost(flights) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    },
                    actions = {
                        CoinBadge(wallet.coins, Modifier.coinFlightTarget(flights), countDelayMs = CoinFlights.ARRIVAL_MS)
                        Spacer(Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(formatTime(controller.seconds), style = MaterialTheme.typography.titleMedium)
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${puzzle.rows}×${puzzle.cols}" + if (isReplay) " · Replay" else "", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        when {
                            adManager.adsRemoved -> "Hints: ∞"
                            hintCredits > 0 -> "Free hints: $hintCredits"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (controller.checkMistakes) {
                        Text(
                            (if (rewards.shielded) "🛡 " else "") + "Mistakes: ${controller.mistakes}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (controller.mistakes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text("Free play · no coins", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                val rowDone = (0 until puzzle.rows).map { controller.rowDone(it) }
                val colDone = (0 until puzzle.cols).map { controller.colDone(it) }

                Box(Modifier.fillMaxWidth().weight(1f).onGloballyPositioned { val b = it.boundsInRoot(); flySource = Offset(b.center.x, b.top + 70f) }) {
                    BoardCanvas(
                        puzzle = puzzle,
                        cells = controller.cells,
                        rowDone = rowDone,
                        colDone = colDone,
                        hintCell = controller.hintCell,
                        mistakeCell = controller.mistakeCell,
                        solved = controller.solved,
                        colors = boardColors,
                        onBegin = { controller.beginStroke(it) },
                        onContinue = { i, brush, original -> controller.continueStroke(i, brush, original) },
                        onEnd = { controller.endStroke() },
                        onCancel = { controller.cancelStroke() },
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    )
                    CoinToast(rewards.toast, rewards.toastTick, Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SingleChoiceSegmentedButtonRow(Modifier.height(44.dp)) {
                        SegmentedButton(
                            selected = controller.mode == CellState.FILLED,
                            onClick = { controller.mode = CellState.FILLED },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                            icon = { Icon(Icons.Default.Square, null, Modifier.size(16.dp)) },
                            label = { Text("Fill") },
                        )
                        SegmentedButton(
                            selected = controller.mode == CellState.CROSSED,
                            onClick = { controller.mode = CellState.CROSSED },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                            icon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                            label = { Text("Cross") },
                        )
                    }
                    Row {
                        FilledTonalIconButton(onClick = { controller.undo() }, enabled = controller.canUndo && !controller.solved) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }
                        FilledTonalIconButton(onClick = { onHintClicked() }, enabled = !controller.solved) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Hint")
                        }
                        FilledTonalIconButton(onClick = { showRestart = true }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart")
                        }
                    }
                }

                BannerAd(adManager, Modifier.padding(bottom = 4.dp))
            }
        }
    }

    if (modeDialog) {
        PlayModeDialog(
            busy = modeBusy,
            message = modeMessage,
            onNormal = {
                store.setPuzzleMode(puzzle.id, ProgressStore.MODE_NORMAL)
                rewards.shielded = false
                modeDialog = false
            },
            onShield = {
                val act = context.findActivity()
                if (act != null) {
                    modeBusy = true; modeMessage = null
                    adManager.showStrictRewardedAds(act, 1) { ok ->
                        modeBusy = false
                        if (ok) {
                            store.setPuzzleMode(puzzle.id, ProgressStore.MODE_SHIELD)
                            rewards.shielded = true
                            modeDialog = false
                        } else {
                            modeMessage = "No ad available right now. Try again, or play normally."
                        }
                    }
                }
            },
            onLeave = { modeDialog = false; onBack() },
        )
    }

    if (showFact && fact != null) {
        FactCardDialog(
            puzzle = puzzle,
            fact = fact,
            secretUnlocked = secretUnlocked,
            justUnlocked = secretJustUnlocked,
            adProgress = secretAdProgress,
            adMessage = secretAdMessage,
            onWatchAds = {
                val act = context.findActivity()
                if (act != null) {
                    secretAdProgress = 0 to 2; secretAdMessage = null
                    adManager.showStrictRewardedAds(act, 2, onProgress = { shown, total -> secretAdProgress = shown to total }) { ok ->
                        secretAdProgress = null
                        if (ok) {
                            store.unlockSecret(puzzle.id)
                            secretJustUnlocked = true
                            secretUnlocked = true
                        } else {
                            secretAdMessage = "The secret unlocks after both ads are watched to the end."
                        }
                    }
                }
            },
            onContinue = { showFact = false; showWin = true },
        )
    }

    if (showRestart) {
        AlertDialog(
            onDismissRequest = { showRestart = false },
            title = { Text("Restart puzzle?") },
            text = { Text("Your progress on this puzzle will be cleared. Lines that already earned coins won't pay again.") },
            confirmButton = { TextButton(onClick = { controller.restart(); showRestart = false }) { Text("Restart") } },
            dismissButton = { TextButton(onClick = { showRestart = false }) { Text("Cancel") } },
        )
    }

    if (hintDialog) {
        val price = Rewards.hintPrice(1)
        HintDialog(
            price = price,
            coins = wallet.coins,
            progress = hintProgress,
            clockPausedNote = true,
            onCoinsAndAds = { purchaseHint(price.coins, price.adsWithCoins) },
            onAdsOnly = { purchaseHint(0, price.adsOnly) },
            onDismiss = { hintDialog = false },
        )
    }

    if (showWin) {
        val best = store.bestTimeSeconds(puzzle.id)
        AlertDialog(
            onDismissRequest = { showWin = false },
            title = { Text("Solved: ${puzzle.name}") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                ) {
                    PuzzleThumbnail(
                        puzzle, boardColors.solvedFill, boardColors.boardBackground,
                        Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(boardColors.boardBackground),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Time ${formatTime(controller.seconds)}", style = MaterialTheme.typography.titleMedium)
                    if (best != null && best < controller.seconds) {
                        Text("Best ${formatTime(best)}", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val details = buildList {
                        if (controller.checkMistakes) add(if (controller.mistakes == 1) "1 mistake" else "${controller.mistakes} mistakes")
                        if (controller.hintsUsed > 0) add("${controller.hintsUsed} hints")
                    }
                    if (details.isNotEmpty()) {
                        Text(details.joinToString(" · "), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    WinCoinsSection(
                        summary = summary,
                        isReplay = isReplay,
                        doubled = rewards.doubled,
                        doubleBusy = doubleBusy,
                        doubleMessage = doubleMessage,
                        coinsOff = rewards.coinsOff,
                        onDouble = {
                            val act = context.findActivity()
                            val s = summary
                            if (act != null && s != null) {
                                doubleBusy = true; doubleMessage = null
                                adManager.showDoubleCoinsAd(act) { ok ->
                                    doubleBusy = false
                                    if (ok) rewards.applyDouble(s) else doubleMessage = "No ad available right now."
                                }
                            }
                        },
                    )
                    TextButton(onClick = {
                        shareText(
                            context,
                            "I solved \"${puzzle.name}\" (${puzzle.rows}×${puzzle.cols}) in " +
                                "${formatTime(controller.seconds)} on Nonogram! Can you beat it? $STORE_URL",
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            },
            confirmButton = {
                if (nextPuzzle != null) {
                    Button(onClick = { showWin = false; goNext(nextPuzzle) }) { Text("Next puzzle") }
                } else {
                    Button(onClick = { showWin = false; onBack() }) { Text("Done") }
                }
            },
            dismissButton = { TextButton(onClick = { showWin = false }) { Text("Admire") } },
        )
    }
}
