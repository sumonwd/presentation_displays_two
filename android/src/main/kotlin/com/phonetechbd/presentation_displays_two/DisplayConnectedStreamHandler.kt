package com.phonetechbd.presentation_displays_two

import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.EventChannel

class DisplayConnectedStreamHandler(private var displayManager: DisplayManager?) : EventChannel.StreamHandler {
    private var sink: EventChannel.EventSink? = null
    private var handler: Handler? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            sink?.success(1)
        }

        override fun onDisplayRemoved(displayId: Int) {
            sink?.success(0)
        }

        override fun onDisplayChanged(displayId: Int) {}
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        sink = events
        handler = Handler(Looper.getMainLooper())
        displayManager?.registerDisplayListener(displayListener, handler)
    }

    override fun onCancel(arguments: Any?) {
        sink = null
        handler = null
        displayManager?.unregisterDisplayListener(displayListener)
    }
}