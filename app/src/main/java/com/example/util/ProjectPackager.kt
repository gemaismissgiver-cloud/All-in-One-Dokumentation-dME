package com.example.util

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ProjectFileItem
import com.example.data.repository.ProjectSourceRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ProjectPackager {

    private const val TAG = "ProjectPackager"

    /**
     * Generates a real ZIP archive containing all project source code, configurations, and build files.
     */
    fun createProjectZipFile(context: Context): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val zipFile = File(exportDir, "Digitales_Tagebuch_Projekt_$timestamp.zip")

            val files = ProjectSourceRepository.getAllProjectFiles()

            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                for (fileItem in files) {
                    // Create zip entry with relative path
                    val entry = ZipEntry(fileItem.path)
                    zipOut.putNextEntry(entry)
                    val data = fileItem.content.toByteArray(Charsets.UTF_8)
                    zipOut.write(data, 0, data.size)
                    zipOut.closeEntry()
                }
            }

            Log.d(TAG, "Successfully created project zip at: ${zipFile.absolutePath} (Size: ${zipFile.length()} bytes)")
            zipFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating project zip", e)
            null
        }
    }

    /**
     * Packages all project files into a ZIP archive and triggers the Android Share Sheet
     * (Allows saving to Gmail, SD Card, Drive, Dateimanager, WhatsApp, Bluetooth, etc.).
     */
    fun exportAndShareProjectZip(context: Context) {
        val zipFile = createProjectZipFile(context)
        if (zipFile == null || !zipFile.exists()) {
            Toast.makeText(context, "Fehler beim Erstellen der ZIP-Datei", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Digitales Tagebuch - Vollständiges Android-Projekt (ZIP)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hier ist das vollständige Android Studio Projekt für das digitale Tagebuch inklusive aller Quellcodes, Room Datenbank und Gradle Build-Dateien zum Nachbauen."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Projekt-ZIP Speichern / Hochladen (Gmail, SD-Karte, Drive, Dateimanager)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing project zip", e)
            Toast.makeText(context, "Fehler beim Teilen der ZIP-Datei: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Extracts the real, installed base APK file from device storage (context.applicationInfo.sourceDir),
     * copies it with a clean name, and opens the Share Sheet to upload via Gmail, save to SD Card, etc.
     */
    fun exportAndShareInstalledApk(context: Context) {
        try {
            val sourceApkPath = context.applicationInfo.sourceDir
            if (sourceApkPath.isNullOrBlank()) {
                Toast.makeText(context, "Keine installierte APK-Datei gefunden", Toast.LENGTH_SHORT).show()
                return
            }

            val sourceFile = File(sourceApkPath)
            if (!sourceFile.exists()) {
                Toast.makeText(context, "APK-Datei existiert nicht im Dateisystem", Toast.LENGTH_SHORT).show()
                return
            }

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val targetApk = File(exportDir, "Digitales_Tagebuch_v1.0_$timestamp.apk")

            // Copy APK stream
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetApk).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Successfully extracted APK: ${targetApk.absolutePath} (${targetApk.length()} bytes)")

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetApk
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Digitales Tagebuch - Installierbare APK Datei")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hier ist die installierbare Android APK-Datei des Digitalen Tagebuchs (Protokoll 0). Kann direkt auf dem Telefon oder der SD-Karte gespeichert und installiert werden."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "APK-Datei Speichern / Hochladen (Gmail, SD-Karte, Dateimanager)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting APK", e)
            Toast.makeText(context, "Fehler beim Exportieren der APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exports a single source code file (.kt, .xml, .kts, .toml) and allows sharing/saving it.
     */
    fun exportSingleFile(context: Context, fileItem: ProjectFileItem) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, fileItem.fileName)
            file.writeText(fileItem.content, Charsets.UTF_8)

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Datei: ${fileItem.fileName}")
                putExtra(Intent.EXTRA_TEXT, fileItem.content)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Datei '${fileItem.fileName}' Speichern / Senden")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting single file", e)
            Toast.makeText(context, "Fehler: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the Files App / Dateimanager.
     */
    fun openFilesAppOrManager(context: Context) {
        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: Open general file manager intent
            try {
                val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Dateimanager / Files App öffnen"))
            } catch (e2: Exception) {
                Toast.makeText(context, "Dateimanager konnte nicht direkt geöffnet werden.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Copies code to clipboard with Toast notification.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Quellcode") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "'$label' in die Zwischenablage kopiert!", Toast.LENGTH_SHORT).show()
    }
}
