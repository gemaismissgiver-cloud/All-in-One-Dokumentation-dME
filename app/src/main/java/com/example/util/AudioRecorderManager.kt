package com.example.util

import android.content.Context
import com.example.util.audio.AndroidAudioRecorder
import com.example.util.audio.AudioRecordItem
import com.example.util.audio.AudioRecorder
import com.example.util.audio.AudioRecorderConfig
import com.example.util.audio.AudioRecordingState
import com.example.util.audio.AudioStorageManager
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * High-level manager and adapter for audio recording operations.
 * Manages media recording lifecycles, amplitude monitoring, and internal app file storage.
 */
class AudioRecorderManager(
    private val context: Context,
    config: AudioRecorderConfig = AudioRecorderConfig()
) {
    val storageManager: AudioStorageManager = AudioStorageManager(context)
    val recorder: AudioRecorder = AndroidAudioRecorder(context, config, storageManager)

    val state: StateFlow<AudioRecordingState> = recorder.state
    val amplitude: StateFlow<Int> = recorder.amplitude
    val durationMs: StateFlow<Long> = recorder.durationMs

    val currentOutputFile: File?
        get() = when (val s = recorder.state.value) {
            is AudioRecordingState.Recording -> s.file
            is AudioRecordingState.Paused -> s.file
            is AudioRecordingState.Stopped -> s.file
            else -> null
        }

    val isRecording: Boolean
        get() = recorder.state.value is AudioRecordingState.Recording || recorder.state.value is AudioRecordingState.Paused

    /**
     * Starts recording to a new file in app storage.
     * @return true if recording successfully started.
     */
    fun startRecording(prefix: String = "PROTOCOL_REC"): Boolean {
        val file = storageManager.createOutputFile(prefix = prefix, subdir = "audio_protocols")
        val result = recorder.start(file)
        return result.isSuccess
    }

    /**
     * Pauses the active recording.
     */
    fun pauseRecording(): Boolean {
        return recorder.pause()
    }

    /**
     * Resumes the paused recording.
     */
    fun resumeRecording(): Boolean {
        return recorder.resume()
    }

    /**
     * Stops the active recording and returns the saved file and recorded duration in milliseconds.
     */
    fun stopRecording(): Pair<File?, Long> {
        val result = recorder.stop()
        return if (result.isSuccess) {
            val item: AudioRecordItem = result.getOrThrow()
            Pair(item.file, item.durationMs)
        } else {
            Pair(null, 0L)
        }
    }

    /**
     * Cancels the active recording and deletes the partially recorded file.
     */
    fun cancelRecording() {
        recorder.cancel()
    }

    /**
     * Lists all recordings stored in the app storage.
     */
    fun listStoredRecordings(): List<AudioRecordItem> {
        return storageManager.listRecordings(subdir = "audio_protocols")
    }

    /**
     * Deletes a recording from storage.
     */
    fun deleteRecording(file: File?): Boolean {
        return storageManager.deleteRecording(file)
    }
}
