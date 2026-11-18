package com.openfield.app.feature.ai.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.openfield.app.OpenFieldApplication
import com.openfield.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the model download alive while the app is in the background
 * and holds a partial wake lock so the CPU does not sleep mid-stream. Without this, Android
 * kills the long-running download process (and shows it to the user as an app crash).
 */
class ModelDownloadService : Service() {

    companion object {
        private const val CHANNEL_ID = "openfield_model_download"
        private const val NOTIFICATION_ID = 7421
        const val ACTION_START = "com.openfield.app.action.START_DOWNLOAD"
        const val ACTION_CANCEL = "com.openfield.app.action.CANCEL_DOWNLOAD"

        fun start(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancel(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var progressJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                val app = applicationContext as? OpenFieldApplication
                app?.modelRepository?.cancelDownload()
                stopSelfSafely()
                return START_NOT_STICKY
            }
            else -> {
                startForegroundWithNotification(
                    title = "Preparing on-device AI model",
                    content = "Starting download…",
                    progressPercent = 0,
                    indeterminate = true
                )
                acquireWakeLock()
                observeProgress()
            }
        }
        return START_NOT_STICKY
    }

    private fun observeProgress() {
        progressJob?.cancel()
        val app = applicationContext as? OpenFieldApplication ?: run {
            stopSelfSafely()
            return
        }
        progressJob = serviceScope.launch {
            // Wait for an actual download to begin, then mirror progress to the notification.
            app.modelRepository.installationState.collectLatest { state ->
                when (state) {
                    is ModelInstallationState.Downloading -> {
                        val mb = state.bytesDownloaded / (1024 * 1024)
                        val totalMb = (state.totalBytes / (1024 * 1024)).coerceAtLeast(1)
                        val speedKb = state.speedBytesPerSec / 1024
                        updateNotification(
                            title = "Downloading ${state.model.displayName}",
                            content = "$mb / $totalMb MB · ${speedKb} KB/s",
                            progressPercent = state.progressPercent,
                            indeterminate = false
                        )
                    }
                    is ModelInstallationState.Verifying -> {
                        updateNotification(
                            title = "Verifying ${state.model.displayName}",
                            content = "Checking model integrity…",
                            progressPercent = 100,
                            indeterminate = true
                        )
                    }
                    is ModelInstallationState.Installed -> {
                        updateNotification(
                            title = "Model ready",
                            content = "${state.activeModel.displayName} installed",
                            progressPercent = 100,
                            indeterminate = false
                        )
                        stopSelfSafely()
                    }
                    is ModelInstallationState.Error -> {
                        updateNotification(
                            title = "Download failed",
                            content = state.message.take(120),
                            progressPercent = 0,
                            indeterminate = false
                        )
                        stopSelfSafely()
                    }
                    else -> { /* NotInstalled / Checking */ }
                }
            }
        }
    }

    private fun stopSelfSafely() {
        releaseWakeLock()
        progressJob?.cancel()
        progressJob = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(false)
        }
        stopSelf()
    }

    override fun onDestroy() {
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OpenField:ModelDownload").apply {
            setReferenceCounted(false)
            acquire(30 * 60 * 1000L /* 30 min safety timeout */)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (ignored: Throwable) {}
        wakeLock = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val existing = nm?.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Model Download",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Progress for the on-device AI model download"
                    setShowBadge(false)
                }
                nm?.createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(
        title: String,
        content: String,
        progressPercent: Int,
        indeterminate: Boolean
    ): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progressPercent.coerceIn(0, 100), indeterminate)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    private fun startForegroundWithNotification(
        title: String,
        content: String,
        progressPercent: Int,
        indeterminate: Boolean
    ) {
        val notification = buildNotification(title, content, progressPercent, indeterminate)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(
        title: String,
        content: String,
        progressPercent: Int,
        indeterminate: Boolean
    ) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        nm.notify(NOTIFICATION_ID, buildNotification(title, content, progressPercent, indeterminate))
    }
}
