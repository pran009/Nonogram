package app.nonogram.puzzle.ads

import app.nonogram.puzzle.BuildConfig

/**
 * Central place for AdMob ad unit ids.
 *
 * The values below are Google's OFFICIAL TEST ids. They always show test ads and are safe to
 * click during development. You must NEVER ship real ids while testing, and you must NEVER click
 * your own real ads — AdMob will ban the account.
 *
 * Before publishing:
 *   1. Create an app in https://apps.admob.com and get your real Application id.
 *      Put it in AndroidManifest.xml (com.google.android.gms.ads.APPLICATION_ID).
 *   2. Create a Banner ad unit and a Rewarded ad unit, and paste their ids into
 *      [releaseBanner] and [releaseRewarded] below.
 *
 * Debug builds always use the test ids. Release builds use your real ids.
 */
object AdIds {
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

    // Real ad unit ids from the AdMob console. Used by release builds only.
    private const val releaseBanner = "ca-app-pub-4687296311300202/5492832921"
    private const val releaseRewarded = "ca-app-pub-4687296311300202/6381524138"

    val banner: String get() = if (BuildConfig.DEBUG) TEST_BANNER else releaseBanner
    val rewarded: String get() = if (BuildConfig.DEBUG) TEST_REWARDED else releaseRewarded
}
