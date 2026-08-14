package com.example.data.model

data class ProjectFileItem(
    val path: String,
    val fileName: String,
    val category: ProjectFileCategory,
    val language: String,
    val description: String,
    val content: String
)

enum class ProjectFileCategory(val displayName: String) {
    ALL("Alle Dateien"),
    CORE("Core & Manifest"),
    DATABASE("Room Datenbank"),
    SCREENS("UI & Screens"),
    COMPONENTS("Komponenten & Theme"),
    UTILS("Audio & Packager Tools"),
    BUILD("Gradle & Konfiguration"),
    GUIDE("Bauanleitung")
}
