package com.georgewitte.placard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.georgewitte.placard.logic.SignStyles
import com.georgewitte.placard.ui.BigTextView

/** The full-screen sign. System Back (or the back gesture) closes it. */
class DisplayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val message = intent.getStringExtra(EXTRA_TEXT).orEmpty()
        if (message.isBlank()) {
            finish()
            return
        }

        val sign = BigTextView(this).apply {
            style = SignStyles.at(intent.getIntExtra(EXTRA_STYLE, 0))
            ticker = intent.getBooleanExtra(EXTRA_TICKER, false)
            text = message
            keepScreenOn = true
        }
        setContentView(sign)

        if (intent.getBooleanExtra(EXTRA_MAX_BRIGHTNESS, true)) {
            window.attributes = window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            }
        }
        hideSystemBars()
        if (savedInstanceState == null) {
            sign.showHint(getString(if (sign.ticker) R.string.display_hint_ticker else R.string.display_hint))
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                @Suppress("DEPRECATION") // Android 15+ is always edge to edge.
                window.setDecorFitsSystemWindows(false)
            }
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    }

    companion object {
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_STYLE = "style"
        private const val EXTRA_TICKER = "ticker"
        private const val EXTRA_MAX_BRIGHTNESS = "max_brightness"

        fun intent(context: Context, text: String, styleIndex: Int, ticker: Boolean, maxBrightness: Boolean): Intent =
            Intent(context, DisplayActivity::class.java)
                .putExtra(EXTRA_TEXT, text)
                .putExtra(EXTRA_STYLE, styleIndex)
                .putExtra(EXTRA_TICKER, ticker)
                .putExtra(EXTRA_MAX_BRIGHTNESS, maxBrightness)
    }
}
