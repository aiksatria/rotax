package id.web.idm.forcerotation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.domain.OverrideStatus
import id.web.idm.forcerotation.domain.RotaxUiState
import id.web.idm.forcerotation.ui.theme.CyanAccent
import id.web.idm.forcerotation.ui.theme.DarkOutline
import id.web.idm.forcerotation.ui.theme.DarkSurface
import id.web.idm.forcerotation.ui.theme.DarkSurfaceElevated
import id.web.idm.forcerotation.ui.theme.StatusError
import id.web.idm.forcerotation.ui.theme.StatusSuccess
import id.web.idm.forcerotation.ui.theme.StatusWarning

@Composable
fun OrientationStatusCard(
    uiState: RotaxUiState,
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = uiState.currentRotationDegrees.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "rotation_angle"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (!uiState.canWriteSettings) StatusError else DarkOutline
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )

                // Natural orientation badge
                uiState.diagnostics?.naturalOrientation?.let { natural ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NATURAL: $natural",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.mode.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = when (uiState.mode) {
                            OrientationMode.AUTO -> StatusSuccess
                            OrientationMode.PORTRAIT -> CyanAccent
                            OrientationMode.LANDSCAPE -> MaterialTheme.colorScheme.secondary
                        },
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${uiState.currentRotationDegrees}° (USER_ROTATION: ${uiState.currentRotation})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Rotating device indicator icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, DarkOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.mode == OrientationMode.AUTO) {
                            Icons.Default.ScreenRotation
                        } else {
                            Icons.AutoMirrored.Filled.RotateRight
                        },
                        contentDescription = "Rotation Indicator",
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(rotationAngle),
                        tint = if (uiState.mode == OrientationMode.AUTO) StatusSuccess else CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Override Status bar
            if (!uiState.canWriteSettings) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusError.copy(alpha = 0.15f))
                        .border(1.dp, StatusError.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM CONTROL UNAVAILABLE",
                                style = MaterialTheme.typography.labelLarge,
                                color = StatusError
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Izin ubah setelan sistem (WRITE_SETTINGS) diperlukan untuk mengontrol orientasi layar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onGrantPermissionClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusError,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_write_settings_button")
                        ) {
                            Text("GRANT ACCESS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (uiState.overrideStatus) {
                                    OverrideStatus.ACTIVE -> CyanAccent
                                    OverrideStatus.INACTIVE -> StatusSuccess
                                    OverrideStatus.EXTERNALLY_MODIFIED -> StatusWarning
                                    OverrideStatus.SYSTEM_REJECTED -> StatusError
                                    OverrideStatus.PERMISSION_REQUIRED -> StatusError
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (uiState.overrideStatus) {
                            OverrideStatus.ACTIVE -> "System rotation override: ACTIVE"
                            OverrideStatus.INACTIVE -> "System rotation: DEFAULT (Auto-Rotate)"
                            OverrideStatus.EXTERNALLY_MODIFIED -> "System diubah di luar ROTAX"
                            OverrideStatus.SYSTEM_REJECTED -> "Android menolak perubahan orientasi"
                            OverrideStatus.PERMISSION_REQUIRED -> "Izin diperlukan"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
