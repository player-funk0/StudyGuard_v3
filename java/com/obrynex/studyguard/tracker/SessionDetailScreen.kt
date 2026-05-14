package com.obrynex.studyguard.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrynex.studyguard.data.StudySession
import com.obrynex.studyguard.ui.theme.AccentGreen
import com.obrynex.studyguard.ui.theme.BgDark
import com.obrynex.studyguard.ui.theme.Divider
import com.obrynex.studyguard.ui.theme.Surface2
import com.obrynex.studyguard.ui.theme.TextMuted
import com.obrynex.studyguard.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SessionDetailScreen(
    session: StudySession,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Session Details",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF6B6B))
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)
        Spacer(Modifier.height(16.dp))

        DetailCard(
            title = session.subject.ifBlank { "General Study" },
            status = if (session.completed) "Completed" else "Stopped early",
            duration = "${session.durationMinutes} min",
            date = dateFormat.format(session.date),
            paused = formatPaused(session.pausedTimeMs)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Delete Session", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Medium)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete session?", color = TextPrimary) },
            text = { Text("This action cannot be undone.", color = TextMuted) },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            }
        )
    }
}

@Composable
private fun DetailCard(
    title: String,
    status: String,
    duration: String,
    date: String,
    paused: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Text(text = status, color = if (status == "Completed") AccentGreen else TextMuted, fontSize = 13.sp)
        HorizontalDivider(color = Divider, thickness = 0.5.dp)
        Text(text = "Duration: $duration", color = TextPrimary, fontSize = 14.sp)
        Text(text = "Date: $date", color = TextPrimary, fontSize = 14.sp)
        Text(text = "Paused: $paused", color = TextPrimary, fontSize = 14.sp)
    }
}

private fun formatPaused(pausedMs: Long): String {
    if (pausedMs <= 0L) return "0 sec"
    val totalSeconds = pausedMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}
