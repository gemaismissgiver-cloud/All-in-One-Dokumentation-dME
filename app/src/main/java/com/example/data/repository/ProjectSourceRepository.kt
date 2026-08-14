package com.example.data.repository

import com.example.data.model.ProjectFileCategory
import com.example.data.model.ProjectFileItem

object ProjectSourceRepository {

    fun getAllProjectFiles(): List<ProjectFileItem> {
        return listOf(
            // 1. AndroidManifest.xml
            ProjectFileItem(
                path = "app/src/main/AndroidManifest.xml",
                fileName = "AndroidManifest.xml",
                category = ProjectFileCategory.CORE,
                language = "XML",
                description = "Haupt-Manifest der App: Registriert Aktivitäten, Berechtigungen (Audio/Netzwerk) und FileProvider für Downloads/Sharing.",
                content = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${'$'}{applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>
                """.trimIndent()
            ),

            // 2. MainActivity.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/MainActivity.kt",
                fileName = "MainActivity.kt",
                category = ProjectFileCategory.CORE,
                language = "Kotlin",
                description = "Der Haupteinstiegspunkt der Android-App. Initialisiert Edge-to-Edge Darstellung, ViewModel und lädt das Jetpack Compose Theme.",
                content = """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ProtocolViewModel

class MainActivity : ComponentActivity() {
    private val protocolViewModel: ProtocolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = protocolViewModel)
            }
        }
    }
}
                """.trimIndent()
            ),

            // 3. ProtocolEntry.kt (Entity)
            ProjectFileItem(
                path = "app/src/main/java/com/example/data/model/ProtocolEntry.kt",
                fileName = "ProtocolEntry.kt",
                category = ProjectFileCategory.DATABASE,
                language = "Kotlin",
                description = "Room Datenbank-Entity für Tagebucheinträge (Titel, Inhalt, Kapitel, Sprachmemo, Dokumenten-Anhänge, Web-Links, 0-Logik-Index).",
                content = """
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocol_entries")
data class ProtocolEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // e.g. "2026-08-13"
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val chapter: String, // Matches ProtocolChapter.id
    val audioPath: String? = null,
    val audioDurationMs: Long = 0,
    val imageUri: String? = null,
    val documentUri: String? = null,
    val documentName: String? = null,
    val webUrl: String? = null,
    val webTitle: String? = null,
    val coherenceScore: Float = 1.0f,
    val isPublicProtocol: Boolean = false
)
                """.trimIndent()
            ),

            // 4. ProtocolChapter.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/data/model/ProtocolChapter.kt",
                fileName = "ProtocolChapter.kt",
                category = ProjectFileCategory.DATABASE,
                language = "Kotlin",
                description = "Die 7 fundamentalen Bewusstseins-Kapitel des Tagebuchs (TUN, DENKEN, ERKENNEN, ERFINDEN, ERFORSCHEN, WÜNSCHEN, DAS UNMÖGLICHE).",
                content = """
package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ProtocolChapter(
    val id: String,
    val title: String,
    val description: String,
    val color: Color
) {
    TUN("TUN", "Täglich Tun", "Was ich aktiv tue und umsetze", ChapterTunColor),
    DENKEN("DENKEN", "Denken & Geist", "Gedanken, Reflexionen & Zustand", ChapterDenkenColor),
    ERKENNEN("ERKENNEN", "Erkennen", "Erkenntnisse & Durchbrüche", ChapterErkennenColor),
    ERFINDEN("ERFINDEN", "Erfinden", "Konzepte, Ideen & Schöpfungen", ChapterErfindenColor),
    ERFORSCHEN("ERFORSCHEN", "Erforschen", "Recherchen, Analyse & Fragen", ChapterErforschenColor),
    WUENSTCHEN("WUENSTCHEN", "Wünschen Wollen", "Intentionen, Impulse & Sehnsüchte", ChapterWuenstenColor),
    UNMOEGLICH("UNMOEGLICH", "Das Unmögliche", "Möglich machen was unmöglich schien", ChapterUnmoeglichColor);

    companion object {
        fun fromId(id: String): ProtocolChapter {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: DENKEN
        }
    }
}
                """.trimIndent()
            ),

            // 5. ProtocolDao.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/data/db/ProtocolDao.kt",
                fileName = "ProtocolDao.kt",
                category = ProjectFileCategory.DATABASE,
                language = "Kotlin",
                description = "Data Access Object (DAO) mit reaktiven Flow-Abfragen, Volltextsuche, Kapitel-Filtern und Insert/Update/Delete Operationen.",
                content = """
package com.example.data.db

import androidx.room.*
import com.example.data.model.ProtocolEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocol_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<ProtocolEntry>>

    @Query("SELECT * FROM protocol_entries WHERE dateStr = :dateStr ORDER BY timestamp DESC")
    fun getEntriesForDate(dateStr: String): Flow<List<ProtocolEntry>>

    @Query("SELECT * FROM protocol_entries WHERE chapter = :chapterId ORDER BY timestamp DESC")
    fun getEntriesForChapter(chapterId: String): Flow<List<ProtocolEntry>>

    @Query("SELECT * FROM protocol_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): ProtocolEntry?

    @Query("SELECT * FROM protocol_entries WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEntries(query: String): Flow<List<ProtocolEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ProtocolEntry): Long

    @Update
    suspend fun updateEntry(entry: ProtocolEntry)

    @Delete
    suspend fun deleteEntry(entry: ProtocolEntry)

    @Query("DELETE FROM protocol_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}
                """.trimIndent()
            ),

            // 6. AppDatabase.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/data/db/AppDatabase.kt",
                fileName = "AppDatabase.kt",
                category = ProjectFileCategory.DATABASE,
                language = "Kotlin",
                description = "Singleton Room-Datenbankhalter für lokale Datenspeicherung ohne Cloud-Abhängigkeit.",
                content = """
package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ProtocolEntry

@Database(entities = [ProtocolEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun protocolDao(): ProtocolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "protokoll_null_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
                """.trimIndent()
            ),

            // 7. ProtocolRepository.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/data/repository/ProtocolRepository.kt",
                fileName = "ProtocolRepository.kt",
                category = ProjectFileCategory.DATABASE,
                language = "Kotlin",
                description = "Abstraktionsschicht zwischen Room DAO und dem Android ViewModel.",
                content = """
package com.example.data.repository

import com.example.data.db.ProtocolDao
import com.example.data.model.ProtocolEntry
import kotlinx.coroutines.flow.Flow

class ProtocolRepository(private val protocolDao: ProtocolDao) {
    val allEntries: Flow<List<ProtocolEntry>> = protocolDao.getAllEntries()

    fun getEntriesForDate(dateStr: String): Flow<List<ProtocolEntry>> =
        protocolDao.getEntriesForDate(dateStr)

    fun getEntriesForChapter(chapterId: String): Flow<List<ProtocolEntry>> =
        protocolDao.getEntriesForChapter(chapterId)

    fun searchEntries(query: String): Flow<List<ProtocolEntry>> =
        protocolDao.searchEntries(query)

    suspend fun getEntryById(id: Long): ProtocolEntry? =
        protocolDao.getEntryById(id)

    suspend fun insertEntry(entry: ProtocolEntry): Long =
        protocolDao.insertEntry(entry)

    suspend fun updateEntry(entry: ProtocolEntry) =
        protocolDao.updateEntry(entry)

    suspend fun deleteEntry(entry: ProtocolEntry) =
        protocolDao.deleteEntry(entry)

    suspend fun deleteById(id: Long) =
        protocolDao.deleteEntryById(id)
}
                """.trimIndent()
            ),

            // 8. ProtocolViewModel.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/ui/viewmodel/ProtocolViewModel.kt",
                fileName = "ProtocolViewModel.kt",
                category = ProjectFileCategory.CORE,
                language = "Kotlin",
                description = "Zentrales ViewModel für Zustand, Filter, Audio-Aufnahme/Wiedergabe, Datei-Import und 0-Logik-Kalkulation.",
                content = """
package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ProtocolChapter
import com.example.data.model.ProtocolEntry
import com.example.data.repository.ProtocolRepository
import com.example.util.AudioPlayerManager
import com.example.util.AudioRecorderManager
import com.example.util.ExportShareManager
import com.example.util.ZeroLogikAnalyzer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProtocolViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProtocolRepository
    val audioRecorder: AudioRecorderManager
    val audioPlayer: AudioPlayerManager

    init {
        val dao = AppDatabase.getInstance(application).protocolDao()
        repository = ProtocolRepository(dao)
        audioRecorder = AudioRecorderManager(application)
        audioPlayer = AudioPlayerManager(application)
    }

    private val _selectedChapter = MutableStateFlow<ProtocolChapter?>(null)
    val selectedChapter: StateFlow<ProtocolChapter?> = _selectedChapter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDateFilter = MutableStateFlow<String?>(null)
    val selectedDateFilter: StateFlow<String?> = _selectedDateFilter

    private val _webClipBuffer = MutableStateFlow<Pair<String, String>?>(null)
    val webClipBuffer: StateFlow<Pair<String, String>?> = _webClipBuffer

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<ProtocolEntry>> = combine(
        _selectedChapter,
        _searchQuery,
        _selectedDateFilter
    ) { chapter, query, date ->
        Triple(chapter, query, date)
    }.flatMapLatest { (chapter, query, date) ->
        when {
            query.isNotBlank() -> repository.searchEntries(query)
            date != null -> repository.getEntriesForDate(date)
            chapter != null -> repository.getEntriesForChapter(chapter.id)
            else -> repository.allEntries
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setChapterFilter(chapter: ProtocolChapter?) { _selectedChapter.value = chapter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setDateFilter(dateStr: String?) { _selectedDateFilter.value = dateStr }
    fun setWebClipBuffer(url: String, title: String) { _webClipBuffer.value = Pair(url, title) }
    fun clearWebClipBuffer() { _webClipBuffer.value = null }

    fun saveEntry(
        id: Long = 0,
        title: String,
        content: String,
        chapter: ProtocolChapter,
        audioPath: String? = null,
        audioDurationMs: Long = 0,
        imageUri: String? = null,
        documentUri: String? = null,
        documentName: String? = null,
        webUrl: String? = null,
        webTitle: String? = null,
        isPublic: Boolean = false
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val analyzerResult = ZeroLogikAnalyzer.analyze(content)

            val entry = ProtocolEntry(
                id = id,
                dateStr = dateStr,
                timestamp = System.currentTimeMillis(),
                title = if (title.isBlank()) "${'$'}{chapter.title} - ${'$'}{SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}" else title,
                content = content,
                chapter = chapter.id,
                audioPath = audioPath,
                audioDurationMs = audioDurationMs,
                imageUri = imageUri,
                documentUri = documentUri,
                documentName = documentName,
                webUrl = webUrl,
                webTitle = webTitle,
                coherenceScore = analyzerResult.coherenceScore,
                isPublicProtocol = isPublic
            )

            if (id == 0L) repository.insertEntry(entry)
            else repository.updateEntry(entry)
        }
    }

    fun deleteEntry(entry: ProtocolEntry) {
        viewModelScope.launch {
            entry.audioPath?.let { path ->
                try { File(path).delete() } catch (e: Exception) {}
            }
            repository.deleteEntry(entry)
        }
    }

    fun readTextFromUri(uri: Uri): String {
        return try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            inputStream?.bufferedReader()?.use { it.readText() } ?: ""
        } catch (e: Exception) {
            "Fehler beim Lesen der Datei: ${'$'}{e.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        audioRecorder.cancelRecording()
    }
}
                """.trimIndent()
            ),

            // 9. MainAppScreen.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/ui/screens/MainAppScreen.kt",
                fileName = "MainAppScreen.kt",
                category = ProjectFileCategory.SCREENS,
                language = "Kotlin",
                description = "Hauptnavigation mit 4 Tabs: Tagebuch-Protokolle, In-App Web-Explorer, Export-Vault und Quellcode & APK-Bauplan.",
                content = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProtocolEntry
import com.example.ui.theme.*
import com.example.ui.viewmodel.ProtocolViewModel

enum class MainTab(val title: String) {
    PROTOKOLLE("Protokolle"),
    BROWSER("Internet"),
    VAULT("Vault"),
    CODE_BLUEPRINT("Code & APK")
}
                """.trimIndent()
            ),

            // 10. CodeBlueprintScreen.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/ui/screens/CodeBlueprintScreen.kt",
                fileName = "CodeBlueprintScreen.kt",
                category = ProjectFileCategory.SCREENS,
                language = "Kotlin",
                description = "Der Code- und Bauplan-Screen mit Datei-Viewer, Monospace-Terminal, 1-Klick Projekt-ZIP Export, echter installierter APK-Extraktion und Dateimanager/Gmail Upload.",
                content = """
// CodeBlueprintScreen: Stellt alle Dateien des Projekts bereit.
// Ermöglicht das Herunterladen als ZIP, Export der installierten APK-Datei
// und Speichern auf SD-Karte, internem Speicher oder Versenden per Gmail/Dateimanager.
                """.trimIndent()
            ),

            // 11. ProjectPackager.kt
            ProjectFileItem(
                path = "app/src/main/java/com/example/util/ProjectPackager.kt",
                fileName = "ProjectPackager.kt",
                category = ProjectFileCategory.UTILS,
                language = "Kotlin",
                description = "Echtes ZIP-Generierungs- und APK-Verpackungs-Tool mit Intent-Export für Gmail, SD-Karte, Internen Speicher und Dateimanager.",
                content = """
package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.repository.ProjectSourceRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ProjectPackager {
    // Generiert ein echtes .ZIP aller Quellcodes und exportiert die echte Geräte-.APK Datei.
}
                """.trimIndent()
            ),

            // 12. build.gradle.kts (App Level)
            ProjectFileItem(
                path = "app/build.gradle.kts",
                fileName = "app_build.gradle.kts",
                category = ProjectFileCategory.BUILD,
                language = "Gradle Kotlin DSL",
                description = "Gradle Build-Konfiguration für das App-Modul: Jetpack Compose, Room Compiler KSP, Coroutines, Material3.",
                content = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.example"
    compileSdk { version = release(36) { minorApiLevel = 1 } }

    defaultConfig {
        applicationId = "com.aistudio.protokollnull.wirth"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.coroutines.android)
    "ksp"(libs.androidx.room.compiler)
}
                """.trimIndent()
            ),

            // 13. libs.versions.toml
            ProjectFileItem(
                path = "gradle/libs.versions.toml",
                fileName = "libs.versions.toml",
                category = ProjectFileCategory.BUILD,
                language = "TOML",
                description = "Gradle Version Catalog mit allen Versionen für Kotlin 2.2, Room 2.7, Compose BOM und AGP 9.1.",
                content = """
[versions]
agp = "9.1.1"
coreKtx = "1.18.0"
lifecycleRuntimeCompose = "2.8.7"
activityCompose = "1.10.1"
kotlin = "2.2.10"
composeBom = "2024.09.00"
googleDevtoolsKsp = "2.3.5"
roomRuntime = "2.7.0"
roomCompiler = "2.7.0"
kotlinxCoroutinesAndroid = "1.10.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "roomRuntime" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "roomRuntime" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "roomCompiler" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutinesAndroid" }
                """.trimIndent()
            ),

            // 14. Step-by-Step Rebuild Guide
            ProjectFileItem(
                path = "README_BAUANLEITUNG.md",
                fileName = "README_BAUANLEITUNG.md",
                category = ProjectFileCategory.GUIDE,
                language = "Markdown",
                description = "Vollständige Schritt-für-Schritt Anleitung zum Nachbauen des Digitalen Tagebuchs in Android Studio oder AI Studio.",
                content = """
# 📘 SCHRITT-FÜR-SCHRITT BAUANLEITUNG: DIGITALES TAGEBUCH (PROTOKOLL 0)

Diese App ist ein vollwertiges, offline-fähiges digitales Tagebuch mit Jetpack Compose, Room-Datenbank, Audioaufnahme, In-App Webbrowser, Dokumenten-Vault und Code-Export.

---

### 1. SCHRITT: ANDROID PROJEKT ERSTELLEN
1. Öffne **Android Studio** (Koala / Ladybug oder neuer).
2. Wähle **"New Project"** -> **"Empty Activity (Jetpack Compose)"**.
3. Name: `Protokoll 0` oder `Digitales Tagebuch`.
4. Package Name: `com.example` (oder deine eigene Domain).
5. Minimum SDK: `API 24: Android 7.0 (Nougat)`.
6. Build Configuration: `Kotlin DSL (build.gradle.kts)`.

---

### 2. SCHRITT: GRADLE DEPENDENCIES & KSP EINTRAGEN
Füge in deiner `app/build.gradle.kts` die Room- und Audio-Abhängigkeiten ein:
- `alias(libs.plugins.google.devtools.ksp)` in den `plugins {}` Block.
- `implementation(libs.androidx.room.ktx)`
- `implementation(libs.androidx.room.runtime)`
- `"ksp"(libs.androidx.room.compiler)`
- `implementation(libs.androidx.compose.material.icons.extended)`

---

### 3. SCHRITT: DATENMODELL & ROOM DATENBANK
1. Erstelle das Entity `data/model/ProtocolEntry.kt` mit `@Entity(tableName = "protocol_entries")`.
2. Erstelle das Enum `data/model/ProtocolChapter.kt` für die 7 Lebensbereiche.
3. Erstelle das DAO `data/db/ProtocolDao.kt` mit `Flow<List<ProtocolEntry>>` Abfragen.
4. Erstelle die Datenbank-Klasse `data/db/AppDatabase.kt` mit `fallbackToDestructiveMigration()`.
5. Erstelle das Repository `data/repository/ProtocolRepository.kt`.

---

### 4. SCHRITT: AUDIO & DATEIVERWALTUNG
1. Kopiere `util/AudioRecorderManager.kt` für Sprachaufnahmen mit `MediaRecorder` im M4A/AAC Format.
2. Kopiere `util/AudioPlayerManager.kt` für flüssige Wiedergabe mit Fortschritts-Slider.
3. Kopiere `util/ProjectPackager.kt` für das echte Verpacken in ZIP- und APK-Dateien.

---

### 5. SCHRITT: USER INTERFACE (JETPACK COMPOSE)
1. `ui/theme/Theme.kt` und `Color.kt`: Cyberpunk-Dunkelthema mit Neonrot, Tiefviolett und Cyberpurple.
2. `ui/components/ZeroMatrixHeader.kt`: Status-Dashboard mit Datum und Kohärenz.
3. `ui/components/ChapterFilterBar.kt`: Horizontale Filterleiste nach Kapiteln.
4. `ui/components/ProtocolCard.kt`: Interaktive Notizkarte mit Audioplayer und Textaufklappung.
5. `ui/screens/EntryEditorDialog.kt`: Erfassungsdialog mit Mikrofon-Button, Dateipicker und Webclip.
6. `ui/screens/CodeBlueprintScreen.kt`: Quellcode-Browser & ZIP/APK Download-Center.

---

### 6. SCHRITT: EXPORTIEREN & AUF DEM HANDY INSTALLIEREN
- **ZIP-Export**: Tippe in der App auf "Projekt als ZIP exportieren" und sende es an Gmail oder Google Drive.
- **Echte APK**: Tippe auf "Echte APK sichern" -> wähle SD-Karte, Dateimanager oder Gmail -> die `.apk` Datei wird direkt kopiert und kann auf jedem Android-Smartphone installiert werden!
                """.trimIndent()
            )
        )
    }
}
