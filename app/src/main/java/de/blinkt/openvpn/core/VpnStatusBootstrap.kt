package de.blinkt.openvpn.core

import android.content.Context

/*
 * StatusListener.init is package-private in ics-openvpn, so the bridge that
 * binds our UI process to the VPN service status socket must live in the
 * same package.
 */
object VpnStatusBootstrap {
    fun start(context: Context) {
        StatusListener().init(context)
    }
}
