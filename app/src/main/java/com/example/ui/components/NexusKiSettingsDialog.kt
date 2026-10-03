package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.ai.OpenRouterClient
import kotlinx.coroutines.launch

@Composable
fun NexusKiSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val openRouterClient = remember { OpenRouterClient(context) }
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf(openRouterClient.apiKey) }
    var selectedModel by remember { mutableStateOf(openRouterClient.selectedModel) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("nexus_ki_settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = DarkBackground,
            border = BorderStroke(1.5.dp, CyberPurple)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonRedPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEXUS 0-KI EINSTELLUNGEN",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen", tint = OffWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Verbinde kostenlose Open-Source AGI / GPT Modelle über OpenRouter (DeepSeek R1, LLaMA 3.3 70B, etc.) direkt mit der 0-Logik Architektur.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Model Selection
                Text(
                    text = "Ausgewähltes KI-Modell (Kostenlos / Open-Source):",
                    color = OffWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OpenRouterClient.AVAILABLE_FREE_MODELS.forEach { (modelId, modelName) ->
                        val isSelected = selectedModel == modelId
                        Surface(
                            onClick = { selectedModel = modelId },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyberPurple.copy(alpha = 0.25f) else DarkSurface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) NeonRedPrimary else DarkCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedModel = modelId },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = NeonRedPrimary,
                                        unselectedColor = TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = modelName,
                                        color = if (isSelected) PureWhite else OffWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = modelId,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // API Key input
                Text(
                    text = "OpenRouter API Key (Kostenlos auf openrouter.ai):",
                    color = OffWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("sk-or-v1-...", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = OffWhite,
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Link to get free key
                Row(
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = ChapterDenkenColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kostenlosen Key auf openrouter.ai generieren",
                        color = ChapterDenkenColor,
                        fontSize = 11.sp
                    )
                }

                if (testResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResult ?: "",
                            color = if (testResult?.startsWith("Erfolg") == true) ChapterErforschenColor else NeonRedPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(14.dp))

                // Save & Test buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            openRouterClient.apiKey = apiKeyInput
                            openRouterClient.selectedModel = selectedModel
                            isTesting = true
                            testResult = "Prüfe Verbindung zu $selectedModel..."
                            scope.launch {
                                val res = openRouterClient.analyzeWithZeroLogik("Status Test: Initialisiere 0-Punkt.")
                                isTesting = false
                                testResult = if (res.isSuccess) {
                                    "Erfolg! Antwort: ${res.getOrNull()?.take(100)}..."
                                } else {
                                    "Fehler: ${res.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        enabled = apiKeyInput.isNotBlank() && !isTesting,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ChapterErfindenColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ChapterErfindenColor)
                        } else {
                            Text("Testen", color = ChapterErfindenColor, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            openRouterClient.apiKey = apiKeyInput
                            openRouterClient.selectedModel = selectedModel
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Speichern", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
