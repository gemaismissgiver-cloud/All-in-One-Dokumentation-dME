package com.example.util.audio

import android.media.MediaRecorder
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * State of the audio recording process.
 */
sealed class AudioRecordingState {
    object Idle : AudioRecordingState()

    data class Recording(
        val file: File,
        val elapsedMs: Long = 0L
    ) : AudioRecordingState()

    data class Paused(
        val file: File,
        val elapsedMs: Long
    ) : AudioRecordingState()

    data class Stopped(
        val file: File,
        val durationMs: Long
    ) : AudioRecordingState()

    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : AudioRecordingState()
}

/**
 * Configuration options for capturing audio with MediaRecorder.
 */
data class AudioRecorderConfig(
    val audioSource: Int = MediaRecorder.AudioSource.MIC,
    val outputFormat: Int = MediaRecorder.OutputFormat.MPEG_4,
    val audioEncoder: Int = MediaRecorder.AudioEncoder.AAC,
    val encodingBitRate: Int = 128000,
    val samplingRate: Int = 44100
)

/**
 * Contract for audio capture implementations.
 */
interface AudioRecorder {
    /**
     * Observable stream representing the current recording lifecycle state.
     */
    val state: StateFlow<AudioRecordingState>

    /**
     * Observable stream representing the live microphone amplitude (0..32767).
     * Useful for waveforms, visualizers, or decibel bars.
     */
    val amplitude: StateFlow<Int>

    /**
     * Observable stream representing elapsed recording duration in milliseconds.
     */
    val durationMs: StateFlow<Long>

    /**
     * Starts recording audio into the specified file or automatically in app storage.
     * @return Result containing the destination File if successfully started.
     */
    fun start(outputFile: File? = null): Result<File>

    /**
     * Pauses the current recording (Android 7.0+ / API 24+).
     * @return true if paused successfully.
     */
    fun pause(): Boolean

    /**
     * Resumes the paused recording.
     * @return true if resumed successfully.
     */
    fun resume(): Boolean

    /**
     * Stops the recording, releases hardware resources, and returns the saved file item.
     */
    fun stop(): Result<AudioRecordItem>

    /**
     * Cancels the current recording, releases resources, and deletes any partial file.
     */
    fun cancel()
}
