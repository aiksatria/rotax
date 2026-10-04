package id.web.idm.forcerotation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.web.idm.forcerotation.ui.theme.CyanAccent
import id.web.idm.forcerotation.ui.theme.DarkOutline
import id.web.idm.forcerotation.ui.theme.DarkSurface
import id.web.idm.forcerotation.ui.theme.DarkSurfaceElevated
import id.web.idm.forcerotation.ui.theme.StatusError
import id.web.idm.forcerotation.ui.theme.StatusSuccess

@Composable
fun PermissionStatusCard(
    canWriteSettings: Boolean,
    canDrawOverlays: Boolean,
    canPostNotifications: Boolean,
    onOpenWriteSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("permission_panel"),
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
            Text(
                text = "SYSTEM PERMISSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItem(
                title = "SYSTEM SETTINGS",
                subtitle = "Ubah setelan sistem untuk USER_ROTATION",
                icon = Icons.Default.Settings,
                isGranted = canWriteSettings,
                isRequired = true,
                testTag = "permission_item_write_settings",
                onClick = onOpenWriteSettings
            )

            Spacer(modifier = Modifier.height(8.dp))

            PermissionItem(
                title = "FLOATING OVERLAY",
                subtitle = "Tampilkan kontrol di atas aplikasi lain",
                icon = Icons.Default.Layers,
                isGranted = canDrawOverlays,
                isRequired = false,
                testTag = "permission_item_overlay",
                onClick = onOpenOverlaySettings
            )

            Spacer(modifier = Modifier.height(8.dp))

            PermissionItem(
                title = "NOTIFICATIONS",
                subtitle = "Kontrol cepat dari notification shade",
                icon = Icons.Default.Notifications,
                isGranted = canPostNotifications,
                isRequired = false,
                testTag = "permission_item_notifications",
                onClick = onOpenNotificationSettings
            )
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isGranted) StatusSuccess.copy(alpha = 0.15f)
                    else if (isRequired) StatusError.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isGranted) StatusSuccess
                else if (isRequired) StatusError
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (isRequired) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• WAJIB",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isGranted) StatusSuccess else StatusError,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isGranted) StatusSuccess.copy(alpha = 0.15f)
                        else StatusError.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isGranted) "● Granted" else "○ Required",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (isGranted) StatusSuccess else StatusError,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Open Settings",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
