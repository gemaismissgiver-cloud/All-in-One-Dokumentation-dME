package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.util.AudioRecorderManager
import com.example.util.audio.AudioRecordingState
import com.example.util.audio.SpeechToTextHelper
import com.example.util.audio.SpeechToTextState
import java.io.File
import java.util.Locale

/**
 * A lightweight, reusable UI component to start and stop audio recordings,
 * featuring microphone permission activation and live Speech-to-Text preparation.
 *
 * @param audioRecorder Instance of AudioRecorderManager for capturing audio to app storage.
 * @param onAudioRecorded Callback returning the saved File and duration in ms once stopped.
 * @param onSpeechTranscribed Optional callback returning transcribed text (prepared for Speech-to-Text).
 * @param enableSpeechToText If true, activates live Speech-to-Text recognition alongside the recording.
 */
@Composable
fun SimpleVoiceRecordButton(
    audioRecorder: AudioRecorderManager,
    modifier: Modifier = Modifier,
    enableSpeechToText: Boolean = true,
    onAudioRecorded: (file: File, durationMs: Long) -> Unit = { _, _ -> },
    onSpeechTranscribed: (text: String) -> Unit = {}
) {
    val context = LocalContext.current
    val speechHelper = remember { SpeechToTextHelper(context) }

    val recordingState by audioRecorder.state.collectAsState()
    val durationMs by audioRecorder.durationMs.collectAsState()
    val amplitude by audioRecorder.amplitude.collectAsState()
    val speechState by speechHelper.state.collectAsState()
    val transcribedText by speechHelper.transcribedText.collectAsState()

    val isRecording = recordingState is AudioRecordingState.Recording || recordingState is AudioRecordingState.Paused

    // Microphone Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = audioRecorder.startRecording(prefix = "VOICE_MEMO")
            if (started && enableSpeechToText && speechHelper.isRecognitionAvailable()) {
                speechHelper.startListening()
            }
        } else {
            Toast.makeText(context, "Mikrofon-Berechtigung erforderlich für Sprachaufnahme", Toast.LENGTH_SHORT).show()
        }
    }

    // Forward recognized speech to callback
    LaunchedEffect(speechState) {
        if (speechState is SpeechToTextState.FinalResult) {
            val resultText = (speechState as SpeechToTextState.FinalResult).text
            if (resultText.isNotBlank()) {
                onSpeechTranscribed(resultText)
            }
        }
    }

    // Animation for pulsing mic ring when recording
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isRecording) NeonRedPrimary else CyberPurple,
        label = "btn_color"
    )

    Column(
        modifier = modifier.testTag("simple_voice_record_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Action Button Row
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    if (isRecording) {
                        // Stop recording and save file
                        if (enableSpeechToText) {
                            speechHelper.stopListening()
                        }
                        val (file, duration) = audioRecorder.stopRecording()
                        if (file != null) {
                            onAudioRecorded(file, duration)
                        }
                    } else {
                        // Check & request RECORD_AUDIO permission
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val started = audioRecorder.startRecording(prefix = "VOICE_MEMO")
                            if (started && enableSpeechToText && speechHelper.isRecognitionAvailable()) {
                                speechHelper.startListening()
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
                .testTag("simple_voice_record_button"),
            shape = RoundedCornerShape(24.dp),
            color = if (isRecording) NeonRedPrimary.copy(alpha = 0.15f) else DarkSurface,
            border = BorderStroke(
                width = if (isRecording) 1.5.dp else 1.dp,
                color = if (isRecording) NeonRedPrimary else CyberPurple
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsing Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .then(if (isRecording) Modifier.scale(pulseScale) else Modifier)
                        .clip(CircleShape)
                        .background(buttonColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Aufnahme stoppen" else "Aufnahme starten (Mikrofon)",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Text / Status
                Column {
                    Text(
                        text = if (isRecording) "AUFNAHME STOPPEN" else "SPRACHE AUFNEHMEN",
                        color = if (isRecording) NeonRedPrimary else PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isRecording) {
                            val totalSec = durationMs / 1000
                            String.format(Locale.getDefault(), "%02d:%02d • M4A Datei wird gespeichert", totalSec / 60, totalSec % 60)
                        } else {
                            "Tippen zum Starten (Audio & STT)"
                        },
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Live Speech-to-Text preview while recording
        AnimatedVisibility(visible = isRecording) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .border(0.5.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = ChapterDenkenColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vorbereitung Speech-to-Text (STT):",
                        color = ChapterDenkenColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val displayText = when (val s = speechState) {
                    is SpeechToTextState.PartialResult -> s.partialText
                    is SpeechToTextState.FinalResult -> s.text
                    is SpeechToTextState.Listening -> "Lausche auf Spracheingabe..."
                    is SpeechToTextState.Processing -> "Verarbeite Sprache..."
                    is SpeechToTextState.Error -> "STT Hinweis: ${s.message} (Audio-Datei wird normal gespeichert)"
                    else -> "Sprechen Sie jetzt... Text wird transkribiert"
                }

                Text(
                    text = displayText,
                    color = if (speechState is SpeechToTextState.Error) TextMuted else PureWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
