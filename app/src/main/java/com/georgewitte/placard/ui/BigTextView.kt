package com.georgewitte.placard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.SystemClock
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.georgewitte.placard.logic.SignStyle
import com.georgewitte.placard.logic.SignStyles
import com.georgewitte.placard.logic.TextFit

/**
 * Draws the sign. In "fit" mode the text is sized to the largest font that fits the screen without
 * splitting a word; in ticker mode one huge line scrolls right to left.
 *
 * Single tap pauses the ticker. Double tap flips the sign 180°, so you can show it to someone
 * across a table without turning the phone around.
 */
@SuppressLint("ViewConstructor")
class BigTextView(context: Context) : View(context) {

    var text: String = ""
        set(value) {
            field = value
            contentDescription = value
            relayout()
        }

    var style: SignStyle = SignStyles.at(0)
        set(value) {
            field = value
            paint.color = value.foreground
            invalidate()
        }

    var ticker: Boolean = false
        set(value) {
            field = value
            relayout()
        }

    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
        color = this@BigTextView.style.foreground
    }
    private val hintPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
    }

    private var layout: StaticLayout? = null
    private var tickerText = ""
    private var tickerWidth = 0f
    private var offset = 0f
    private var lastFrameNanos = 0L
    private var paused = false
    private var flipped = false

    private var hintText = ""
    private var hintUntil = 0L

    private val gestures = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            performClick()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            flipped = !flipped
            rotation = if (flipped) 180f else 0f
            return true
        }
    })

    /** Shows a small, fading line of help text at the bottom of the sign. */
    fun showHint(message: String, durationMs: Long = 3_000L) {
        hintText = message
        hintUntil = SystemClock.uptimeMillis() + durationMs
        postInvalidateOnAnimation()
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (ticker) {
            paused = !paused
            lastFrameNanos = 0L
            invalidate()
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility") // Single taps are routed through performClick().
    override fun onTouchEvent(event: MotionEvent): Boolean = gestures.onTouchEvent(event) || super.onTouchEvent(event)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        relayout()
    }

    private fun relayout() {
        if (width == 0 || height == 0) return
        val availableWidth = (width * (1 - 2 * PADDING)).toInt().coerceAtLeast(1)
        val availableHeight = (height * (1 - 2 * PADDING)).toInt().coerceAtLeast(1)

        if (ticker) {
            layout = null
            tickerText = TextFit.tickerLine(text)
            paint.textSize = availableHeight * TICKER_HEIGHT_FRACTION
            tickerWidth = paint.measureText(tickerText)
            offset = 0f
            lastFrameNanos = 0L
        } else {
            val words = TextFit.words(text)
            val size = TextFit.largestFitting(MIN_TEXT_PX, availableHeight.toFloat()) { candidate ->
                paint.textSize = candidate
                words.all { paint.measureText(it) <= availableWidth } &&
                    buildLayout(availableWidth).height <= availableHeight
            }
            paint.textSize = size
            layout = buildLayout(availableWidth)
        }
        invalidate()
    }

    private fun buildLayout(width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .setBreakStrategy(Layout.BREAK_STRATEGY_BALANCED)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
            .build()

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(style.background)
        if (ticker) drawTicker(canvas) else drawFitted(canvas)
        drawHint(canvas)
    }

    private fun drawFitted(canvas: Canvas) {
        val l = layout ?: return
        canvas.save()
        canvas.translate((width - l.width) / 2f, (height - l.height) / 2f)
        l.draw(canvas)
        canvas.restore()
    }

    private fun drawTicker(canvas: Canvas) {
        val metrics = paint.fontMetrics
        val baseline = height / 2f - (metrics.ascent + metrics.descent) / 2f
        canvas.drawText(tickerText, width - offset, baseline, paint)
        if (paused) return

        val now = System.nanoTime()
        if (lastFrameNanos != 0L) {
            val seconds = (now - lastFrameNanos) / 1_000_000_000f
            offset += paint.textSize * TICKER_SPEED * seconds
            if (width - offset + tickerWidth < 0) offset = 0f
        }
        lastFrameNanos = now
        postInvalidateOnAnimation()
    }

    private fun drawHint(canvas: Canvas) {
        val remaining = hintUntil - SystemClock.uptimeMillis()
        if (remaining <= 0 || hintText.isEmpty()) return
        hintPaint.color = style.foreground
        hintPaint.alpha = (180 * (remaining.coerceAtMost(500L) / 500f)).toInt()
        val x = (width - hintPaint.measureText(hintText)) / 2f
        canvas.drawText(hintText, x, height - height * PADDING / 2f, hintPaint)
        postInvalidateOnAnimation()
    }

    private companion object {
        const val PADDING = 0.05f
        const val MIN_TEXT_PX = 12f
        const val TICKER_HEIGHT_FRACTION = 0.7f

        /** Ticker speed, in text-heights per second. */
        const val TICKER_SPEED = 1.6f
    }
}
