package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocol_entries")
data class ProtocolEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // e.g. "2026-08-13"
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val chapter: String, // Matches ProtocolChapter.id
    val audioPath: String? = null,
    val audioDurationMs: Long = 0,
    val imageUri: String? = null,
    val documentUri: String? = null,
    val documentName: String? = null,
    val webUrl: String? = null,
    val webTitle: String? = null,
    val coherenceScore: Float = 1.0f, // 0-Logik coherence parameter (0.0 to 1.0)
    val isPublicProtocol: Boolean = false
)
