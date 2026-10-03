package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.ProtocolChapter
import com.example.data.model.ProtocolEntry
import com.example.ui.components.AudioRecordingBar
import com.example.ui.components.NexusKiSettingsDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.ProtocolViewModel
import com.example.util.ZeroLogikAnalyzer
import com.example.util.ai.OpenRouterClient
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorDialog(
    initialEntry: ProtocolEntry? = null,
    initialWebClip: Pair<String, String>? = null,
    viewModel: ProtocolViewModel,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        content: String,
        chapter: ProtocolChapter,
        audioPath: String?,
        audioDurationMs: Long,
        imageUri: String?,
        documentUri: String?,
        documentName: String?,
        webUrl: String?,
        webTitle: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialEntry?.title ?: initialWebClip?.second ?: "") }
    var content by remember { mutableStateOf(initialEntry?.content ?: "") }
    var selectedChapter by remember {
        mutableStateOf(
            if (initialEntry != null) ProtocolChapter.fromId(initialEntry.chapter)
            else ProtocolChapter.DENKEN
        )
    }

    // Attachments state
    var audioPath by remember { mutableStateOf(initialEntry?.audioPath) }
    var audioDurationMs by remember { mutableStateOf(initialEntry?.audioDurationMs ?: 0L) }
    var documentUri by remember { mutableStateOf(initialEntry?.documentUri) }
    var documentName by remember { mutableStateOf(initialEntry?.documentName) }
    var webUrl by remember { mutableStateOf(initialEntry?.webUrl ?: initialWebClip?.first) }
    var webTitle by remember { mutableStateOf(initialEntry?.webTitle ?: initialWebClip?.second) }

    // Audio recording state
    var isRecordingAudio by remember { mutableStateOf(false) }
    val isAudioPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
    val playingPath by viewModel.audioPlayer.currentPath.collectAsState()
    val isThisAudioPlaying = isAudioPlaying && playingPath == audioPath

    // Real-time 0-Logik Analysis
    val logikResult = remember(content) { ZeroLogikAnalyzer.analyze(content) }

    // Nexus OpenRouter AI State
    val openRouterClient = remember { OpenRouterClient(context) }
    val scope = rememberCoroutineScope()
    var nexusReflection by remember { mutableStateOf<String?>(null) }
    var isAnalyzingWithNexus by remember { mutableStateOf(false) }
    var showNexusSettingsDialog by remember { mutableStateOf(false) }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = viewModel.audioRecorder.startRecording()
            if (started) isRecordingAudio = true
        }
    }

    // Document file picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            documentUri = it.toString()
            val filename = it.lastPathSegment ?: "Importierte_Datei.txt"
            documentName = filename

            // Attempt to read text content automatically into editor if empty or requested
            val textContent = viewModel.readTextFromUri(it)
            if (textContent.isNotBlank() && !textContent.startsWith("Fehler")) {
                if (content.isBlank()) {
                    content = textContent
                } else {
                    content += "\n\n--- INHALT AUS $filename ---\n$textContent"
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("entry_editor_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = DarkBackground,
            border = BorderStroke(1.dp, selectedChapter.color)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_editor")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = OffWhite
                        )
                    }

                    Text(
                        text = if (initialEntry == null) "NEUES PROTOKOLL" else "PROTOKOLL BEARBEITEN",
                        color = selectedChapter.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = {
                            if (viewModel.audioRecorder.isRecording) {
                                val (file, duration) = viewModel.audioRecorder.stopRecording()
                                isRecordingAudio = false
                                if (file != null) {
                                    audioPath = file.absolutePath
                                    audioDurationMs = duration
                                }
                            }
                            onSave(
                                title,
                                content,
                                selectedChapter,
                                audioPath,
                                audioDurationMs,
                                null,
                                documentUri,
                                documentName,
                                webUrl,
                                webTitle
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedChapter.color),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_entry_button")
                    ) {
                        Text("SPEICHERN", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Chapter Selector Dropdown / Chips
                    Text(
                        text = "Kapitel Auswählen:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProtocolChapter.values().toList().chunked(2).forEach { rowChapters ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowChapters.forEach { chap ->
                                    val isSel = chap == selectedChapter
                                    Surface(
                                        onClick = { selectedChapter = chap },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) chap.color.copy(alpha = 0.25f) else DarkSurface,
                                        border = BorderStroke(
                                            width = if (isSel) 1.5.dp else 0.5.dp,
                                            color = if (isSel) chap.color else DarkCardBorder
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(chap.color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = chap.title,
                                                color = if (isSel) PureWhite else OffWhite,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titel des Protokolls (optional)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = OffWhite,
                            focusedBorderColor = selectedChapter.color,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_title_input")
                    )

                    // Text Content Input
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Dokumentiere deine Gedanken, Taten, Erkenntnisse...", color = TextMuted) },
                        minLines = 6,
                        maxLines = 12,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = OffWhite,
                            focusedBorderColor = selectedChapter.color,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entry_content_input")
                    )

                    // Attachments Bar: Audio Recording, Document Upload
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Medien & Formate (Dateien, Sprachaufnahme, Web)",
                                color = OffWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Audio Recording Section: AudioRecordingBar or Attached Audio Preview
                            if (audioPath == null) {
                                AudioRecordingBar(
                                    audioRecorder = viewModel.audioRecorder,
                                    onRecordingSaved = { file, duration ->
                                        audioPath = file.absolutePath
                                        audioDurationMs = duration
                                    },
                                    onRecordingCancelled = {
                                        audioPath = null
                                        audioDurationMs = 0L
                                    }
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceVariant,
                                    border = BorderStroke(1.dp, CyberPurple)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    audioPath?.let { path ->
                                                        viewModel.audioPlayer.playOrPause(path)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(CyberPurple)
                                            ) {
                                                Icon(
                                                    imageVector = if (isThisAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "Sprachnachricht abspielen",
                                                    tint = PureWhite
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Sprachaufnahme angehängt",
                                                    color = OffWhite,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = viewModel.audioRecorder.storageManager.formatDuration(audioDurationMs),
                                                    color = TextMuted,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                if (isThisAudioPlaying) {
                                                    viewModel.audioPlayer.stop()
                                                }
                                                audioPath = null
                                                audioDurationMs = 0L
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Sprachaufnahme löschen",
                                                tint = NeonRedPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Upload Document / Text File
                            Button(
                                onClick = { documentPickerLauncher.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.5.dp, ChapterErfindenColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_file_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = "Datei Hochladen",
                                    tint = ChapterErfindenColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Datei / Notiz anhängen (*.txt, *.md, *.json)", color = OffWhite, fontSize = 11.sp)
                            }

                            // Attached Document Indicator
                            if (documentName != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = ChapterErfindenColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Datei: $documentName",
                                            color = OffWhite,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { documentUri = null; documentName = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Entfernen",
                                            tint = NeonRedPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Web Clip Indicator
                            if (webUrl != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = ChapterDenkenColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = webTitle ?: webUrl!!,
                                            color = ChapterDenkenColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { webUrl = null; webTitle = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Entfernen",
                                            tint = NeonRedPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 0-Logik Analysis Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(0.5.dp, CyberPurple)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "0-LOGIK ANALYSE",
                                    color = ElectricViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Kohärenz: ${String.format(Locale.US, "%.0f%%", logikResult.coherenceScore * 100)}",
                                    color = if (logikResult.isHarmonious) ChapterErkennenColor else NeonRedPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = logikResult.evaluationMessage,
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Wörter: ${logikResult.wordCount}", color = TextMuted, fontSize = 10.sp)
                                Text(text = "Zeichen: ${logikResult.characterCount}", color = TextMuted, fontSize = 10.sp)
                                Text(text = "Ego-Impulse: ${logikResult.egoWordsCount}", color = TextMuted, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Nexus OpenRouter AI Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (!openRouterClient.isConfigured) {
                                            showNexusSettingsDialog = true
                                        } else {
                                            isAnalyzingWithNexus = true
                                            scope.launch {
                                                val promptText = "Titel: $title\nKapitel: ${selectedChapter.title}\nText: $content"
                                                val result = openRouterClient.analyzeWithZeroLogik(promptText)
                                                isAnalyzingWithNexus = false
                                                nexusReflection = if (result.isSuccess) {
                                                    result.getOrNull()
                                                } else {
                                                    "Fehler: ${result.exceptionOrNull()?.message}"
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isAnalyzingWithNexus) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = PureWhite
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Berechne...", fontSize = 11.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = PureWhite
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("NEXUS 0-KI REFLEKTION", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(
                                    onClick = { showNexusSettingsDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Nexus KI Einstellungen",
                                        tint = TextMuted
                                    )
                                }
                            }

                            // Nexus Reflection Result Display
                            if (nexusReflection != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkBackground,
                                    border = BorderStroke(1.dp, NeonRedPrimary.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "NEXUS REFLEKTION (${openRouterClient.selectedModel}):",
                                                color = NeonRedPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            IconButton(
                                                onClick = { nexusReflection = null },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Schließen", tint = TextMuted, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = nexusReflection ?: "",
                                            color = OffWhite,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        TextButton(
                                            onClick = {
                                                content += "\n\n--- NEXUS 0-KI REFLEKTION ---\n${nexusReflection ?: ""}"
                                                nexusReflection = null
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("In Notiz einfügen", color = ChapterErkennenColor, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNexusSettingsDialog) {
            NexusKiSettingsDialog(onDismiss = { showNexusSettingsDialog = false })
        }
    }
}
