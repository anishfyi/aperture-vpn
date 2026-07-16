package io.github.anishfyi.aperture

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.VpnStatusBootstrap

class ApertureApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashLog.install(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createOpenVpnNotificationChannels()
        }
        runCatching { VpnStatusBootstrap.start(applicationContext) }
    }

    /*
     * OpenVPNService posts its startForeground notification on these exact
     * channel ids. Upstream creates them in ICSOpenVPNApplication, which our
     * manifest replaces, so they must be created here; a missing channel
     * kills the app with RemoteServiceException when the service starts.
     */
    private fun createOpenVpnNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
                "Connection statistics",
                NotificationManager.IMPORTANCE_MIN,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                "Connection status",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
                "Connection requests",
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }
}
