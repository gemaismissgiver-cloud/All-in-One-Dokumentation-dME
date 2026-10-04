package com.example.util.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * State representing speech-to-text recognition progress.
 */
sealed class SpeechToTextState {
    object Idle : SpeechToTextState()
    object Ready : SpeechToTextState()
    object Listening : SpeechToTextState()
    object Processing : SpeechToTextState()
    data class PartialResult(val partialText: String) : SpeechToTextState()
    data class FinalResult(val text: String) : SpeechToTextState()
    data class Error(val message: String, val errorCode: Int? = null) : SpeechToTextState()
}

/**
 * Helper class that coordinates Android's built-in [SpeechRecognizer]
 * to transcribe audio into text, prepared for instant or future speech-to-text features.
 */
class SpeechToTextHelper(private val context: Context) {

    companion object {
        private const val TAG = "SpeechToTextHelper"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<SpeechToTextState>(SpeechToTextState.Idle)
    val state: StateFlow<SpeechToTextState> = _state.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    /**
     * Checks if SpeechRecognizer is installed and available on this device.
     */
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Begins listening to the microphone for speech-to-text recognition.
     */
    fun startListening(languageCode: String = Locale.getDefault().toLanguageTag()) {
        if (!isRecognitionAvailable()) {
            _state.value = SpeechToTextState.Error("Spracherkennung ist auf diesem Gerät nicht verfügbar.")
            return
        }

        stopListening()

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            recognizer.startListening(intent)
            _state.value = SpeechToTextState.Listening
            Log.d(TAG, "SpeechRecognizer listening started in language $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SpeechRecognizer", e)
            _state.value = SpeechToTextState.Error("Fehler beim Starten der Spracherkennung: ${e.message}")
        }
    }

    /**
     * Stops listening and processes remaining speech.
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping SpeechRecognizer", e)
        } finally {
            speechRecognizer = null
            if (_state.value is SpeechToTextState.Listening) {
                _state.value = SpeechToTextState.Idle
            }
        }
    }

    /**
     * Clears current transcription buffer.
     */
    fun clearTranscribedText() {
        _transcribedText.value = ""
        _state.value = SpeechToTextState.Idle
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = SpeechToTextState.Ready
            }

            override fun onBeginningOfSpeech() {
                _state.value = SpeechToTextState.Listening
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changes if needed for visualization
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _state.value = SpeechToTextState.Processing
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio-Aufnahmefehler"
                    SpeechRecognizer.ERROR_CLIENT -> "Client-Fehler"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon-Berechtigung fehlt"
                    SpeechRecognizer.ERROR_NETWORK -> "Netzwerkfehler"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Netzwerk-Timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Keine Sprache erkannt"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Spracherkennung ist ausgelastet"
                    SpeechRecognizer.ERROR_SERVER -> "Server-Fehler"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Keine Spracheingabe empfangen"
                    else -> "Fehlercode $error"
                }
                Log.w(TAG, "Speech recognition error: $errorMsg ($error)")
                _state.value = SpeechToTextState.Error(errorMsg, error)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull() ?: ""
                if (recognized.isNotBlank()) {
                    _transcribedText.value = recognized
                    _state.value = SpeechToTextState.FinalResult(recognized)
                    Log.d(TAG, "Recognized text: $recognized")
                } else {
                    _state.value = SpeechToTextState.Idle
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull() ?: ""
                if (partial.isNotBlank()) {
                    _state.value = SpeechToTextState.PartialResult(partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
