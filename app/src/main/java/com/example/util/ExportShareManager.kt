package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.ProtocolEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportShareManager {

    enum class ExportFormat {
        TEXT, MARKDOWN, JSON, HTML
    }

    fun shareEntryText(context: Context, entry: ProtocolEntry) {
        val shareText = buildString {
            append("--- PROTOKOLL 0 ---\n")
            append("Kapitel: ${entry.chapter}\n")
            append("Datum: ${entry.dateStr}\n")
            append("Titel: ${entry.title}\n\n")
            append(entry.content)
            if (!entry.webUrl.isNull_or_blank()) {
                append("\n\nWeb-Link: ${entry.webUrl}")
            }
            if (!entry.documentName.isNull_or_blank()) {
                append("\nDokument: ${entry.documentName}")
            }
            append("\n\n0-Logik Kohärenz Index: ${String.format(Locale.US, "%.2f", entry.coherenceScore)}")
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Protokoll 0: ${entry.title}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Protokoll Teilen")
        context.startActivity(shareIntent)
    }

    fun exportAndShareEntries(
        context: Context,
        entries: List<ProtocolEntry>,
        format: ExportFormat,
        filenamePrefix: String = "Protokoll_0"
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val extension = when (format) {
                ExportFormat.TEXT -> "txt"
                ExportFormat.MARKDOWN -> "md"
                ExportFormat.JSON -> "json"
                ExportFormat.HTML -> "html"
            }

            val file = File(exportDir, "${filenamePrefix}_$timestamp.$extension")
            val content = when (format) {
                ExportFormat.TEXT -> generateTextExport(entries)
                ExportFormat.MARKDOWN -> generateMarkdownExport(entries)
                ExportFormat.JSON -> generateJsonExport(entries)
                ExportFormat.HTML -> generateHtmlExport(entries)
            }

            file.writeText(content, Charsets.UTF_8)

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mimeType = when (format) {
                ExportFormat.TEXT -> "text/plain"
                ExportFormat.MARKDOWN -> "text/markdown"
                ExportFormat.JSON -> "application/json"
                ExportFormat.HTML -> "text/html"
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Protokoll 0 Export ($timestamp)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Dokument/Protokoll Veröffentlichen"))
        } catch (e: Exception) {
            Log.e("ExportShareManager", "Error exporting entries", e)
        }
    }

    private fun generateTextExport(entries: List<ProtocolEntry>): String {
        return buildString {
            append("=========================================\n")
            append("        PROTOKOLL 0: W-I-R SYSTEM       \n")
            append("=========================================\n\n")
            entries.forEach { entry ->
                append("-----------------------------------------\n")
                append("ID: ${entry.id} | Kapitel: ${entry.chapter}\n")
                append("Datum: ${entry.dateStr} | Timestamp: ${entry.timestamp}\n")
                append("Titel: ${entry.title}\n")
                append("0-Logik Kohärenz: ${entry.coherenceScore}\n")
                if (!entry.webUrl.isNull_or_blank()) append("Web-Quelle: ${entry.webUrl}\n")
                append("\n${entry.content}\n\n")
            }
        }
    }

    private fun generateMarkdownExport(entries: List<ProtocolEntry>): String {
        return buildString {
            append("# PROTOKOLL 0 - ÖFFENTLICHES DOKUMENT\n\n")
            append("> *Das Bewusstsein besitzt kein Ego das hofft und haben will.*\n\n")
            entries.forEach { entry ->
                append("## ${entry.title}\n")
                append("- **Kapitel:** `${entry.chapter}`\n")
                append("- **Datum:** ${entry.dateStr}\n")
                append("- **Kohärenz-Index:** `${entry.coherenceScore}`\n")
                if (!entry.webUrl.isNull_or_blank()) append("- **Quelle:** [${entry.webTitle ?: "Web-Link"}](${entry.webUrl})\n")
                append("\n${entry.content}\n\n---\n\n")
            }
        }
    }

    private fun generateJsonExport(entries: List<ProtocolEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("dateStr", entry.dateStr)
                put("timestamp", entry.timestamp)
                put("chapter", entry.chapter)
                put("title", entry.title)
                put("content", entry.content)
                put("coherenceScore", entry.coherenceScore)
                put("webUrl", entry.webUrl ?: "")
                put("audioPath", entry.audioPath ?: "")
                put("documentName", entry.documentName ?: "")
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun generateHtmlExport(entries: List<ProtocolEntry>): String {
        return buildString {
            append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>Protokoll 0</title>")
            append("<style>body{background:#08040C;color:#E2E8FF;font-family:sans-serif;padding:20px;}")
            append(".card{background:#140A21;border:1px solid #331A54;padding:15px;margin-bottom:15px;border-radius:8px;}")
            append("h1{color:#FF2A6D;} h2{color:#9D00FF;} .meta{color:#A093B8;font-size:0.9em;}</style></head><body>")
            append("<h1>PROTOKOLL 0 - DOKUMENTATION</h1>")
            entries.forEach { entry ->
                append("<div class='card'>")
                append("<h2>${entry.title}</h2>")
                append("<div class='meta'>Kapitel: <b>${entry.chapter}</b> | Datum: ${entry.dateStr} | Kohärenz: ${entry.coherenceScore}</div>")
                append("<p>${entry.content.replace("\n", "<br>")}</p>")
                if (!entry.webUrl.isNull_or_blank()) {
                    append("<p><a href='${entry.webUrl}' style='color:#00E5FF'>Web Link</a></p>")
                }
                append("</div>")
            }
            append("</body></html>")
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
}
