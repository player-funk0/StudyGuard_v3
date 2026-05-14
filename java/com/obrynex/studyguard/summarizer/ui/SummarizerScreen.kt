package com.obrynex.studyguard.summarizer.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obrynex.studyguard.ai.AIModelState
import com.obrynex.studyguard.ui.adaptive.contentHorizontalPadding
import com.obrynex.studyguard.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SummarizerScreen(
    vm: SummarizerViewModel,
    windowSizeClass: WindowSizeClass? = null
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hPad = windowSizeClass?.contentHorizontalPadding ?: 16.dp
    val isExpanded = windowSizeClass?.widthSizeClass ==
        androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Expanded

    Scaffold(
        containerColor = BgDark,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        // On tablets, center content up to a comfortable reading width
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .then(if (isExpanded) Modifier.widthIn(max = 700.dp) else Modifier.fillMaxWidth())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = hPad, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Text Summarizer",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Paste text to get a summary",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Row {
                    if (state.summaryText.isNotEmpty()) {
                        IconButton(onClick = vm::clear) {
                            Icon(Icons.Default.Refresh, "Clear", tint = TextMuted)
                        }
                    }
                }
            }

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(hPad),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use AI Model", color = TextMuted, fontSize = 13.sp)
                    Switch(
                        checked = state.useAI,
                        onCheckedChange = { vm.toggleUseAI() },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentGreen)
                    )
                }

                // Summary level selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryLevel.values().forEach { level ->
                        val selected = state.summaryLevel == level
                        FilterChip(
                            selected = selected,
                            onClick = { vm.onSummaryLevelChanged(level) },
                            label = { Text(level.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
                                selectedLabelColor = AccentGreen,
                                containerColor = Surface2,
                                labelColor = TextMuted
                            )
                        )
                    }
                }

                // Input area
                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = vm::onInputChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp),
                    placeholder = { Text("Paste your text here...", color = TextMuted.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = Divider,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Surface2,
                        unfocusedContainerColor = Surface2
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    )
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Paste button
                    OutlinedButton(
                        onClick = {
                            clipboard.getText()?.let { vm.onInputChanged(it.text) }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SolidColor(Divider))
                    ) {
                        Icon(Icons.Outlined.ContentPaste, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Paste", color = TextMuted, fontSize = 13.sp)
                    }

                    // Summarize button
                    Button(
                        onClick = vm::summarize,
                        enabled = state.inputText.isNotBlank() && !state.isSummarizing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSummarizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = BgDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.AutoStories, null, tint = BgDark, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (state.isSummarizing) "Summarizing..." else "Summarize",
                            color = BgDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Error
                AnimatedVisibility(visible = state.error != null) {
                    state.error?.let { error ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF5252).copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(error, color = Color(0xFFFF5252), fontSize = 13.sp)
                        }
                    }
                }

                // Summary result
                AnimatedVisibility(
                    visible = state.summaryText.isNotEmpty(),
                    enter = fadeIn() + expandVertically()
                ) {
                    SummaryResultCard(
                        summary = state.summaryText,
                        onCopy = {
                            clipboard.setText(AnnotatedString(state.summaryText))
                            scope.launch { snackbar.showSnackbar("Copied to clipboard") }
                        }
                    )
                }
            }
        } // end Column (tablet-width-capped)
        } // end Box (tablet centering wrapper)
    }
}

@Composable
private fun SummaryResultCard(
    summary: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Summary", color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ContentCopy, "Copy", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            summary,
            color = TextPrimary,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            style = LocalTextStyle.current.copy(textDirection = TextDirection.Content)
        )
    }
}
