package id.web.idm.forcerotation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.web.idm.forcerotation.domain.RotaxUiState
import id.web.idm.forcerotation.ui.components.FloatingControlPreview
import id.web.idm.forcerotation.ui.theme.CyanAccent
import id.web.idm.forcerotation.ui.theme.CyanAccentDim
import id.web.idm.forcerotation.ui.theme.DarkBackground
import id.web.idm.forcerotation.ui.theme.DarkOutline
import id.web.idm.forcerotation.ui.theme.DarkSurfaceElevated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: RotaxUiState,
    onBackClick: () -> Unit,
    onToggleFloating: (Boolean) -> Unit,
    onResetFloatingPosition: () -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onSetSafeRestore: (Boolean) -> Unit,
    onTriggerTestNotification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        onBackClick()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Diagnostics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("button_settings_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWideScreen = maxWidth >= 600.dp

            if (isWideScreen) {
                // Adaptive 2-Column Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Overlay & Notification Controls
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FloatingControllerSection(
                            floatingEnabled = uiState.floatingEnabled,
                            onToggleFloating = onToggleFloating,
                            onResetFloatingPosition = onResetFloatingPosition
                        )

                        NotificationSection(
                            notificationEnabled = uiState.notificationEnabled,
                            onToggleNotification = onToggleNotification,
                            onTriggerTestNotification = onTriggerTestNotification
                        )

                        SystemBehaviorSection(
                            safeRestoreEnabled = uiState.safeRestoreEnabled,
                            onSetSafeRestore = onSetSafeRestore
                        )
                    }

                    // Right Column: Live Diagnostics & About
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LiveDiagnosticsSection(uiState = uiState)
                        AboutSection()
                    }
                }
            } else {
                // Single Column Layout for Phones
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingControllerSection(
                        floatingEnabled = uiState.floatingEnabled,
                        onToggleFloating = onToggleFloating,
                        onResetFloatingPosition = onResetFloatingPosition
                    )

                    NotificationSection(
                        notificationEnabled = uiState.notificationEnabled,
                        onToggleNotification = onToggleNotification,
                        onTriggerTestNotification = onTriggerTestNotification
                    )

                    SystemBehaviorSection(
                        safeRestoreEnabled = uiState.safeRestoreEnabled,
                        onSetSafeRestore = onSetSafeRestore
                    )

                    LiveDiagnosticsSection(uiState = uiState)

                    AboutSection()

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun FloatingControllerSection(
    floatingEnabled: Boolean,
    onToggleFloating: (Boolean) -> Unit,
    onResetFloatingPosition: () -> Unit
) {
    SettingsCard(title = "FLOATING CONTROLLER", icon = Icons.Default.Layers) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable Overlay",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tampilkan handle bulat orientasi di atas aplikasi lain",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = floatingEnabled,
                onCheckedChange = onToggleFloating,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanAccent,
                    checkedTrackColor = CyanAccentDim
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onResetFloatingPosition,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("button_reset_floating_position")
        ) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = CyanAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset Floating Position (Default Top-Left)", color = CyanAccent)
        }

        Spacer(modifier = Modifier.height(14.dp))
        FloatingControlPreview()
    }
}

@Composable
private fun NotificationSection(
    notificationEnabled: Boolean,
    onToggleNotification: (Boolean) -> Unit,
    onTriggerTestNotification: () -> Unit
) {
    SettingsCard(title = "NOTIFICATIONS", icon = Icons.Default.Notifications) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Persistent Notification",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tombol Portrait, Landscape, dan Auto di status bar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = notificationEnabled,
                onCheckedChange = onToggleNotification,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanAccent,
                    checkedTrackColor = CyanAccentDim
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onTriggerTestNotification,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kirim / Perbarui Notifikasi Kontrol", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SystemBehaviorSection(
    safeRestoreEnabled: Boolean,
    onSetSafeRestore: (Boolean) -> Unit
) {
    SettingsCard(title = "SYSTEM BEHAVIOR", icon = Icons.Default.Security) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Safe Restore (Compare-Before-Restore)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hanya memulihkan setelan jika tidak ada perubahan manual eksternal selama ROTAX berjalan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = safeRestoreEnabled,
                onCheckedChange = onSetSafeRestore,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyanAccent,
                    checkedTrackColor = CyanAccentDim
                )
            )
        }
    }
}

@Composable
private fun LiveDiagnosticsSection(uiState: RotaxUiState) {
    SettingsCard(title = "LIVE DIAGNOSTICS (READ-ONLY)", icon = Icons.Default.Analytics) {
        uiState.diagnostics?.let { (actualAccelerometerRotation, actualUserRotation, actualRotationDegrees, isRotaxOwner, canWriteSettings, canDrawOverlays, canPostNotifications, naturalOrientation, lastSnapshot) ->
            DiagnosticRow("Actual ACCELEROMETER_ROTATION", "$actualAccelerometerRotation (${if (actualAccelerometerRotation == 1) "Auto" else "Locked"})")
            DiagnosticRow("Actual USER_ROTATION", "$actualUserRotation ($actualRotationDegrees°)")
            DiagnosticRow("ROTAX Ownership Active", if (isRotaxOwner) "YES (Active)" else "NO")
            DiagnosticRow("WRITE_SETTINGS Permission", if (canWriteSettings) "GRANTED" else "DENIED")
            DiagnosticRow("SYSTEM_ALERT_WINDOW", if (canDrawOverlays) "GRANTED" else "NOT GRANTED")
            DiagnosticRow("POST_NOTIFICATIONS", if (canPostNotifications) "GRANTED" else "NOT GRANTED")
            DiagnosticRow("Display Natural Orientation", naturalOrientation)

            lastSnapshot?.let { snap ->
                val dateStr = remember(snap.timestamp) {
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(snap.timestamp))
                }
                DiagnosticRow("Last Stored Snapshot", "Accel: ${snap.accelerometerRotation}, Rot: ${snap.userRotation} ($dateStr)")
            }
        }
    }
}

@Composable
private fun AboutSection() {
    SettingsCard(title = "ABOUT & PRIVACY", icon = Icons.Default.Shield) {
        DiagnosticRow("Application", "ROTAX — Orientation Controller")
        DiagnosticRow("Version", "1.0.0")
        DiagnosticRow("Package", "id.web.idm.forcerotation")
        DiagnosticRow("Internet Permission", "NONE (100% Offline)")
        DiagnosticRow("Data Collection", "ZERO (Local DataStore only)")
        DiagnosticRow("Architecture", "Android Native Non-Root Public APIs")

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "ROTAX beroperasi sepenuhnya secara mandiri di perangkat Anda tanpa analitik, pelacakan, koneksi server, atau izin internet. Semua setelan disimpan lokal via DataStore Preferences.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkOutline)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}
