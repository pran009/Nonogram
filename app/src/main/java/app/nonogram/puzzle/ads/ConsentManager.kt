package app.nonogram.puzzle.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Google's User Messaging Platform (UMP): asks players in regions that require it (EEA, UK,
 * Switzerland, some US states) for ad consent before any ad is requested, and offers a
 * "Privacy settings" entry so they can change their choice later.
 *
 * Everywhere else no form is shown and ads start straight away.
 */
class ConsentManager(context: Context) {

    private val info: ConsentInformation = UserMessagingPlatform.getConsentInformation(context.applicationContext)

    /** True when the player must be offered a way to revisit their choice (Settings shows a row). */
    var privacyOptionsRequired by mutableStateOf(false)
        private set

    /** Whether ads may be requested right now (consent gathered, or not needed). */
    val canRequestAds: Boolean get() = info.canRequestAds()

    /**
     * Refreshes the consent status and shows the consent form if one is required. [onDone] runs
     * once on the main thread when this finishes, whatever the outcome; check [canRequestAds].
     */
    fun gather(activity: Activity, onDone: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()
        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) Log.w(TAG, "Consent form: ${error.message}")
                    refresh()
                    onDone()
                }
            },
            { error ->
                // Offline or similar: fall back to the last known status.
                Log.w(TAG, "Consent info update failed: ${error.message}")
                refresh()
                onDone()
            },
        )
    }

    /** Opens Google's privacy options form so the player can change their consent. */
    fun showPrivacyOptions(activity: Activity, onDone: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w(TAG, "Privacy options: ${error.message}")
            refresh()
            onDone()
        }
    }

    private fun refresh() {
        privacyOptionsRequired =
            info.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    init { refresh() }

    private companion object {
        const val TAG = "Consent"
    }
}
