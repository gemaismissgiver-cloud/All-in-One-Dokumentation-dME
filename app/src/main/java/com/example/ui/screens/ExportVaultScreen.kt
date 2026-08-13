package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.ProtocolChapter
import com.example.data.model.ProtocolEntry
import com.example.ui.theme.*
import com.example.util.ExportShareManager
import java.util.Locale

@Composable
fun ExportVaultScreen(
    entries: List<ProtocolEntry>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportShareManager.ExportFormat.JSON) }
    var selectedChapterFilter by remember { mutableStateOf<ProtocolChapter?>(null) }

    val filteredEntries = remember(entries, selectedChapterFilter) {
        if (selectedChapterFilter == null) entries
        else entries.filter { it.chapter == selectedChapterFilter!!.id }
    }

    val avgCoherence = remember(entries) {
        if (entries.isEmpty()) 1.0f else entries.map { it.coherenceScore }.average().toFloat()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("export_vault_screen")
    ) {
        // Vault Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyberPurple)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = NeonRedPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ÖFFENTLICHER DOKUMENTEN-VAULT",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Protokolle, Dokumente & Formate frei teilen",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${entries.size}", color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Protokolle", color = TextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", avgCoherence * 100),
                            color = ChapterErkennenColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Ø Kohärenz", color = TextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "4", color = ChapterDenkenColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Formate (JSON/TXT/MD/HTML)", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export Configuration Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.5.dp, DarkCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "1. EXPORT-FORMAT WÄHLEN",
                    color = ElectricViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportShareManager.ExportFormat.values().forEach { format ->
                        val isSel = format == selectedFormat
                        Surface(
                            onClick = { selectedFormat = format },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) NeonRedPrimary else DarkSurfaceVariant,
                            border = BorderStroke(0.5.dp, if (isSel) NeonRedPrimary else DarkCardBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = format.name,
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        ExportShareManager.exportAndShareEntries(
                            context,
                            filteredEntries,
                            selectedFormat,
                            "Protokoll_0_Oeffentlich"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_document_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.IosShare, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DOKUMENT VERÖFFENTLICHEN & TEILEN (${filteredEntries.size} Einträge)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ÜBERSICHT DEINER ENTITY-PROTOKOLLE",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Keine Protokolle vorhanden. Erstelle deinen ersten Eintrag!", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEntries) { entry ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = BorderStroke(0.5.dp, DarkCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = entry.title, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Kapitel: ${entry.chapter} | Datum: ${entry.dateStr}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(
                                onClick = { ExportShareManager.shareEntryText(context, entry) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Einzeleintrag Teilen",
                                    tint = ElectricViolet
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
