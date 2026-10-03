package com.example.util.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Metadata descriptor for an audio recording stored in app storage.
 */
data class AudioRecordItem(
    val file: File,
    val fileName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Long
)

/**
 * Manages audio recording files safely inside the application's private internal storage.
 * Files stored here are isolated from other apps, require no runtime storage permissions,
 * and are cleaned up when the app is uninstalled.
 */
class AudioStorageManager(private val context: Context) {

    companion object {
        const val DEFAULT_AUDIO_DIR = "audio_recordings"
        private const val TAG = "AudioStorageManager"
    }

    /**
     * Returns the dedicated directory for storing audio recordings in internal app storage.
     * Ensures the directory exists.
     */
    fun getRecordingsDirectory(subdir: String = DEFAULT_AUDIO_DIR): File {
        val dir = File(context.filesDir, subdir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Generates a new file reference inside app storage with a timestamped unique name.
     *
     * @param prefix Optional prefix for the recording file name (e.g. "PROTOCOL_REC", "VOICE_NOTE")
     * @param extension Extension for the audio container (defaults to "m4a")
     */
    fun createOutputFile(
        prefix: String = "REC",
        extension: String = "m4a",
        subdir: String = DEFAULT_AUDIO_DIR
    ): File {
        val dir = getRecordingsDirectory(subdir)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val randomSuffix = (1000..9999).random()
        val fileName = "${prefix}_${timestamp}_$randomSuffix.$extension"
        return File(dir, fileName)
    }

    /**
     * Lists all audio recordings currently stored in the recordings directory,
     * sorted by creation time descending (most recent first).
     */
    fun listRecordings(subdir: String = DEFAULT_AUDIO_DIR): List<AudioRecordItem> {
        val dir = getRecordingsDirectory(subdir)
        val files = dir.listFiles { file ->
            file.isFile && (file.extension.equals("m4a", ignoreCase = true) ||
                    file.extension.equals("mp4", ignoreCase = true) ||
                    file.extension.equals("aac", ignoreCase = true) ||
                    file.extension.equals("3gp", ignoreCase = true))
        } ?: emptyArray()

        return files.map { file ->
            val durationMs = extractDurationMs(file)
            AudioRecordItem(
                file = file,
                fileName = file.name,
                durationMs = durationMs,
                sizeBytes = file.length(),
                createdAt = file.lastModified()
            )
        }.sortedByDescending { it.createdAt }
    }

    /**
     * Extracts playback duration of an audio file using MediaMetadataRetriever.
     */
    fun extractDurationMs(file: File): Long {
        if (!file.exists() || file.length() == 0L) return 0L
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract duration from ${file.name}", e)
            0L
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Safely deletes a recording file from app storage.
     */
    fun deleteRecording(file: File?): Boolean {
        if (file == null || !file.exists()) return false
        return try {
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete recording: ${file.absolutePath}", e)
            false
        }
    }

    /**
     * Clears all recorded audio files in the specified app storage directory.
     */
    fun clearAllRecordings(subdir: String = DEFAULT_AUDIO_DIR): Int {
        val dir = getRecordingsDirectory(subdir)
        var deletedCount = 0
        dir.listFiles()?.forEach { file ->
            if (file.isFile && deleteRecording(file)) {
                deletedCount++
            }
        }
        return deletedCount
    }

    /**
     * Formats byte size into human-readable representation (KB, MB).
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.getDefault(), "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Formats milliseconds duration into MM:SS or HH:MM:SS format.
     */
    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
