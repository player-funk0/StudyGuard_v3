package com.obrynex.studyguard.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obrynex.studyguard.data.StudySession
import com.obrynex.studyguard.ui.adaptive.contentHorizontalPadding
import com.obrynex.studyguard.ui.theme.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackerScreen(
    vm: TrackerViewModel,
    windowSizeClass: WindowSizeClass? = null,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAI: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToBookSummarizer: () -> Unit = {},
    onNavigateToWellbeing: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {}
) {
    val state by vm.state.collectAsStateWithLifecycle()
    // Adaptive horizontal padding: compact=16dp, medium=24dp, expanded=48dp
    val hPad = windowSizeClass?.contentHorizontalPadding ?: 16.dp
    // On expanded (tablet) screens, cap content width so it doesn't stretch wall-to-wall
    val isExpanded = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = hPad, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Study Tracker",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (state.totalSessions > 0) {
                    Text(
                        "${state.totalSessions} sessions · ${state.todayMinutes} min today",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
            IconButton(onClick = vm::refresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = TextMuted
                )
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // ── Stats ───────────────────────────────────────────────────────
        // On tablets, constrain the stat row so cards don't become ludicrously wide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(hPad)
                .then(if (isExpanded) Modifier.widthIn(max = 600.dp) else Modifier),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                value = "${state.todayMinutes}",
                label = "Today (min)",
                modifier = Modifier.weight(1f)
            )
            StatBox(
                value = "${state.weekMinutes}",
                label = "Week (min)",
                modifier = Modifier.weight(1f)
            )
            StatBox(
                value = "${state.streak}",
                label = "Streak",
                modifier = Modifier.weight(1f)
            )
        }

        // ── Session History ───────────────────────────────────────────────
        Text(
            "Session History",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = hPad, vertical = 8.dp)
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGreen)
            }
        } else if (state.sessions.isEmpty()) {
            EmptyTrackerState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = hPad, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.sessions, key = { it.id }) { session ->
                    SessionRow(
                        session = session,
                        // On expanded screens, cap session cards at a readable width
                        modifier = if (isExpanded)
                            Modifier.widthIn(max = 700.dp) else Modifier,
                        onClick = { onNavigateToDetail(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = AccentGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun SessionRow(
    session: StudySession,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault()) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                session.subject.ifBlank { "General Study" },
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                dateFormat.format(session.date),
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${session.durationMinutes} min",
                color = if (session.completed) AccentGreen else Color(0xFFFFB300),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (session.completed) AccentGreen else Color(0xFFFFB300),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EmptyTrackerState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📊", fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "No sessions yet",
            color = TextPrimary,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Start a timer to record your first study session",
            color = TextMuted,
            fontSize = 13.sp
        )
    }
}
