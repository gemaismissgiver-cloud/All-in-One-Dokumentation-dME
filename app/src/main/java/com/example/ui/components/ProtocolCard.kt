package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProtocolChapter
import com.example.data.model.ProtocolEntry
import com.example.ui.theme.*
import com.example.util.AudioPlayerManager
import com.example.util.ExportShareManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProtocolCard(
    entry: ProtocolEntry,
    audioPlayer: AudioPlayerManager,
    onEdit: (ProtocolEntry) -> Unit,
    onDelete: (ProtocolEntry) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chapter = remember(entry.chapter) { ProtocolChapter.fromId(entry.chapter) }
    val formattedTime = remember(entry.timestamp) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(Date(entry.timestamp))
    }

    val isPlayingAudio by audioPlayer.isPlaying.collectAsState()
    val playingPath by audioPlayer.currentPath.collectAsState()
    val isThisPlaying = isPlayingAudio && playingPath == entry.audioPath

    val currentPos by audioPlayer.currentPosition.collectAsState()
    val totalDuration by audioPlayer.duration.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("protocol_card_${entry.id}")
            .border(
                width = 1.dp,
                color = chapter.color.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Chapter Badge & Timestamp & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chapter Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(chapter.color.copy(alpha = 0.2f))
                        .border(1.dp, chapter.color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chapter.title,
                        color = chapter.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { ExportShareManager.shareEntryText(context, entry) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("share_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Teilen",
                            tint = OffWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDelete(entry) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Löschen",
                            tint = NeonRedPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = entry.title,
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onEdit(entry) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Content
            Text(
                text = entry.content,
                color = OffWhite,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )

            if (entry.content.length > 180) {
                Text(
                    text = if (isExpanded) "▲ Weniger anzeigen" else "▼ Mehr anzeigen",
                    color = ElectricViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { isExpanded = !isExpanded }
                )
            }

            // Web URL attachment chip
            if (!entry.webUrl.isNull_or_empty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    onClick = { entry.webUrl?.let { onOpenUrl(it) } },
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(0.5.dp, ChapterDenkenColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web Link",
                            tint = ChapterDenkenColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = entry.webTitle ?: entry.webUrl ?: "",
                            color = ChapterDenkenColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Document Attachment Pill
            if (!entry.documentName.isNull_or_empty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(0.5.dp, ChapterErfindenColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Dokument",
                            tint = ChapterErfindenColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Datei: ${entry.documentName}",
                            color = OffWhite,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Audio Player Bar if Voice Note attached
            if (!entry.audioPath.isNull_or_empty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { entry.audioPath?.let { audioPlayer.playOrPause(it) } },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberPurple)
                        ) {
                            Icon(
                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Sprachnachricht Abspielen",
                                tint = PureWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sprachprotokoll Aufgenommen",
                                    color = OffWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isThisPlaying && totalDuration > 0) {
                                        "${formatTimeMs(currentPos)} / ${formatTimeMs(totalDuration)}"
                                    } else {
                                        formatTimeMs(entry.audioDurationMs)
                                    },
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (isThisPlaying && totalDuration > 0) {
                                Slider(
                                    value = currentPos.toFloat(),
                                    onValueChange = { audioPlayer.seekTo(it.toInt()) },
                                    valueRange = 0f..totalDuration.toFloat(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonRedPrimary,
                                        activeTrackColor = CyberPurple
                                    ),
                                    modifier = Modifier
                                        .height(20.dp)
                                        .padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Info Bar: Coherence Meter
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (entry.coherenceScore >= 0.8f) ChapterErkennenColor
                                else if (entry.coherenceScore >= 0.5f) ChapterWuenstenColor
                                else NeonRedPrimary
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0-Logik: ${String.format(Locale.US, "%.0f%%", entry.coherenceScore * 100)}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Bearbeiten",
                    color = ElectricViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onEdit(entry) }
                )
            }
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.isBlank()

private fun formatTimeMs(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.GERMAN, "%02d:%02d", minutes, seconds)
}

private fun formatTimeMs(timeMs: Int): String = formatTimeMs(timeMs.toLong())
