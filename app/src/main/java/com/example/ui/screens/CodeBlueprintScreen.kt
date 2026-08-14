package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFileCategory
import com.example.data.model.ProjectFileItem
import com.example.data.model.ProtocolEntry
import com.example.data.repository.ProjectSourceRepository
import com.example.ui.theme.*
import com.example.util.ProjectPackager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlueprintScreen(
    entries: List<ProtocolEntry>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allFiles = remember { ProjectSourceRepository.getAllProjectFiles() }

    var selectedCategory by remember { mutableStateOf(ProjectFileCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedFilePath by remember { mutableStateOf<String?>(null) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var isLargeTextMode by remember { mutableStateOf(true) }

    val filteredFiles = remember(selectedCategory, searchQuery) {
        allFiles.filter { file ->
            val matchesCategory = (selectedCategory == ProjectFileCategory.ALL) || (file.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    file.fileName.contains(searchQuery, ignoreCase = true) ||
                    file.path.contains(searchQuery, ignoreCase = true) ||
                    file.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val baseFontSize = if (isLargeTextMode) 15.sp else 13.sp
    val codeFontSize = if (isLargeTextMode) 13.sp else 11.sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 14.dp)
            .testTag("code_blueprint_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Haupt-Hero: APK & ZIP Download Center mit großer, klarer Schrift
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(listOf(NeonRedPrimary, ElectricViolet, CyberPurple)),
                    shape = RoundedCornerShape(18.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header-Zeile mit Titel und Schriftgrößen-Umschalter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberPurple)
                                .border(1.5.dp, NeonRedPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "APK & CODE DOWNLOAD",
                                color = PureWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Dateien, Bauplan & fertige APK sichern",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Button für Schriftgröße vergrößern
                    IconButton(
                        onClick = { isLargeTextMode = !isLargeTextMode },
                        modifier = Modifier.testTag("toggle_font_size_button")
                    ) {
                        Icon(
                            imageVector = if (isLargeTextMode) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                            contentDescription = "Schriftgröße ändern",
                            tint = ChapterErkennenColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GROSSER BUTTON 1: ECHTE APK DATEI SPEICHERN
                Button(
                    onClick = { ProjectPackager.exportAndShareInstalledApk(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("export_apk_file_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "1. ECHTE .APK DATEI HERUNTERLADEN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✓ Speichern über Gmail, SD-Karte, Internen Speicher oder Files-App zum direkten Installieren.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GROSSER BUTTON 2: PROJEKT ALS .ZIP DATEI
                Button(
                    onClick = { ProjectPackager.exportAndShareProjectZip(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("export_project_zip_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "2. ALLE QUELLCODES ALS .ZIP HERUNTERLADEN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // WEITERE AKTIONEN: Files App & Bauanleitung
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { ProjectPackager.openFilesAppOrManager(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_files_app_button"),
                        border = BorderStroke(1.5.dp, ChapterErfindenColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = ChapterErfindenColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dateimanager",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { showGuideDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_guide_button"),
                        border = BorderStroke(1.5.dp, ChapterErkennenColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = ChapterErkennenColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bauanleitung",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Suchfeld für Dateien
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Datei suchen (z.B. MainActivity, Room, Database)...",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Suchen",
                    tint = ElectricViolet,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Löschen",
                            tint = PureWhite
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedBorderColor = CyberPurple,
                unfocusedBorderColor = DarkCardBorder,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("search_code_files_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Kategorie-Leiste
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(ProjectFileCategory.values()) { category ->
                val isSel = category == selectedCategory
                Surface(
                    onClick = { selectedCategory = category },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSel) NeonRedPrimary else DarkSurface,
                    border = BorderStroke(1.dp, if (isSel) NeonRedPrimary else DarkCardBorder)
                ) {
                    Text(
                        text = category.displayName,
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Info-Zeile: Anzahl gefundener Dateien
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredFiles.size} DATEIEN IM PROJEKT",
                color = ElectricViolet,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tippe auf Datei zum Öffnen & Kopieren",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dateiliste mit vergrößerten Klick-Bereichen und deutlicher Schrift
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(
                items = filteredFiles,
                key = { it.path }
            ) { fileItem ->
                val isExpanded = expandedFilePath == fileItem.path

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("file_item_${fileItem.fileName}")
                        .border(
                            width = if (isExpanded) 1.5.dp else 1.dp,
                            color = if (isExpanded) ChapterErkennenColor else DarkCardBorder,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Klickbare Zeile für Dateikopf
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedFilePath = if (isExpanded) null else fileItem.path
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (fileItem.language) {
                                            "Kotlin" -> Icons.Default.Code
                                            "XML" -> Icons.Default.DataObject
                                            "TOML", "Gradle Kotlin DSL" -> Icons.Default.Build
                                            "Markdown" -> Icons.Default.Article
                                            else -> Icons.Default.InsertDriveFile
                                        },
                                        contentDescription = null,
                                        tint = when (fileItem.category) {
                                            ProjectFileCategory.CORE -> NeonRedPrimary
                                            ProjectFileCategory.DATABASE -> ChapterErkennenColor
                                            ProjectFileCategory.SCREENS -> ElectricViolet
                                            ProjectFileCategory.COMPONENTS -> ChapterDenkenColor
                                            ProjectFileCategory.UTILS -> ChapterErfindenColor
                                            ProjectFileCategory.BUILD -> ChapterWuenstenColor
                                            ProjectFileCategory.GUIDE -> ChapterUnmoeglichColor
                                            else -> PureWhite
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileItem.fileName,
                                        color = PureWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = fileItem.path,
                                        color = Color(0xFFA0AEC0),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Sprache & Aufklapp-Icon
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = fileItem.language,
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Einklappen" else "Ausklappen",
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Dateibeschreibung in großer lesbarer Schrift
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = fileItem.description,
                            color = Color(0xFFE2E8F0),
                            fontSize = baseFontSize,
                            lineHeight = 19.sp
                        )

                        // Ausgeklappter Quellcode
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                // Aktionsleiste für diese Datei
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${fileItem.content.lines().size} Zeilen | ${fileItem.content.length} Zeichen",
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { ProjectPackager.copyToClipboard(context, fileItem.content, fileItem.fileName) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp).testTag("copy_file_${fileItem.fileName}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Kopieren", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { ProjectPackager.exportSingleFile(context, fileItem) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonRedPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp).testTag("share_file_${fileItem.fileName}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sichern", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Monospace Code-Viewer Box mit gut lesbarem Kontrast
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
                                    color = Color(0xFF07040B)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .verticalScroll(rememberScrollState())
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = fileItem.content,
                                            color = Color(0xFFE2E8F0),
                                            fontSize = codeFontSize,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bauanleitungs-Dialog mit großer, gut lesbarer Schrift
    if (showGuideDialog) {
        val guideFile = remember { allFiles.find { it.category == ProjectFileCategory.GUIDE } }

        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = NeonRedPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "BAUPLAN & ANLEITUNG",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    color = DarkSurface
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = guideFile?.content ?: "Bauanleitung wird geladen...",
                            color = PureWhite,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        guideFile?.let { ProjectPackager.copyToClipboard(context, it.content, "Bauanleitung") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anleitung Kopieren", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuideDialog = false }) {
                    Text("Schließen", color = TextMuted, fontSize = 13.sp)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
