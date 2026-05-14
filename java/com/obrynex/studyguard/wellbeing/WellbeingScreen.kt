package com.obrynex.studyguard.wellbeing

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obrynex.studyguard.ui.adaptive.contentHorizontalPadding
import com.obrynex.studyguard.ui.theme.*

@Composable
fun WellbeingScreen(
    vm: WellbeingViewModel,
    windowSizeClass: WindowSizeClass? = null,
    onBack: (() -> Unit)? = null
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val hPad = windowSizeClass?.contentHorizontalPadding ?: 16.dp
    val isExpanded = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = hPad, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Column {
                    Text(
                        "Digital Wellbeing",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Monitor your screen time",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
            IconButton(onClick = vm::refresh) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = "Refresh",
                    tint = TextMuted
                )
            }
        }

        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        if (!state.hasPermission) {
            // Permission request state
            PermissionRequiredCard(
                onGrant = {
                    ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )
        } else {
            // Wrap body in a centered Box so tablets get a max-width cap
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
            Column(
                modifier = Modifier
                    .then(if (isExpanded) Modifier.widthIn(max = 700.dp) else Modifier.fillMaxWidth())
            ) {
            // Today's screen time summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(hPad),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today",
                    value = WellbeingViewModel.formatDuration(state.todayScreenTimeMs),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Most Used Apps",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = hPad, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = hPad, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.topApps) { app ->
                    AppUsageCard(app = app)
                }

                if (state.topApps.isEmpty()) {
                    item {
                        EmptyWellbeingState()
                    }
                }
            }
            } // end Column (tablet-width-capped)
            } // end Box (tablet centering wrapper)
        }
    }
}

@Composable
private fun PermissionRequiredCard(onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(48.dp)
        )
        Text(
            "Permission Required",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            "StudyGuard needs access to usage statistics to track your screen time.",
            color = TextMuted,
            fontSize = 13.sp
        )
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Grant Permission", color = BgDark, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = AccentGreen, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(title, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun AppUsageCard(app: AppUsageInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface3),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    app.appName.firstOrNull()?.uppercase() ?: "?",
                    color = AccentGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text(
                    app.appName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    app.packageName.substringBeforeLast(".", app.packageName),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
        Text(
            WellbeingViewModel.formatDuration(app.usageTimeMs),
            color = AccentGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyWellbeingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No app usage data yet", color = TextPrimary, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Usage data will appear as you use other apps",
            color = TextMuted,
            fontSize = 12.sp
        )
    }
}
