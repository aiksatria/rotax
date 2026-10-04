package id.web.idm.forcerotation.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import id.web.idm.forcerotation.R
import id.web.idm.forcerotation.data.RotaxDataStore
import id.web.idm.forcerotation.domain.OrientationController
import id.web.idm.forcerotation.domain.OrientationMode
import id.web.idm.forcerotation.notification.RotaxNotificationManager
import id.web.idm.forcerotation.platform.OverlayPermissionController
import id.web.idm.forcerotation.tile.RotaxTileService
import id.web.idm.forcerotation.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

class OrientationOverlayService : Service() {

    companion object {
        const val ACTION_START = "id.web.idm.forcerotation.service.START"
        const val ACTION_STOP = "id.web.idm.forcerotation.service.STOP"

        private val COLOR_DARK_SURFACE = Color.parseColor("#161B22")
        private val COLOR_CYAN_ACCENT = Color.parseColor("#00E5FF")
        private val COLOR_COOL_BLUE = Color.parseColor("#58A6FF")
        private val COLOR_STATUS_SUCCESS = Color.parseColor("#39D353")
        private val COLOR_TEXT_MUTED = Color.parseColor("#8B949E")
        private val COLOR_DARK_CONTAINER = Color.parseColor("#21262D")

        fun startService(context: Context) {
            val intent = Intent(context, OrientationOverlayService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, OrientationOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var dataStore: RotaxDataStore
    private lateinit var orientationController: OrientationController
    private var windowManager: WindowManager? = null
    private var overlayContainer: FrameLayout? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private var touchSlop = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        dataStore = RotaxDataStore(applicationContext)
        orientationController = OrientationController(applicationContext, dataStore)
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Must start in foreground
        serviceScope.launch {
            val mode = dataStore.selectedModeFlow.first()
            val hasOverride = dataStore.hasActiveOverrideFlow.first()
            val notification = RotaxNotificationManager.buildNotification(
                context = applicationContext,
                mode = mode,
                isOverrideActive = hasOverride,
                isFloatingActive = true
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        RotaxNotificationManager.NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(
                        RotaxNotificationManager.NOTIFICATION_ID,
                        notification,
                        0
                    )
                }
            } else {
                startForeground(RotaxNotificationManager.NOTIFICATION_ID, notification)
            }

            if (!OverlayPermissionController.canDrawOverlays(applicationContext)) {
                Logger.w("Overlay permission missing, stopping overlay service")
                dataStore.setFloatingEnabled(false)
                stopSelf()
                return@launch
            }

            if (overlayContainer == null) {
                val (posX, posY) = dataStore.floatingPositionFlow.first()
                initOverlayView(posX, posY)
            }
        }

        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverlayView(initialX: Int, initialY: Int) {
        if (overlayContainer != null) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }
        windowLayoutParams = params

        val container = FrameLayout(this)
        overlayContainer = container

        renderOverlayContent()

        try {
            windowManager?.addView(container, params)
        } catch (e: Exception) {
            Logger.e("Failed to add overlay view to window manager", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun renderOverlayContent() {
        val container = overlayContainer ?: return
        container.removeAllViews()

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        if (!isExpanded) {
            // Collapsed small circle (52dp)
            val handleSize = dp(52)
            val handle = FrameLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(handleSize, handleSize)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(COLOR_DARK_SURFACE)
                    setStroke(dp(2), COLOR_CYAN_ACCENT)
                }
                elevation = 16f
            }

            val icon = ImageView(this).apply {
                val iconSize = dp(28)
                layoutParams = FrameLayout.LayoutParams(iconSize, iconSize).apply {
                    gravity = Gravity.CENTER
                }
                setImageResource(R.drawable.ic_rotax_tile)
                setColorFilter(COLOR_CYAN_ACCENT)
            }
            handle.addView(icon)

            var initialTouchX = 0f
            var initialTouchY = 0f
            var initialWindowX = 0
            var initialWindowY = 0
            var isDragging = false

            handle.setOnTouchListener { _, event ->
                val params = windowLayoutParams ?: return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        initialWindowX = params.x
                        initialWindowY = params.y
                        isDragging = false
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            isDragging = true
                        }
                        if (isDragging) {
                            val metrics = resources.displayMetrics
                            val maxX = maxOf(0, metrics.widthPixels - handleSize)
                            val maxY = maxOf(0, metrics.heightPixels - handleSize)

                            params.x = (initialWindowX + dx).coerceIn(0, maxX)
                            params.y = (initialWindowY + dy).coerceIn(0, maxY)

                            try {
                                windowManager?.updateViewLayout(container, params)
                            } catch (e: Exception) {
                                Logger.d("updateViewLayout failed: ${e.message}")
                            }
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isDragging) {
                            serviceScope.launch {
                                dataStore.saveFloatingPosition(params.x, params.y)
                            }
                        } else {
                            // Tap: expand controller
                            isExpanded = true
                            renderOverlayContent()
                        }
                        true
                    }
                    else -> false
                }
            }

            container.addView(handle)
        } else {
            // Expanded horizontal panel: [ P ] [ L ] [ A ] [ ✕ ]
            val panel = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(4), dp(8), dp(4))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(24).toFloat()
                    setColor(COLOR_DARK_SURFACE)
                    setStroke(dp(2), COLOR_CYAN_ACCENT)
                }
                elevation = 20f
            }

            fun createActionButton(
                label: String,
                tintColor: Int,
                onClick: () -> Unit
            ): TextView {
                val btnSize = dp(48)
                return TextView(this).apply {
                    text = label
                    textSize = 17f
                    setTextColor(tintColor)
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                        setMargins(dp(2), dp(2), dp(2), dp(2))
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(COLOR_DARK_CONTAINER)
                    }
                    setOnClickListener {
                        onClick()
                    }
                }
            }

            // Portrait button
            panel.addView(createActionButton("P", COLOR_CYAN_ACCENT) {
                serviceScope.launch {
                    orientationController.applyMode(OrientationMode.PORTRAIT)
                    updateNotificationAndTile(OrientationMode.PORTRAIT)
                    isExpanded = false
                    renderOverlayContent()
                }
            })

            // Landscape button
            panel.addView(createActionButton("L", COLOR_COOL_BLUE) {
                serviceScope.launch {
                    orientationController.applyMode(OrientationMode.LANDSCAPE)
                    updateNotificationAndTile(OrientationMode.LANDSCAPE)
                    isExpanded = false
                    renderOverlayContent()
                }
            })

            // Auto button
            panel.addView(createActionButton("A", COLOR_STATUS_SUCCESS) {
                serviceScope.launch {
                    orientationController.applyMode(OrientationMode.AUTO)
                    updateNotificationAndTile(OrientationMode.AUTO)
                    isExpanded = false
                    renderOverlayContent()
                }
            })

            // Close / collapse button
            panel.addView(createActionButton("✕", COLOR_TEXT_MUTED) {
                isExpanded = false
                renderOverlayContent()
            })

            container.addView(panel)
        }
    }

    private suspend fun updateNotificationAndTile(mode: OrientationMode) {
        val hasOverride = mode != OrientationMode.AUTO
        val isNotif = dataStore.notificationEnabledFlow.first()
        val isFloat = dataStore.floatingEnabledFlow.first()

        if (isNotif || isFloat) {
            RotaxNotificationManager.updateNotification(
                applicationContext,
                mode,
                hasOverride,
                isFloat
            )
        }
        RotaxTileService.requestListeningState(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        overlayContainer?.let { container ->
            try {
                windowManager?.removeView(container)
            } catch (e: Exception) {
                Logger.e("Failed to remove overlay view on destroy", e)
            }
        }
        overlayContainer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
