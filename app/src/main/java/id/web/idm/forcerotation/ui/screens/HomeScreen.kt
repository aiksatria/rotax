package id.web.idm.forcerotation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.domain.RotaxUiState
import id.web.idm.forcerotation.ui.components.OrientationButtons
import id.web.idm.forcerotation.ui.components.OrientationStatusCard
import id.web.idm.forcerotation.ui.components.PermissionStatusCard
import id.web.idm.forcerotation.ui.components.QuickControlSection
import id.web.idm.forcerotation.ui.theme.CyanAccent
import id.web.idm.forcerotation.ui.theme.DarkBackground
import id.web.idm.forcerotation.ui.theme.DarkOutline
import id.web.idm.forcerotation.ui.theme.DarkSurface
import id.web.idm.forcerotation.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: RotaxUiState,
    onModeSelected: (OrientationMode) -> Unit,
    onToggleFloating: (Boolean) -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onEmergencyRestore: () -> Unit,
    onOpenWriteSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenSettingsScreen: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ROTAX",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = CyanAccent,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyanAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NON-ROOT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Orientation Controller",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettingsScreen,
                        modifier = Modifier.testTag("button_open_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
                // Adaptive 2-Column Layout for Tablets, Unfolded Foldables & Landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Pane: Status, Mode Select & Quick Control
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Error Banner if present
                        uiState.errorMessage?.let { errorText ->
                            ErrorBanner(errorText = errorText, onDismissError = onDismissError)
                        }

                        OrientationStatusCard(
                            uiState = uiState,
                            onGrantPermissionClick = onOpenWriteSettings
                        )

                        OrientationButtons(
                            selectedMode = uiState.mode,
                            onModeSelected = onModeSelected
                        )

                        QuickControlSection(
                            floatingEnabled = uiState.floatingEnabled,
                            onToggleFloating = onToggleFloating,
                            notificationEnabled = uiState.notificationEnabled,
                            onToggleNotification = onToggleNotification,
                            onEmergencyRestore = onEmergencyRestore
                        )
                    }

                    // Right Pane: Permissions & Info Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PermissionStatusCard(
                            canWriteSettings = uiState.canWriteSettings,
                            canDrawOverlays = uiState.canDrawOverlays,
                            canPostNotifications = uiState.canPostNotifications,
                            onOpenWriteSettings = onOpenWriteSettings,
                            onOpenOverlaySettings = onOpenOverlaySettings,
                            onOpenNotificationSettings = onOpenNotificationSettings
                        )

                        SystemInfoCard()
                    }
                }
            } else {
                // Standard Single-Column Layout for Phones
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uiState.errorMessage?.let { errorText ->
                        ErrorBanner(errorText = errorText, onDismissError = onDismissError)
                    }

                    OrientationStatusCard(
                        uiState = uiState,
                        onGrantPermissionClick = onOpenWriteSettings
                    )

                    OrientationButtons(
                        selectedMode = uiState.mode,
                        onModeSelected = onModeSelected
                    )

                    QuickControlSection(
                        floatingEnabled = uiState.floatingEnabled,
                        onToggleFloating = onToggleFloating,
                        notificationEnabled = uiState.notificationEnabled,
                        onToggleNotification = onToggleNotification,
                        onEmergencyRestore = onEmergencyRestore
                    )

                    PermissionStatusCard(
                        canWriteSettings = uiState.canWriteSettings,
                        canDrawOverlays = uiState.canDrawOverlays,
                        canPostNotifications = uiState.canPostNotifications,
                        onOpenWriteSettings = onOpenWriteSettings,
                        onOpenOverlaySettings = onOpenOverlaySettings,
                        onOpenNotificationSettings = onOpenNotificationSettings
                    )

                    SystemInfoCard()

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(errorText: String, onDismissError: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(StatusError.copy(alpha = 0.15f))
            .border(1.dp, StatusError.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = StatusError,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismissError,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SystemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkOutline)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INFORMASI SISTEM ANDROID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ROTAX mengontrol system-level user rotation pada perangkat Android menggunakan system settings yang diizinkan pengguna. Bila aplikasi target meminta orientasi khusus atau diprioritaskan oleh sistem OEM, Android dapat memberikan precedence kepada Activity tersebut.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
