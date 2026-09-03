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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Owns everything AdMob. One instance lives for the whole app (created in MainActivity).
 *
 * Ads are shown only when [adsRemoved] is false, so the future "Remove ads" purchase is a single
 * flag flip: set [ProgressStore.adsRemoved] = true and every ad in the app disappears.
 */
class AdManager(context: Context, private val store: ProgressStore) {

    private val appContext = context.applicationContext

    /** Compose-observable so the banner recomposes away the instant ads are removed. */
    var adsRemoved by mutableStateOf(store.adsRemoved)
        private set

    private var rewardedAd: RewardedAd? = null
    private var loadingRewarded = false

    fun initialize() {
        MobileAds.initialize(appContext) {}
        loadRewarded()
    }

    /** Called by the future "Remove ads" purchase to hide all ads immediately. */
    fun applyAdsRemoved(removed: Boolean) {
        store.adsRemoved = removed
        adsRemoved = removed
    }

    // ---- Rewarded ad for hints --------------------------------------------------------------

    private fun loadRewarded() {
        if (adsRemoved || loadingRewarded || rewardedAd != null) return
        loadingRewarded = true
        RewardedAd.load(
            appContext,
            AdIds.rewarded,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    loadingRewarded = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded failed to load: ${error.message}")
                    rewardedAd = null
                    loadingRewarded = false
                }
            },
        )
    }

    /** True if a rewarded ad is ready to show right now. */
    val rewardedReady: Boolean get() = rewardedAd != null

    /**
     * Shows the rewarded ad. [onReward] runs only if the user earns the reward (watches enough).
     * If no ad is available, [onReward] is called immediately so the player is never blocked by a
     * failed ad load, then a fresh ad is requested for next time.
     */
    fun showRewardedForHint(activity: Activity, onReward: () -> Unit) {
        if (adsRemoved) { onReward(); return }
        val ad = rewardedAd
        if (ad == null) {
            onReward()
            loadRewarded()
            return
        }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
                if (earned) onReward()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded()
                onReward() // don't punish the player for an ad failure
            }
        }
        ad.show(activity) { earned = true }
    }

    /**
     * Shows [count] rewarded ads back to back (used for escalating Challenge hints).
     * [onProgress] reports (shownSoFar, count) as each ad completes; [onComplete] runs once all
     * are done. If ads are removed or unavailable, it completes immediately so play never stalls.
     */
    fun showRewardedAds(
        activity: Activity,
        count: Int,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onComplete: () -> Unit,
    ) {
        if (adsRemoved || count <= 0) { onComplete(); return }
        fun step(done: Int) {
            if (done >= count) { onComplete(); return }
            showRewardedForHint(activity) {
                onProgress(done + 1, count)
                step(done + 1)
            }
        }
        step(0)
    }

    private companion object {
        const val TAG = "AdManager"
    }
}
