package com.example.util.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Standard Android implementation of [AudioRecorder] using [MediaRecorder].
 * Saves audio directly to app internal storage without requiring external storage permissions.
 */
class AndroidAudioRecorder(
    private val context: Context,
    private val config: AudioRecorderConfig = AudioRecorderConfig(),
    val storageManager: AudioStorageManager = AudioStorageManager(context)
) : AudioRecorder {

    companion object {
        private const val TAG = "AndroidAudioRecorder"
        private const val AMPLITUDE_POLL_INTERVAL_MS = 100L
        private const val DURATION_TICK_INTERVAL_MS = 200L
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTimeMs: Long = 0L
    private var accumulatedDurationMs: Long = 0L
    private var isPausedInternal: Boolean = false

    private val recorderScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var amplitudeJob: Job? = null
    private var durationJob: Job? = null

    private val _state = MutableStateFlow<AudioRecordingState>(AudioRecordingState.Idle)
    override val state: StateFlow<AudioRecordingState> = _state.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    override val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    @Synchronized
    override fun start(outputFile: File?): Result<File> {
        // Cancel any lingering recording
        if (_state.value is AudioRecordingState.Recording || _state.value is AudioRecordingState.Paused) {
            cancel()
        }

        return try {
            val destinationFile = outputFile ?: storageManager.createOutputFile()
            // Ensure parent exists
            destinationFile.parentFile?.mkdirs()

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(config.audioSource)
                setOutputFormat(config.outputFormat)
                setAudioEncoder(config.audioEncoder)
                setAudioEncodingBitRate(config.encodingBitRate)
                setAudioSamplingRate(config.samplingRate)
                setOutputFile(destinationFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            currentFile = destinationFile
            startTimeMs = System.currentTimeMillis()
            accumulatedDurationMs = 0L
            isPausedInternal = false

            _durationMs.value = 0L
            _state.value = AudioRecordingState.Recording(destinationFile, 0L)

            startMonitoring()

            Log.i(TAG, "Audio recording started -> ${destinationFile.absolutePath}")
            Result.success(destinationFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            cleanup()
            _state.value = AudioRecordingState.Error("Fehler beim Starten der Aufnahme: ${e.message}", e)
            Result.failure(e)
        }
    }

    @Synchronized
    override fun pause(): Boolean {
        val recorder = mediaRecorder ?: return false
        if (_state.value !is AudioRecordingState.Recording) return false

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.pause()
                accumulatedDurationMs += (System.currentTimeMillis() - startTimeMs)
                isPausedInternal = true
                val file = currentFile ?: return false
                _state.value = AudioRecordingState.Paused(file, accumulatedDurationMs)
                stopMonitoring()
                Log.d(TAG, "Audio recording paused at ${accumulatedDurationMs}ms")
                true
            } else {
                Log.w(TAG, "Pause is not supported on Android < API 24")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause audio recording", e)
            false
        }
    }

    @Synchronized
    override fun resume(): Boolean {
        val recorder = mediaRecorder ?: return false
        if (_state.value !is AudioRecordingState.Paused) return false

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.resume()
                startTimeMs = System.currentTimeMillis()
                isPausedInternal = false
                val file = currentFile ?: return false
                _state.value = AudioRecordingState.Recording(file, accumulatedDurationMs)
                startMonitoring()
                Log.d(TAG, "Audio recording resumed")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume audio recording", e)
            false
        }
    }

    @Synchronized
    override fun stop(): Result<AudioRecordItem> {
        val recorder = mediaRecorder
        val file = currentFile

        if (recorder == null || file == null) {
            return Result.failure(IllegalStateException("No active recording to stop"))
        }

        stopMonitoring()

        val finalDuration = if (isPausedInternal) {
            accumulatedDurationMs
        } else {
            accumulatedDurationMs + (System.currentTimeMillis() - startTimeMs)
        }

        return try {
            try {
                recorder.stop()
            } catch (stopEx: Exception) {
                Log.w(TAG, "Exception during recorder.stop() (possibly very short recording)", stopEx)
            }
            cleanup()

            val extractedDuration = storageManager.extractDurationMs(file)
            val effectiveDuration = if (extractedDuration > 0) extractedDuration else finalDuration

            val item = AudioRecordItem(
                file = file,
                fileName = file.name,
                durationMs = effectiveDuration,
                sizeBytes = file.length(),
                createdAt = file.lastModified()
            )

            _state.value = AudioRecordingState.Stopped(file, effectiveDuration)
            _durationMs.value = effectiveDuration
            Log.i(TAG, "Audio recording saved successfully: ${file.absolutePath} ($effectiveDuration ms)")
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "Error finalizing audio recording", e)
            cleanup()
            _state.value = AudioRecordingState.Error("Fehler beim Beenden der Aufnahme: ${e.message}", e)
            Result.failure(e)
        }
    }

    @Synchronized
    override fun cancel() {
        Log.d(TAG, "Canceling audio recording and deleting partial file")
        stopMonitoring()
        try {
            mediaRecorder?.stop()
        } catch (ignored: Exception) {
        }
        cleanup()

        currentFile?.let { partialFile ->
            storageManager.deleteRecording(partialFile)
        }
        currentFile = null
        _state.value = AudioRecordingState.Idle
        _durationMs.value = 0L
        _amplitude.value = 0
    }

    private fun startMonitoring() {
        stopMonitoring()

        // Amplitude monitor loop
        amplitudeJob = recorderScope.launch {
            while (isActive) {
                val amp = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }
                _amplitude.value = amp
                delay(AMPLITUDE_POLL_INTERVAL_MS)
            }
        }

        // Duration tick loop
        durationJob = recorderScope.launch {
            while (isActive) {
                if (!isPausedInternal) {
                    val currentElapsed = accumulatedDurationMs + (System.currentTimeMillis() - startTimeMs)
                    _durationMs.value = currentElapsed
                    currentFile?.let { f ->
                        _state.value = AudioRecordingState.Recording(f, currentElapsed)
                    }
                }
                delay(DURATION_TICK_INTERVAL_MS)
            }
        }
    }

    private fun stopMonitoring() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        durationJob?.cancel()
        durationJob = null
        _amplitude.value = 0
    }

    private fun cleanup() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error during mediaRecorder release", e)
        } finally {
            mediaRecorder = null
        }
    }
}
