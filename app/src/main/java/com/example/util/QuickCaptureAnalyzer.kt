package com.example.util

object QuickCaptureAnalyzer {

    private val urlRegex = Regex("(https?://\\S+)")
    private val waTimestampRegex = Regex("\\[?\\d{1,2}/\\d{1,2}/\\d{2,4},?\\s*\\d{1,2}:\\d{2}")

    /** First URL found in the pasted text, or empty string. */
    fun extractLink(text: String): String = urlRegex.find(text)?.value?.trimEnd('.', ',', ')') ?: ""

    /** First non-blank line, trimmed to a sane title length. */
    fun deriveTitle(text: String, linkUrl: String): String {
        val firstLine = text.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        if (firstLine.isBlank()) return "Untitled"
        if (firstLine == linkUrl) {
            // Pure link paste: use the host as a friendlier title.
            val host = runCatching { java.net.URI(linkUrl).host }.getOrNull()
            return host?.removePrefix("www.") ?: firstLine.take(50)
        }
        return if (firstLine.length > 60) firstLine.take(60) + "…" else firstLine
    }

    /** Best-effort category guess so the user rarely needs to pick one manually. */
    fun detectCategory(text: String): String {
        val trimmed = text.trim()
        return when {
            trimmed.isNotBlank() && urlRegex.matches(trimmed) -> "Links"
            waTimestampRegex.containsMatchIn(trimmed) ||
                trimmed.contains("<Media omitted>") ||
                trimmed.startsWith("Forwarded", ignoreCase = true) -> "WhatsApp"
            else -> "Notes"
        }
    }
}
