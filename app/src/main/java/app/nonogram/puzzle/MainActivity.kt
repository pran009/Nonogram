package app.nonogram.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.nonogram.puzzle.ads.AdManager
import app.nonogram.puzzle.ads.ConsentManager
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.data.Wallet
import app.nonogram.puzzle.ui.CoinSounds
import app.nonogram.puzzle.ui.NonogramApp
import app.nonogram.puzzle.ui.theme.NonogramTheme

class MainActivity : ComponentActivity() {

    private lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val store = ProgressStore(this)
        adManager = AdManager(this, store)
        val consent = ConsentManager(this)
        // Ask for ad consent where the law requires it, and only then start ads. A returning
        // player who already answered can start ads right away while the status refreshes.
        consent.gather(this) { if (consent.canRequestAds) adManager.initialize() }
        if (consent.canRequestAds) adManager.initialize()
        val wallet = Wallet(store)
        CoinSounds.init(this, store.soundEnabled)

        setContent {
            NonogramTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NonogramApp(store = store, adManager = adManager, wallet = wallet, consent = consent)
                }
            }
        }
    }
}
