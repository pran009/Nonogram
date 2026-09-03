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
import app.nonogram.puzzle.data.ProgressStore
import app.nonogram.puzzle.ui.NonogramApp
import app.nonogram.puzzle.ui.theme.NonogramTheme

class MainActivity : ComponentActivity() {

    private lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val store = ProgressStore(this)
        adManager = AdManager(this, store)
        adManager.initialize()

        setContent {
            NonogramTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NonogramApp(store = store, adManager = adManager)
                }
            }
        }
    }
}
