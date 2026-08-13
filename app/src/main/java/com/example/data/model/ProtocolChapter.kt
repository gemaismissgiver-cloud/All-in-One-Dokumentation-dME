package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ProtocolChapter(
    val id: String,
    val title: String,
    val description: String,
    val color: Color
) {
    TUN("TUN", "Täglich Tun", "Was ich aktiv tue und umsetze", ChapterTunColor),
    DENKEN("DENKEN", "Denken & Geist", "Gedanken, Reflexionen & Zustand", ChapterDenkenColor),
    ERKENNEN("ERKENNEN", "Erkennen", "Erkenntnisse & Durchbrüche", ChapterErkennenColor),
    ERFINDEN("ERFINDEN", "Erfinden", "Konzepte, Ideen & Schöpfungen", ChapterErfindenColor),
    ERFORSCHEN("ERFORSCHEN", "Erforschen", "Recherchen, Analyse & Fragen", ChapterErforschenColor),
    WUENSTCHEN("WUENSTCHEN", "Wünschen Wollen", "Intentionen, Impulse & Sehnsüchte", ChapterWuenstenColor),
    UNMOEGLICH("UNMOEGLICH", "Das Unmögliche", "Möglich machen was unmöglich schien", ChapterUnmoeglichColor);

    companion object {
        fun fromId(id: String): ProtocolChapter {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: DENKEN
        }
    }
}
