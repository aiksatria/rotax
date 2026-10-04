package id.web.idm.forcerotation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.ui.theme.CyanAccent
import id.web.idm.forcerotation.ui.theme.CyanAccentDim
import id.web.idm.forcerotation.ui.theme.DarkOutline
import id.web.idm.forcerotation.ui.theme.DarkSurface
import id.web.idm.forcerotation.ui.theme.DarkSurfaceElevated
import id.web.idm.forcerotation.ui.theme.StatusSuccess

@Composable
fun OrientationButtons(
    selectedMode: OrientationMode,
    onModeSelected: (OrientationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OrientationButtonItem(
            label = "AUTO",
            sublabel = "Sensors",
            icon = Icons.Default.AutoMode,
            isSelected = selectedMode == OrientationMode.AUTO,
            activeColor = StatusSuccess,
            testTag = "button_mode_auto",
            onClick = { onModeSelected(OrientationMode.AUTO) },
            modifier = Modifier.weight(1f)
        )

        OrientationButtonItem(
            label = "PORTRAIT",
            sublabel = "Vertical",
            icon = Icons.Default.StayCurrentPortrait,
            isSelected = selectedMode == OrientationMode.PORTRAIT,
            activeColor = CyanAccent,
            testTag = "button_mode_portrait",
            onClick = { onModeSelected(OrientationMode.PORTRAIT) },
            modifier = Modifier.weight(1f)
        )

        OrientationButtonItem(
            label = "LANDSCAPE",
            sublabel = "Horizontal",
            icon = Icons.Default.StayCurrentLandscape,
            isSelected = selectedMode == OrientationMode.LANDSCAPE,
            activeColor = MaterialTheme.colorScheme.secondary,
            testTag = "button_mode_landscape",
            onClick = { onModeSelected(OrientationMode.LANDSCAPE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OrientationButtonItem(
    label: String,
    sublabel: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 100.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.12f) else DarkSurfaceElevated
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeColor else DarkOutline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) activeColor.copy(alpha = 0.2f) else DarkSurface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 12.sp
            )

            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) activeColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
