package app.nonogram.puzzle.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.nonogram.puzzle.data.ProgressStore
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Owns everything AdMob. One instance lives for the whole app (created in MainActivity).
 *
 * Each placement has its own ad unit (so AdMob reports show which one earns) and its own
 * preloaded ad. Ads are shown only when [adsRemoved] is false, so the future "Remove ads"
 * purchase is a single flag flip.
 */
class AdManager(context: Context, private val store: ProgressStore) {

    private val appContext = context.applicationContext

    /** Compose-observable so the banner recomposes away the instant ads are removed. */
    var adsRemoved by mutableStateOf(store.adsRemoved)
        private set

    private val hintSlot = RewardedSlot(AdIds.hint)
    private val freeCoinsSlot = RewardedSlot(AdIds.freeCoins)
    private val doubleCoinsSlot = RewardedSlot(AdIds.doubleCoins)
    private val replaySlot = InterstitialSlot(AdIds.replay)

    fun initialize() {
        MobileAds.initialize(appContext) {}
        if (!adsRemoved) {
            hintSlot.load(); freeCoinsSlot.load(); doubleCoinsSlot.load(); replaySlot.load()
        }
    }

    /** Called by the future "Remove ads" purchase to hide all ads immediately. */
    fun applyAdsRemoved(removed: Boolean) {
        store.adsRemoved = removed
        adsRemoved = removed
    }

    // ---- Hints: lenient (a missing ad never blocks the player) -----------------------------

    /**
     * Shows [count] hint ads back to back. [onProgress] reports (shown, count). [onComplete] gets
     * true when all were watched, or false if the player closed one early. If ads are removed or
     * unavailable it succeeds anyway, so play never stalls on a missing ad.
     */
    fun showRewardedAds(
        activity: Activity,
        count: Int,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onComplete: (Boolean) -> Unit,
    ) {
        if (adsRemoved || count <= 0) { onComplete(true); return }
        fun step(done: Int) {
            if (done >= count) { onComplete(true); return }
            hintSlot.show(activity, strict = false) { earned ->
                if (earned) { onProgress(done + 1, count); step(done + 1) } else onComplete(false)
            }
        }
        step(0)
    }

    // ---- Coins: strict (no ad, no coins) ----------------------------------------------------

    val freeCoinsReady: Boolean get() = freeCoinsSlot.ready
    val doubleCoinsReady: Boolean get() = doubleCoinsSlot.ready

    /** [onResult] is true only if a real ad was watched to the end. */
    fun showFreeCoinsAd(activity: Activity, onResult: (Boolean) -> Unit) =
        freeCoinsSlot.show(activity, strict = true, onResult)

    fun showDoubleCoinsAd(activity: Activity, onResult: (Boolean) -> Unit) =
        doubleCoinsSlot.show(activity, strict = true, onResult)

    // ---- Replay interstitial ----------------------------------------------------------------

    /** Shows the replay ad if one is ready, then calls [onDone]. Never blocks starting the replay. */
    fun showReplayAd(activity: Activity, onDone: () -> Unit) {
        if (adsRemoved) { onDone(); return }
        replaySlot.show(activity, onDone)
    }

    // ---- Slots ------------------------------------------------------------------------------

    private inner class RewardedSlot(private val unitId: String) {
        private var ad: RewardedAd? = null
        private var loading = false
        val ready: Boolean get() = ad != null

        fun load() {
            if (adsRemoved || loading || ad != null) return
            loading = true
            RewardedAd.load(appContext, unitId, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(loaded: RewardedAd) { ad = loaded; loading = false }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded $unitId failed to load: ${error.message}")
                    ad = null; loading = false
                }
            })
        }

        /**
         * Shows the ad; [onResult] gets true if the reward was earned.
         * strict = false: a missing or failed ad counts as earned (hints must never block play).
         * strict = true:  a missing or failed ad counts as not earned (coins need a real ad).
         */
        fun show(activity: Activity, strict: Boolean, onResult: (Boolean) -> Unit) {
            if (adsRemoved) { onResult(!strict); return }
            val current = ad
            if (current == null) { load(); onResult(!strict); return }
            var earned = false
            current.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    ad = null; load(); onResult(earned)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    ad = null; load(); onResult(!strict)
                }
            }
            current.show(activity) { earned = true }
        }
    }

    private inner class InterstitialSlot(private val unitId: String) {
        private var ad: InterstitialAd? = null
        private var loading = false

        fun load() {
            if (adsRemoved || loading || ad != null) return
            loading = true
            InterstitialAd.load(appContext, unitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) { ad = loaded; loading = false }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial $unitId failed to load: ${error.message}")
                    ad = null; loading = false
                }
            })
        }

        fun show(activity: Activity, onDone: () -> Unit) {
            val current = ad
            if (current == null) { load(); onDone(); return }
            current.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { ad = null; load(); onDone() }
                override fun onAdFailedToShowFullScreenContent(error: AdError) { ad = null; load(); onDone() }
            }
            current.show(activity)
        }
    }

    private companion object {
        const val TAG = "AdManager"
    }
}
