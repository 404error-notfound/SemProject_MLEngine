package com.example.fakenewsdetector

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Invisible entry point for the Android share sheet.
 * It grabs the shared text, resolves which app it came from, and opens the
 * quick-check sheet over that app.
 */
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = if (intent?.action == Intent.ACTION_SEND) {
            intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        } else {
            null
        }

        if (text.isNullOrBlank()) {
            finish()
            return
        }

        val open = Intent(this, QuickCheckActivity::class.java).apply {
            putExtra(EXTRA_SHARED_TEXT, text)
            putExtra(EXTRA_SOURCE_APP, resolveSourceLabel())
        }
        startActivity(open)
        finish()
    }

    /** Best-effort label of the app the share came from ("WhatsApp"). */
    private fun resolveSourceLabel(): String? {
        // Share-sheet referrers look like android-app://com.whatsapp.
        val referrerPackage = referrer
            ?.takeIf { it.scheme == "android-app" }
            ?.host
            ?: return null
        return runCatching {
            val info = packageManager.getApplicationInfo(referrerPackage, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrNull()
    }

    companion object {
        const val EXTRA_SHARED_TEXT = "shared_text"
        const val EXTRA_SOURCE_APP = "source_app"
    }
}
