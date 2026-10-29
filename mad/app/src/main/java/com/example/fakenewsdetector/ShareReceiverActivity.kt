package com.example.fakenewsdetector

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Invisible entry point for the Android share sheet.
 * It grabs the shared text, hands it to MainActivity, and closes itself.
 */
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else null

        val open = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_SHARED_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(open)
        finish()
    }
}
