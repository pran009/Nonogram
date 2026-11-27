package app.nonogram.puzzle.ads

import app.nonogram.puzzle.BuildConfig

/**
 * Central place for AdMob ad unit ids.
 *
 * Debug builds always use Google's OFFICIAL TEST ids, which always show test ads and are safe to
 * tap. Release builds use the real ids from the AdMob console. Never tap your own real ads —
 * AdMob treats that as invalid traffic.
 */
object AdIds {
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

    // Real ad unit ids from the AdMob console. Used by release builds only.
    private const val releaseBanner = "ca-app-pub-4687296311300202/5492832921"
    private const val releaseHint = "ca-app-pub-4687296311300202/6381524138"       // "Rewarded"
    private const val releaseFreeCoins = "ca-app-pub-4687296311300202/5941634844"  // "Free coins"
    private const val releaseDoubleCoins = "ca-app-pub-4687296311300202/2761258532" // "Double coins"
    private const val releaseReplay = "ca-app-pub-4687296311300202/9135095191"     // "Replay"

    private fun pick(test: String, real: String) = if (BuildConfig.DEBUG) test else real

    val banner: String get() = pick(TEST_BANNER, releaseBanner)
    val hint: String get() = pick(TEST_REWARDED, releaseHint)
    val freeCoins: String get() = pick(TEST_REWARDED, releaseFreeCoins)
    val doubleCoins: String get() = pick(TEST_REWARDED, releaseDoubleCoins)
    val replay: String get() = pick(TEST_INTERSTITIAL, releaseReplay)
}
