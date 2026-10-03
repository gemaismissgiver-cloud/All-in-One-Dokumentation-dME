package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.io.File
import java.util.Locale

/**
 * Reusable Jetpack Compose component for recording audio and saving files into app storage.
 * Includes live amplitude visualizer, elapsed time clock, pause/resume, and cancellation.
 */
@Composable
fun AudioRecordingBar(
    audioRecorder: AudioRecorderManager,
    modifier: Modifier = Modifier,
    onRecordingSaved: (file: File, durationMs: Long) -> Unit = { _, _ -> },
    onRecordingCancelled: () -> Unit = {}
) {
    val context = LocalContext.current
    val recordingState by audioRecorder.state.collectAsState()
    val amplitude by audioRecorder.amplitude.collectAsState()
    val durationMs by audioRecorder.durationMs.collectAsState()

    val isRecording = recordingState is AudioRecordingState.Recording
    val isPaused = recordingState is AudioRecordingState.Paused
    val isActive = isRecording || isPaused

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            audioRecorder.startRecording()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_recording_bar"),
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isActive) NeonRedPrimary else DarkCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status indicator + Timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .then(if (isRecording) Modifier.scale(pulseScale) else Modifier)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isRecording -> NeonRedPrimary
                                    isPaused -> ChapterErfindenColor
                                    else -> TextMuted
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when {
                            isRecording -> "AUFNAHME LÄUFT"
                            isPaused -> "PAUSIERT"
                            else -> "AUDIO AUFNAHME"
                        },
                        color = when {
                            isRecording -> NeonRedPrimary
                            isPaused -> ChapterErfindenColor
                            else -> OffWhite
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Elapsed timer
                Text(
                    text = formatDuration(durationMs),
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("recording_duration_text")
                )
            }

            AnimatedVisibility(visible = isActive) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Amplitude visualizer bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val normalizedAmp = (amplitude.toFloat() / 32767f).coerceIn(0.05f, 1f)
                        repeat(16) { index ->
                            val phase = (index % 4) * 0.15f
                            val heightFraction = if (isRecording) {
                                (normalizedAmp * (0.6f + phase)).coerceIn(0.15f, 1f)
                            } else {
                                0.2f
                            }
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isRecording) NeonRedSecondary else TextMuted)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isActive) {
                    // Start Recording Button
                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                audioRecorder.startRecording()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_recording_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Aufnahme starten",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SPRACHE AUFNEHMEN",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // Cancel / Delete
                    OutlinedButton(
                        onClick = {
                            audioRecorder.cancelRecording()
                            onRecordingCancelled()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        border = BorderStroke(0.5.dp, DarkCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_recording_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Verwerfen",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verwerfen", fontSize = 11.sp)
                    }

                    // Pause / Resume
                    FilledTonalButton(
                        onClick = {
                            if (isRecording) {
                                audioRecorder.pauseRecording()
                            } else {
                                audioRecorder.resumeRecording()
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkSurface,
                            contentColor = ChapterErfindenColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pause_resume_recording_button")
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRecording) "Pause" else "Fortsetzen",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRecording) "Pause" else "Weiter", fontSize = 11.sp)
                    }

                    // Stop & Save
                    Button(
                        onClick = {
                            val (file, duration) = audioRecorder.stopRecording()
                            if (file != null) {
                                onRecordingSaved(file, duration)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("stop_save_recording_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Speichern",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Speichern", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
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
