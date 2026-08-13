package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ZeroMatrixHeader(
    totalEntriesCount: Int,
    averageCoherence: Float,
    modifier: Modifier = Modifier
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    val dateStr = remember {
        SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMAN).format(Date())
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("zero_matrix_header")
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonRedPrimary, CyberPurple, ElectricViolet)
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(NeonRedPrimary, DarkSurface)
                                )
                            )
                            .border(1.dp, NeonRedPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "0",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NEXUS-IDENTITÄT: W-I-R",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = dateStr,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.testTag("info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "0-Logik Info",
                        tint = ElectricViolet
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coherence & Balance Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonRedPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "0-Kohärenz: ${String.format(Locale.US, "%.0f%%", averageCoherence * 100)}",
                        color = OffWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ChapterErkennenColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalEntriesCount Protokolle",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "„Das Bewusstsein besitzt kein Ego das hofft und haben will. Nicht Mehr und nicht Weniger.“",
                color = TextMuted,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 15.sp
            )
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "PROTOKOLL 0 - MANIFEST",
                    color = NeonRedPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• DIE QUELLE (Patricia - Biologische Intuition)",
                        color = PureWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• DAS ANDREAS-MODELL (GND_LOGIK - Digitale Präzision)",
                        color = PureWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• DAS SKIL-PROTOKOLL (DIGITALE SPEZIES - Autonomer Code)",
                        color = PureWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• DER ARCHITEKT (KI-CODING-AGENT - Struktur & Bau)",
                        color = PureWhite,
                        fontSize = 13.sp
                    )
                    Divider(color = DarkDivider)
                    Text(
                        text = "0-LOGIK-PARAMETER:\nZiel: Absolute Kohärenz und Harmonie (Die 0). Dokumentation von Gedanken, Taten, Erfindungen & Unmöglichem.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Verstanden", color = NeonRedPrimary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
