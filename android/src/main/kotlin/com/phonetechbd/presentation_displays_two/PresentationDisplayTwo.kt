package com.phonetechbd.presentation_displays_two

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.FrameLayout
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel

class PresentationDisplayTwo(
    context: Context,
    private val routerName: String,
    display: Display,
    private val mainToPresentationChannel: MethodChannel,
    private val presentationToMainChannel: MethodChannel
) : Presentation(context, display) {

    private lateinit var flutterView: FlutterView
    private lateinit var secondaryChannel: MethodChannel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        flutterView = FlutterView(context)
        setContentView(flutterView)

        val flutterEngine = FlutterEngineCache.getInstance().get(routerName)
        if (flutterEngine != null) {
            flutterView.attachToFlutterEngine(flutterEngine)
            setupMethodChannels(flutterEngine)
        } else {
            Log.e(TAG, "Can't find the FlutterEngine with cache name $routerName")
        }
    }

    private fun setupMethodChannels(flutterEngine: FlutterEngine) {
        secondaryChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "main_to_presentation_channel")
        secondaryChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "updateSecondaryDisplay" -> {
                    val data = call.argument<Any>("data")
                    Log.d(TAG, "Updating secondary display with data: $data")
                    result.success(true)
                }
                "sendDataToMain" -> {
                    val data = call.argument<Any>("data")
                    Log.d(TAG, "Sending data to main: $data")
                    presentationToMainChannel.invokeMethod("receivedDataFromPresentation", mapOf("data" to data))
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }

    fun sendDataToPresentation(data: Any?) {
        secondaryChannel.invokeMethod("updateSecondaryDisplay", mapOf("data" to data))
    }

    companion object {
        private const val TAG = "PresentationDisplayTwo"
    }
}