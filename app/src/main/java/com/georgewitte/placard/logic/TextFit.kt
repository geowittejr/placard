package com.georgewitte.placard.logic

/** Pure helpers behind the auto-sizing sign, kept free of Android types so they can be unit tested. */
object TextFit {

    /**
     * Binary-searches the largest size in [min]..[max] for which [fits] returns true.
     * Assumes [fits] is monotonic: if a size fits, every smaller size fits too.
     */
    fun largestFitting(min: Float, max: Float, precision: Float = 1f, fits: (Float) -> Boolean): Float {
        if (max <= min || !fits(min)) return min
        if (fits(max)) return max
        var lo = min
        var hi = max
        while (hi - lo > precision) {
            val mid = (lo + hi) / 2f
            if (fits(mid)) lo = mid else hi = mid
        }
        return lo
    }

    /** Each word must fit on one line, so the sign never splits a word in half. */
    fun words(text: String): List<String> = text.split(Regex("\\s+")).filter { it.isNotEmpty() }

    /** The scrolling ticker shows one line: line breaks become a dot separator. */
    fun tickerLine(text: String): String =
        text.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("   •   ")
}
