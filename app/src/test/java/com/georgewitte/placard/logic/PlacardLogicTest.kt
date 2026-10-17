package com.georgewitte.placard.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacardLogicTest {

    @Test
    fun largestFittingFindsTheThreshold() {
        // Pretend anything up to 137.4 px fits.
        val size = TextFit.largestFitting(10f, 1000f) { it <= 137.4f }
        assertTrue("got $size", size <= 137.4f && size > 136.4f)
    }

    @Test
    fun largestFittingEdgeCases() {
        assertEquals(1000f, TextFit.largestFitting(10f, 1000f) { true })
        assertEquals(10f, TextFit.largestFitting(10f, 1000f) { false }) // nothing fits: fall back to min
        assertEquals(10f, TextFit.largestFitting(10f, 5f) { true }) // empty range
    }

    @Test
    fun wordsAndTicker() {
        assertEquals(listOf("Over", "here!", "👋"), TextFit.words("  Over here!\n👋 "))
        assertEquals("Line one   •   Line two", TextFit.tickerLine("Line one\n\n  Line two  "))
        assertEquals("", TextFit.tickerLine("   "))
    }

    @Test
    fun savingPhrasesDedupesAndCaps() {
        var saved = listOf("Thank you!", "Be right back")
        saved = Phrases.save(saved, "  be right BACK ")
        assertEquals(listOf("be right BACK", "Thank you!"), saved)
        assertEquals(saved, Phrases.save(saved, "   "))

        var many = emptyList<String>()
        repeat(Phrases.MAX_SAVED + 5) { many = Phrases.save(many, "Phrase $it") }
        assertEquals(Phrases.MAX_SAVED, many.size)
        assertEquals("Phrase ${Phrases.MAX_SAVED + 4}", many.first())

        assertEquals(listOf("Thank you!"), Phrases.remove(saved, "be right BACK"))
        assertEquals(Phrases.MAX_LENGTH, Phrases.clean("x".repeat(500)).length)
        assertEquals("Two\nlines", Phrases.clean("  Two\nlines \n"))
    }

    @Test
    fun everyStyleIsReadable() {
        for (style in SignStyles.ALL) {
            val ratio = SignStyles.contrast(style.background, style.foreground)
            assertTrue("${style.name} contrast $ratio", ratio >= 4.5)
        }
        assertEquals(21.0, SignStyles.contrast(0xFF000000.toInt(), 0xFFFFFFFF.toInt()), 0.01)
        assertEquals(SignStyles.ALL.first(), SignStyles.at(-3))
        assertEquals(SignStyles.ALL.last(), SignStyles.at(99))
    }
}
