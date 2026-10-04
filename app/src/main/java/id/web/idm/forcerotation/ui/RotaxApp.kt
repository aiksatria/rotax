package id.web.idm.forcerotation.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.web.idm.forcerotation.domain.OverrideStatus
import id.web.idm.forcerotation.notification.RotaxNotificationManager
import id.web.idm.forcerotation.ui.components.FirstRunDialog
import id.web.idm.forcerotation.ui.screens.HomeScreen
import id.web.idm.forcerotation.ui.screens.SettingsScreen
import id.web.idm.forcerotation.ui.theme.RotaxTheme
import id.web.idm.forcerotation.util.IntentUtils

enum class Screen {
    HOME,
    SETTINGS
}

@Composable
fun RotaxApp(
    viewModel: RotaxViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    // Lifecycle effect: refresh state on ON_RESUME so permissions update immediately upon return
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    RotaxTheme(darkTheme = isSystemInDarkTheme()) {
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    uiState = uiState,
                    onModeSelected = { mode -> viewModel.applyMode(mode) },
                    onToggleFloating = { enabled -> viewModel.toggleFloating(enabled) },
                    onToggleNotification = { enabled -> viewModel.toggleNotification(enabled) },
                    onEmergencyRestore = { viewModel.emergencyRestore() },
                    onOpenWriteSettings = { IntentUtils.openWriteSettings(context) },
                    onOpenOverlaySettings = { IntentUtils.openOverlaySettings(context) },
                    onOpenNotificationSettings = { IntentUtils.openNotificationSettings(context) },
                    onOpenSettingsScreen = { currentScreen = Screen.SETTINGS },
                    onDismissError = { viewModel.clearErrorMessage() },
                    modifier = modifier
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    uiState = uiState,
                    onBackClick = { currentScreen = Screen.HOME },
                    onToggleFloating = { enabled -> viewModel.toggleFloating(enabled) },
                    onResetFloatingPosition = { viewModel.resetFloatingPosition() },
                    onToggleNotification = { enabled -> viewModel.toggleNotification(enabled) },
                    onSetSafeRestore = { enabled -> viewModel.setSafeRestore(enabled) },
                    onTriggerTestNotification = {
                        RotaxNotificationManager.updateNotification(
                            context = context,
                            mode = uiState.mode,
                            isOverrideActive = uiState.overrideStatus == OverrideStatus.ACTIVE,
                            isFloatingActive = uiState.floatingEnabled
                        )
                    },
                    modifier = modifier
                )
            }
        }

        if (uiState.isFirstRun) {
            FirstRunDialog(
                canWriteSettings = uiState.canWriteSettings,
                canDrawOverlays = uiState.canDrawOverlays,
                canPostNotifications = uiState.canPostNotifications,
                onOpenWriteSettings = { IntentUtils.openWriteSettings(context) },
                onOpenOverlaySettings = { IntentUtils.openOverlaySettings(context) },
                onOpenNotificationSettings = { IntentUtils.openNotificationSettings(context) },
                onCompleteFirstRun = { viewModel.completeFirstRun() }
            )
        }
    }
}
