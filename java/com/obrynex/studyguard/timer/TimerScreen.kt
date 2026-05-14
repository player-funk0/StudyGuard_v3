package com.obrynex.studyguard.timer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ManageHistory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obrynex.studyguard.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Study Timer screen — primary composable for the "المذاكرة" tab.
 *
 * Bug #3 fix: [SnackbarHostState.showSnackbar] returns [SnackbarResult] (non-nullable).
 * The original code used `?.let { }` treating the result as nullable — compile error.
 */
@Composable
fun TimerScreen(
    vm: TimerViewModel,
    onNavigateToTracker: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show completion snackbar when the session finishes
    LaunchedEffect(state.isFinished) {
        if (!state.isFinished) return@LaunchedEffect
        scope.launch {
            // ✅ showSnackbar returns SnackbarResult — NOT nullable, no ?. needed
            val result = snackbarHostState.showSnackbar(
                message     = "اكتملت الجلسة! أحسنت 🎉",
                actionLabel = "عرض السجل",
                duration    = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onNavigateToTracker()
            }
            vm.resetTimer()
        }
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // Fix #3: detect landscape vs portrait at runtime so the fixed 220dp circle
        // and pure-vertical stack don't overflow on short landscape phones.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val layoutMaxWidth = this@BoxWithConstraints.maxWidth
            val layoutMaxHeight = this@BoxWithConstraints.maxHeight
            val isLandscape = layoutMaxWidth > layoutMaxHeight

            if (isLandscape) {
                // ── Landscape layout: timer left, controls right ─────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Shared header (always full-width at top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "المذاكرة",
                                color      = TextPrimary,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (state.completedSessions > 0) {
                                Text(
                                    "${state.completedSessions} جلسة اليوم",
                                    color    = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToTracker) {
                            Icon(
                                imageVector = Icons.Outlined.ManageHistory,
                                contentDescription = "سجل الجلسات",
                                tint = TextMuted
                            )
                        }
                    }
                    HorizontalDivider(color = Divider, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: timer circle (scaled down for landscape)
                        val circleSize = (layoutMaxHeight * 0.65f).coerceAtMost(180.dp)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(circleSize)
                        ) {
                            CircularProgressIndicator(
                                progress = { state.progress },
                                modifier  = Modifier.fillMaxSize(),
                                color     = if (state.phase is TimerPhase.Break) AccentBlue else AccentGreen,
                                strokeWidth = 6.dp,
                                trackColor  = Surface2
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text       = String.format("%02d:%02d", state.displayMinutes, state.displaySeconds),
                                    color      = TextPrimary,
                                    fontSize   = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (state.phase is TimerPhase.Break) {
                                    Text("استراحة", color = AccentBlue, fontSize = 12.sp)
                                } else if (state.phase is TimerPhase.Paused) {
                                    Text("متوقف مؤقتاً", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }

                        // Right: subject + duration selectors + controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                        ) {
                            OutlinedTextField(
                                value = state.subject,
                                onValueChange = vm::onSubjectChanged,
                                placeholder = { Text("المادة (اختياري)", color = TextMuted) },
                                enabled = state.isIdle,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = AccentGreen,
                                    unfocusedBorderColor = Divider,
                                    focusedTextColor     = TextPrimary,
                                    unfocusedTextColor   = TextPrimary,
                                    disabledTextColor    = TextMuted,
                                    disabledBorderColor  = Divider
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (state.isIdle) {
                                DurationSelector(
                                    label       = "مدة المذاكرة",
                                    minutes     = state.durationMinutes,
                                    onDecrement = { vm.onDurationChanged(maxOf(5, state.durationMinutes - 5)) },
                                    onIncrement = { vm.onDurationChanged(minOf(120, state.durationMinutes + 5)) }
                                )
                                DurationSelector(
                                    label       = "مدة الاستراحة",
                                    minutes     = state.breakMinutes,
                                    onDecrement = { vm.onBreakChanged(maxOf(1, state.breakMinutes - 1)) },
                                    onIncrement = { vm.onBreakChanged(minOf(30, state.breakMinutes + 1)) }
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("استراحة تلقائية", color = TextMuted, fontSize = 13.sp)
                                    Switch(
                                        checked         = state.isBreakEnabled,
                                        onCheckedChange = vm::onBreakEnabledChanged,
                                        colors          = SwitchDefaults.colors(checkedThumbColor = AccentGreen)
                                    )
                                }
                            }

                            // Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                when {
                                    state.isIdle || state.isFinished -> {
                                        Button(
                                            onClick = vm::startTimer,
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                            shape  = RoundedCornerShape(14.dp)
                                        ) {
                                            Text("ابدأ المذاكرة", color = BgDark, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    state.phase is TimerPhase.Break -> {
                                        Text("جلسة الاستراحة جارية…", color = AccentBlue, fontSize = 14.sp)
                                    }
                                    else -> {
                                        OutlinedButton(
                                            onClick = vm::stopTimer,
                                            modifier = Modifier.height(48.dp),
                                            border  = BorderStroke(1.dp, SolidColor(Divider)),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text("إيقاف", color = TextMuted)
                                        }
                                        Button(
                                            onClick  = vm::togglePauseResume,
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                            shape    = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(
                                                if (state.isPaused) "استئناف" else "إيقاف مؤقت",
                                                color      = BgDark,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ── Portrait layout (adaptive) ────────────────────────────────
                // Circle and spacers scale with available height so the layout
                // never overflows on small phones (4"–5") or foldables.
                val circleSize    = (layoutMaxHeight * 0.38f).coerceIn(160.dp, 240.dp)
                val verticalGap   = (layoutMaxHeight * 0.05f).coerceIn(12.dp, 44.dp)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = verticalGap.coerceAtMost(20.dp)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "المذاكرة",
                                color      = TextPrimary,
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (state.completedSessions > 0) {
                                Text(
                                    "${state.completedSessions} جلسة اليوم",
                                    color    = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToTracker) {
                            Icon(
                                imageVector = Icons.Outlined.ManageHistory,
                                contentDescription = "سجل الجلسات",
                                tint = TextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = Divider, thickness = 0.5.dp)
                    Spacer(Modifier.height(verticalGap))

                    OutlinedTextField(
                        value = state.subject,
                        onValueChange = vm::onSubjectChanged,
                        placeholder = { Text("المادة (اختياري)", color = TextMuted) },
                        enabled = state.isIdle,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = AccentGreen,
                            unfocusedBorderColor = Divider,
                            focusedTextColor     = TextPrimary,
                            unfocusedTextColor   = TextPrimary,
                            disabledTextColor    = TextMuted,
                            disabledBorderColor  = Divider
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(verticalGap))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(circleSize)
                    ) {
                        CircularProgressIndicator(
                            progress = { state.progress },
                            modifier  = Modifier.fillMaxSize(),
                            color     = if (state.phase is TimerPhase.Break) AccentBlue else AccentGreen,
                            strokeWidth = 6.dp,
                            trackColor  = Surface2
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val timeFontSize = (circleSize.value * 0.21f).coerceIn(28f, 52f)
                            Text(
                                text       = String.format("%02d:%02d", state.displayMinutes, state.displaySeconds),
                                color      = TextPrimary,
                                fontSize   = timeFontSize.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.phase is TimerPhase.Break) {
                                Text("استراحة", color = AccentBlue, fontSize = 14.sp)
                            } else if (state.phase is TimerPhase.Paused) {
                                Text("متوقف مؤقتاً", color = TextMuted, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(verticalGap))

                    if (state.isIdle) {
                        DurationSelector(
                            label        = "مدة المذاكرة",
                            minutes      = state.durationMinutes,
                            onDecrement  = { vm.onDurationChanged(maxOf(5, state.durationMinutes - 5)) },
                            onIncrement  = { vm.onDurationChanged(minOf(120, state.durationMinutes + 5)) }
                        )
                        Spacer(Modifier.height(12.dp))
                        DurationSelector(
                            label        = "مدة الاستراحة",
                            minutes      = state.breakMinutes,
                            onDecrement  = { vm.onBreakChanged(maxOf(1, state.breakMinutes - 1)) },
                            onIncrement  = { vm.onBreakChanged(minOf(30, state.breakMinutes + 1)) }
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("استراحة تلقائية", color = TextMuted, fontSize = 14.sp)
                            Switch(
                                checked         = state.isBreakEnabled,
                                onCheckedChange = vm::onBreakEnabledChanged,
                                colors          = SwitchDefaults.colors(checkedThumbColor = AccentGreen)
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when {
                            state.isIdle || state.isFinished -> {
                                Button(
                                    onClick = vm::startTimer,
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                    shape  = RoundedCornerShape(14.dp)
                                ) {
                                    Text("ابدأ المذاكرة", color = BgDark, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            state.phase is TimerPhase.Break -> {
                                Text("جلسة الاستراحة جارية…", color = AccentBlue, fontSize = 15.sp)
                            }
                            else -> {
                                OutlinedButton(
                                    onClick = vm::stopTimer,
                                    modifier = Modifier.height(52.dp),
                                    border  = BorderStroke(1.dp, SolidColor(Divider)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("إيقاف", color = TextMuted)
                                }
                                Button(
                                    onClick  = vm::togglePauseResume,
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                    shape    = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        if (state.isPaused) "استئناف" else "إيقاف مؤقت",
                                        color      = BgDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DurationSelector(
    label: String,
    minutes: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextMuted, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Text("−", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("$minutes د", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Text("+", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
