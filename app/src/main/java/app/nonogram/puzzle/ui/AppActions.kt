package app.nonogram.puzzle.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.nonogram.puzzle.BuildConfig

/** Release package id; used for store links even from the ".debug" build. */
private const val STORE_PACKAGE = "app.nonogram.puzzle"

/** Support address shown to players for feedback. Same as the Play Store contact email. */
private const val FEEDBACK_EMAIL = "pran009@gmail.com"

const val STORE_URL = "https://play.google.com/store/apps/details?id=$STORE_PACKAGE"

fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** Opens the system share sheet with plain text. */
fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share via"))
}

/** Opens an email draft to the developer with the app version pre-filled. */
fun sendFeedback(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "Nonogram feedback (v${BuildConfig.VERSION_NAME})")
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No email app found. Write to $FEEDBACK_EMAIL", Toast.LENGTH_LONG).show()
    }
}

/** Opens the app's Play Store page so the player can rate it. */
fun openStorePage(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$STORE_PACKAGE")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(STORE_URL)))
    }
}
