package com.georgewitte.placard.logic

import kotlin.math.pow

/** A sign color scheme. Colors are ARGB ints so this file stays free of Android types. */
data class SignStyle(val name: String, val background: Int, val foreground: Int)

object SignStyles {
    val ALL = listOf(
        SignStyle("Classic", 0xFF000000.toInt(), 0xFFFFFFFF.toInt()),
        SignStyle("Paper", 0xFFFFFFFF.toInt(), 0xFF111111.toInt()),
        SignStyle("Highlighter", 0xFFFFD60A.toInt(), 0xFF111111.toInt()),
        SignStyle("Alert", 0xFFD50000.toInt(), 0xFFFFFFFF.toInt()),
        SignStyle("Ocean", 0xFF0047AB.toInt(), 0xFFFFFFFF.toInt()),
        SignStyle("LED", 0xFF000000.toInt(), 0xFF39FF14.toInt()),
    )

    fun at(index: Int): SignStyle = ALL[index.coerceIn(0, ALL.lastIndex)]

    /** WCAG 2 contrast ratio between two opaque colors (1.0 to 21.0). */
    fun contrast(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun luminance(color: Int): Double {
        fun channel(shift: Int): Double {
            val c = ((color shr shift) and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
}
