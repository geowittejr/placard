package com.georgewitte.placard.logic

/** Saved-phrase list rules. Newest first, no duplicates (ignoring case), capped in size. */
object Phrases {
    const val MAX_SAVED = 30
    const val MAX_LENGTH = 280

    val DEFAULTS = listOf(
        "Thank you!",
        "Over here! 👋",
        "One moment, please",
        "I can't hear you.\nCan you type it?",
        "Welcome home!",
        "Be right back",
    )

    /** Trims the ends but keeps line breaks the user typed on purpose. */
    fun clean(raw: String): String = raw.trim().take(MAX_LENGTH)

    fun save(saved: List<String>, phrase: String): List<String> {
        val clean = clean(phrase)
        if (clean.isEmpty()) return saved
        return (listOf(clean) + saved.filterNot { it.equals(clean, ignoreCase = true) }).take(MAX_SAVED)
    }

    fun remove(saved: List<String>, phrase: String): List<String> = saved.filterNot { it == phrase }
}
