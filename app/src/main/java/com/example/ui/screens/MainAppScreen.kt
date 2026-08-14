package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProtocolEntry
import com.example.ui.theme.*
import com.example.ui.viewmodel.ProtocolViewModel

enum class MainTab(val title: String) {
    PROTOKOLLE("Protokolle"),
    BROWSER("Internet Browser"),
    VAULT("Export Vault"),
    CODE_BLUEPRINT("Code & APK")
}

@Composable
fun MainAppScreen(
    viewModel: ProtocolViewModel
) {
    var selectedTab by remember { mutableStateOf(MainTab.PROTOKOLLE) }

    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val selectedChapter by viewModel.selectedChapter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val webClipBuffer by viewModel.webClipBuffer.collectAsStateWithLifecycle()

    var showEditorDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<ProtocolEntry?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = OffWhite,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .testTag("main_bottom_nav")
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.PROTOKOLLE,
                    onClick = { selectedTab = MainTab.PROTOKOLLE },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Tagebuch",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Tagebuch",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == MainTab.PROTOKOLLE) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = PureWhite,
                        indicatorColor = NeonRedPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.BROWSER,
                    onClick = { selectedTab = MainTab.BROWSER },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Browser",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Browser",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == MainTab.BROWSER) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = PureWhite,
                        indicatorColor = CyberPurple,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.VAULT,
                    onClick = { selectedTab = MainTab.VAULT },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Export Vault",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Export",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == MainTab.VAULT) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = PureWhite,
                        indicatorColor = ElectricViolet,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.CODE_BLUEPRINT,
                    onClick = { selectedTab = MainTab.CODE_BLUEPRINT },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Code & APK",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Code & APK",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == MainTab.CODE_BLUEPRINT) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = PureWhite,
                        indicatorColor = ChapterErkennenColor,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.PROTOKOLLE -> {
                    ProtocolStreamScreen(
                        viewModel = viewModel,
                        entries = entries,
                        selectedChapter = selectedChapter,
                        searchQuery = searchQuery,
                        onAddNewEntryClick = {
                            entryToEdit = null
                            showEditorDialog = true
                        },
                        onEditEntryClick = { entry ->
                            entryToEdit = entry
                            showEditorDialog = true
                        },
                        onOpenUrlInBrowser = { url ->
                            selectedTab = MainTab.BROWSER
                        }
                    )
                }

                MainTab.BROWSER -> {
                    InAppBrowserScreen(
                        onSaveWebClipToChapter = { url, title ->
                            viewModel.setWebClipBuffer(url, title)
                            entryToEdit = null
                            showEditorDialog = true
                        }
                    )
                }

                MainTab.VAULT -> {
                    ExportVaultScreen(
                        entries = entries,
                        onNavigateToCodeBlueprint = {
                            selectedTab = MainTab.CODE_BLUEPRINT
                        }
                    )
                }

                MainTab.CODE_BLUEPRINT -> {
                    CodeBlueprintScreen(entries = entries)
                }
            }
        }
    }

    // Editor Dialog
    if (showEditorDialog || webClipBuffer != null) {
        EntryEditorDialog(
            initialEntry = entryToEdit,
            initialWebClip = webClipBuffer,
            viewModel = viewModel,
            onDismiss = {
                showEditorDialog = false
                entryToEdit = null
                viewModel.clearWebClipBuffer()
            },
            onSave = { title, content, chapter, audioPath, audioDurationMs, imageUri, documentUri, documentName, webUrl, webTitle ->
                viewModel.saveEntry(
                    id = entryToEdit?.id ?: 0L,
                    title = title,
                    content = content,
                    chapter = chapter,
                    audioPath = audioPath,
                    audioDurationMs = audioDurationMs,
                    imageUri = imageUri,
                    documentUri = documentUri,
                    documentName = documentName,
                    webUrl = webUrl,
                    webTitle = webTitle
                )
                showEditorDialog = false
                entryToEdit = null
                viewModel.clearWebClipBuffer()
                selectedTab = MainTab.PROTOKOLLE
            }
        )
    }
}
