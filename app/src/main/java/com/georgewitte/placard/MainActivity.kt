package com.georgewitte.placard

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.georgewitte.placard.data.PlacardStore
import com.georgewitte.placard.logic.Phrases
import com.georgewitte.placard.logic.SignStyles
import com.georgewitte.placard.ui.dp
import com.georgewitte.placard.ui.roundedRect
import com.georgewitte.placard.ui.selectableForeground
import com.georgewitte.placard.ui.setUpEdgeToEdge

/** The composer: type a message (or pick a saved one), choose a look, then show it full screen. */
class MainActivity : Activity() {

    private lateinit var store: PlacardStore
    private lateinit var messageInput: EditText
    private lateinit var styleRow: LinearLayout
    private lateinit var modeFit: TextView
    private lateinit var modeTicker: TextView
    private lateinit var brightness: CheckBox
    private lateinit var phraseList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpEdgeToEdge(findViewById(R.id.root))
        store = PlacardStore(this)

        messageInput = findViewById(R.id.message)
        styleRow = findViewById(R.id.style_row)
        modeFit = findViewById(R.id.mode_fit)
        modeTicker = findViewById(R.id.mode_ticker)
        brightness = findViewById(R.id.max_brightness)
        phraseList = findViewById(R.id.phrase_list)

        if (savedInstanceState == null) messageInput.setText(store.draft)
        brightness.isChecked = store.maxBrightness
        brightness.setOnCheckedChangeListener { _, checked -> store.maxBrightness = checked }

        modeFit.setOnClickListener { setTicker(false) }
        modeTicker.setOnClickListener { setTicker(true) }
        findViewById<View>(R.id.show).setOnClickListener { showSign(messageInput.text.toString()) }
        findViewById<View>(R.id.save_phrase).setOnClickListener { savePhrase() }

        buildStyleSwatches()
        renderMode()
        renderPhrases()
    }

    override fun onPause() {
        store.draft = messageInput.text.toString()
        super.onPause()
    }

    private fun showSign(raw: String) {
        val text = Phrases.clean(raw)
        if (text.isEmpty()) {
            messageInput.error = getString(R.string.error_empty)
            messageInput.requestFocus()
            return
        }
        startActivity(DisplayActivity.intent(this, text, store.styleIndex, store.ticker, store.maxBrightness))
    }

    private fun savePhrase() {
        val text = Phrases.clean(messageInput.text.toString())
        if (text.isEmpty()) {
            messageInput.error = getString(R.string.error_empty)
            return
        }
        store.phrases = Phrases.save(store.phrases, text)
        renderPhrases()
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
    }

    private fun setTicker(ticker: Boolean) {
        store.ticker = ticker
        renderMode()
    }

    private fun renderMode() {
        stylePill(modeFit, selected = !store.ticker)
        stylePill(modeTicker, selected = store.ticker)
    }

    private fun stylePill(view: TextView, selected: Boolean) {
        val radius = dp(22).toFloat()
        view.isSelected = selected
        view.background = if (selected) {
            roundedRect(getColor(R.color.brand), radius)
        } else {
            roundedRect(getColor(R.color.surface), radius, getColor(R.color.outline), dp(1.5f))
        }
        view.setTextColor(getColor(if (selected) R.color.on_brand else R.color.text))
    }

    private fun buildStyleSwatches() {
        styleRow.removeAllViews()
        SignStyles.ALL.forEachIndexed { index, style ->
            val swatch = TextView(this).apply {
                text = getString(R.string.swatch_sample)
                textSize = 22f
                gravity = Gravity.CENTER
                typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
                setTextColor(style.foreground)
                contentDescription = getString(R.string.style_description, style.name)
                foreground = selectableForeground()
                setOnClickListener {
                    store.styleIndex = index
                    renderSwatches()
                }
            }
            val size = dp(64)
            styleRow.addView(swatch, LinearLayout.LayoutParams(size, size).apply { marginEnd = dp(10) })
        }
        renderSwatches()
    }

    private fun renderSwatches() {
        val selected = store.styleIndex.coerceIn(0, SignStyles.ALL.lastIndex)
        for (i in 0 until styleRow.childCount) {
            val style = SignStyles.ALL[i]
            val view = styleRow.getChildAt(i)
            view.isSelected = i == selected
            view.background = if (i == selected) {
                roundedRect(style.background, dp(16).toFloat(), getColor(R.color.selection), dp(4))
            } else {
                roundedRect(style.background, dp(16).toFloat(), getColor(R.color.outline), dp(1))
            }
        }
    }

    private fun renderPhrases() {
        phraseList.removeAllViews()
        val phrases = store.phrases
        findViewById<View>(R.id.phrases_empty).visibility = if (phrases.isEmpty()) View.VISIBLE else View.GONE
        for (phrase in phrases) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = roundedRect(getColor(R.color.surface), dp(16).toFloat(), getColor(R.color.outline), dp(1))
            }
            val label = TextView(this).apply {
                text = phrase
                textSize = 17f
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(getColor(R.color.text))
                setPadding(dp(16), dp(14), dp(8), dp(14))
                foreground = selectableForeground()
                isClickable = true
                isFocusable = true
                contentDescription = getString(R.string.phrase_show_description, phrase)
                setOnClickListener { showSign(phrase) }
                setOnLongClickListener {
                    messageInput.setText(phrase)
                    messageInput.setSelection(messageInput.text.length)
                    messageInput.requestFocus()
                    Toast.makeText(this@MainActivity, R.string.loaded_into_editor, Toast.LENGTH_SHORT).show()
                    true
                }
            }
            val remove = TextView(this).apply {
                text = getString(R.string.remove_symbol)
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(getColor(R.color.text_muted))
                contentDescription = getString(R.string.phrase_remove_description, phrase)
                foreground = selectableForeground()
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    store.phrases = Phrases.remove(store.phrases, phrase)
                    renderPhrases()
                }
            }
            row.addView(label, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(remove, LinearLayout.LayoutParams(dp(52), dp(52)))
            phraseList.addView(
                row,
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .apply { bottomMargin = dp(8) },
            )
        }
    }
}
