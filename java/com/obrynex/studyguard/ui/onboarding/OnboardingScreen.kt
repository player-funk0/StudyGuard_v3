package com.obrynex.studyguard.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrynex.studyguard.ui.theme.*

/**
 * Onboarding screen shown on first app launch.
 */
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Page content
        AnimatedContent(
            targetState = currentPage,
            label = "onboarding_page",
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it / 2 }) togetherWith
                (fadeOut() + slideOutHorizontally { -it / 2 })
            }
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    icon = Icons.Default.Timer,
                    title = "Study Timer",
                    description = "Set focused study sessions with customizable durations. Track your breaks and stay productive."
                )
                1 -> OnboardingPage(
                    icon = Icons.Default.Psychology,
                    title = "AI Tutor",
                    description = "Ask questions and get answers powered by on-device AI. No internet required after setup."
                )
                2 -> OnboardingPage(
                    icon = Icons.Default.AutoStories,
                    title = "Book Summarizer",
                    description = "Import long texts and get AI-powered summaries using the Map-Reduce technique."
                )
                3 -> OnboardingPage(
                    icon = Icons.Default.PhoneAndroid,
                    title = "Digital Wellbeing",
                    description = "Monitor your screen time and build healthier digital habits."
                )
                else -> OnboardingPage(
                    icon = Icons.Default.Timer,
                    title = "Welcome",
                    description = "Let's get started!"
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // Page indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .width(if (index == currentPage) 24.dp else 8.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (index == currentPage) AccentGreen else Surface3)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Navigation button
        Button(
            onClick = {
                if (currentPage < totalPages - 1) {
                    currentPage++
                } else {
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                if (currentPage < totalPages - 1) "Next" else "Get Started",
                color = BgDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AccentGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(40.dp)
            )
        }
        Text(
            title,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            description,
            color = TextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
