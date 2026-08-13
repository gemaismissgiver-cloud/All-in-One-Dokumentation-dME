package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProtocolChapter
import com.example.data.model.ProtocolEntry
import com.example.ui.components.ChapterFilterBar
import com.example.ui.components.ProtocolCard
import com.example.ui.components.ZeroMatrixHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.ProtocolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocolStreamScreen(
    viewModel: ProtocolViewModel,
    entries: List<ProtocolEntry>,
    selectedChapter: ProtocolChapter?,
    searchQuery: String,
    onAddNewEntryClick: () -> Unit,
    onEditEntryClick: (ProtocolEntry) -> Unit,
    onOpenUrlInBrowser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val averageCoherence = remember(entries) {
        if (entries.isEmpty()) 1.0f else entries.map { it.coherenceScore }.average().toFloat()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewEntryClick,
                containerColor = NeonRedPrimary,
                contentColor = PureWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_protocol_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Neues Protokoll Erstellen",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = DarkBackground,
        modifier = modifier.testTag("protocol_stream_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Zero Matrix Header
            ZeroMatrixHeader(
                totalEntriesCount = entries.size,
                averageCoherence = averageCoherence
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Protokolle durchsuchen...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Suchen", tint = ElectricViolet)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Löschen", tint = OffWhite)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = OffWhite,
                    focusedBorderColor = CyberPurple,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("search_protocol_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Chapter Filter Bar
            ChapterFilterBar(
                selectedChapter = selectedChapter,
                onSelectChapter = { viewModel.setChapterFilter(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Entry List
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "0-Zustand: Noch keine Protokolle",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tippe auf das '+' um deinen ersten Tagebuch-Eintrag\noder deine Sprachaufnahme zu dokumentieren.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = entries,
                        key = { it.id }
                    ) { entry ->
                        ProtocolCard(
                            entry = entry,
                            audioPlayer = viewModel.audioPlayer,
                            onEdit = { onEditEntryClick(entry) },
                            onDelete = { viewModel.deleteEntry(entry) },
                            onOpenUrl = onOpenUrlInBrowser
                        )
                    }
                }
            }
        }
    }
}
