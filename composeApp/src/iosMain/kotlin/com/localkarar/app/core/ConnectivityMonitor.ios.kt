package com.localkarar.app.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_global_queue
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT

/**
 * iOS baglanti gozlemcisi — NWPathMonitor.
 *
 * `nw_path_status_satisfied` "bu yol uzerinden baglanti kurulabilir" demek;
 * Android tarafindaki NET_CAPABILITY_VALIDATED ile ayni role sahip.
 */
@OptIn(ExperimentalForeignApi::class)
actual class ConnectivityMonitor actual constructor() {

    private val _cevrimici = MutableStateFlow(true)
    actual val cevrimici: StateFlow<Boolean> = _cevrimici.asStateFlow()

    private var gozlemci: nw_path_monitor_t? = null

    actual fun basla() {
        if (gozlemci != null) return
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_queue(
            monitor,
            dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0uL)
        )
        nw_path_monitor_set_update_handler(monitor) { path ->
            _cevrimici.value = nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_start(monitor)
        gozlemci = monitor
    }

    actual fun dur() {
        gozlemci?.let { nw_path_monitor_cancel(it) }
        gozlemci = null
    }
}
