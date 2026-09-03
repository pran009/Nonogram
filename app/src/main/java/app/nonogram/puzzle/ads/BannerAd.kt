package app.nonogram.puzzle.ads

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * An adaptive anchored banner. Renders nothing when ads are removed.
 * Place it at the bottom of a screen.
 */
@Composable
fun BannerAd(adManager: AdManager, modifier: Modifier = Modifier) {
    if (adManager.adsRemoved) return
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            FrameLayout(ctx).apply {
                val adView = AdView(ctx).apply {
                    adUnitId = AdIds.banner
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, widthDp))
                }
                addView(adView)
                adView.loadAd(AdRequest.Builder().build())
            }
        },
    )
}
