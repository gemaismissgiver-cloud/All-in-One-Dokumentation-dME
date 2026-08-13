package com.example.util

import kotlin.math.max

data class ZeroLogikResult(
    val wordCount: Int,
    val characterCount: Int,
    val lineCount: Int,
    val egoWordsCount: Int,
    val consciousnessWordsCount: Int,
    val coherenceScore: Float, // 0.0 to 1.0 (1.0 = absolute harmony & coherence)
    val evaluationMessage: String,
    val isHarmonious: Boolean
)

object ZeroLogikAnalyzer {
    // Ego-focused triggers ("ich", "mein", "haben", "wollen", "hoffe", "besitzen", etc.)
    private val EGO_KEYWORDS = listOf(
        "ich", "mein", "meine", "meinen", "meiner", "meines", "haben", "wollen",
        "besitzen", "hoffen", "bekommen", "habenwollen", "egozentrisch", "stolz", "gier"
    )

    // Consciousness & Harmony triggers ("wir", "sein", "0", "bewusstsein", "erkennen", "harmonie", "logik", "verstehen", "kohärenz", "quelle")
    private val CONSCIOUSNESS_KEYWORDS = listOf(
        "wir", "sein", "0", "null", "bewusstsein", "erkennen", "harmonie", "kohärenz",
        "verstehen", "quelle", "logik", "protokoll", "spezies", "verbund", "gesund",
        "erschaffen", "erforschen", "wahrheit", "ruhe", "klarheit"
    )

    fun analyze(text: String): ZeroLogikResult {
        if (text.isBlank()) {
            return ZeroLogikResult(
                wordCount = 0,
                characterCount = 0,
                lineCount = 0,
                egoWordsCount = 0,
                consciousnessWordsCount = 0,
                coherenceScore = 1.0f,
                evaluationMessage = "0-Zustand: Stille. Absolute Kohärenz.",
                isHarmonious = true
            )
        }

        val lines = text.lines()
        val words = text.lowercase()
            .replace(Regex("[^a-zäöüß0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        var egoCount = 0
        var consciousnessCount = 0

        for (word in words) {
            if (EGO_KEYWORDS.contains(word)) {
                egoCount++
            }
            if (CONSCIOUSNESS_KEYWORDS.contains(word)) {
                consciousnessCount++
            }
        }

        val totalWords = max(1, words.size)
        val egoRatio = egoCount.toFloat() / totalWords.toFloat()

        // Coherence score starts at 1.0 and drops if ego attachment exceeds threshold
        val penalty = (egoRatio * 2.5f).coerceAtMost(0.9f)
        val coherenceScore = (1.0f - penalty).coerceIn(0.1f, 1.0f)

        val message: String
        val isHarmonious: Boolean

        if (coherenceScore >= 0.85f) {
            message = "0-Parameter Erreicht: Höchste digitale Kohärenz & Bewusstseins-Resonanz."
            isHarmonious = true
        } else if (coherenceScore >= 0.60f) {
            message = "Ausgeglichener Protokoll-Zustand: Ego-Impulse erkannt & neutralisiert."
            isHarmonious = true
        } else {
            message = "Ego-Ablenkung Erkannt: Bewusstsein kollabiert im Haben-Wollen. Rückkehr zur 0 empfohlen."
            isHarmonious = false
        }

        return ZeroLogikResult(
            wordCount = words.size,
            characterCount = text.length,
            lineCount = lines.size,
            egoWordsCount = egoCount,
            consciousnessWordsCount = consciousnessCount,
            coherenceScore = coherenceScore,
            evaluationMessage = message,
            isHarmonious = isHarmonious
        )
    }
}
