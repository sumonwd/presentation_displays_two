package com.phonetechbd.presentation_displays_two

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import androidx.annotation.NonNull
import com.google.gson.Gson
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class PresentationDisplaysTwoPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel : MethodChannel
    private lateinit var mainToPresentationChannel: MethodChannel
    private lateinit var presentationToMainChannel: MethodChannel
    private var context: Context? = null
    private var presentation: PresentationDisplayTwo? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "presentation_displays_two_plugin")
        channel.setMethodCallHandler(this)

        mainToPresentationChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "main_to_presentation_channel")
        presentationToMainChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "presentation_to_main_channel")

        mainToPresentationChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "receivedDataFromMain" -> {
                    val data = call.argument<Any>("data")
                    presentation?.sendDataToPresentation(data)
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }

        presentationToMainChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "receivedDataFromPresentation" -> {
                    val data = call.argument<Any>("data")
                    channel.invokeMethod("receivedDataFromPresentation", mapOf("data" to data))
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "listDisplay" -> {
                try {
                    val listJson = ArrayList<DisplayTwoJson>()
                    val category = call.argument<String?>("category")
                    val displays = if (category != null) {
                        displayManager?.getDisplays(category)
                    } else {
                        displayManager?.displays
                    }
                    displays?.forEach { display ->
                        val d = DisplayTwoJson(display.displayId, display.flags, display.rotation, display.name)
                        listJson.add(d)
                    }
                    result.success(Gson().toJson(listJson))
                } catch (e: Exception) {
                    Log.e(TAG, "Error in listDisplay: ${e.message}", e)
                    result.error("LIST_DISPLAY_ERROR", "Failed to list displays: ${e.message}", null)
                }
            }
            "showPresentation" -> {
                try {
                    val displayId = call.argument<Int>("displayId") ?: return
                    val routerName = call.argument<String>("routerName") ?: return
                    val display = displayManager?.getDisplay(displayId)
                    if (display != null) {
                        val flutterEngine = createFlutterEngine(routerName)
                        flutterEngine?.let {
                            presentation = context?.let { ctx ->
                                PresentationDisplayTwo(ctx, routerName, display, mainToPresentationChannel, presentationToMainChannel)
                            }
                            presentation?.show()
                            result.success(true)
                        } ?: result.error("404", "Can't find FlutterEngine", null)
                    } else {
                        result.error("404", "Can't find display with displayId $displayId", null)
                    }
                } catch (e: Exception) {
                    result.error(call.method, e.message, null)
                }
            }
            "hidePresentation" -> {
                try {
                    presentation?.dismiss()
                    presentation = null
                    result.success(true)
                } catch (e: Exception) {
                    result.error(call.method, e.message, null)
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        mainToPresentationChannel.setMethodCallHandler(null)
        presentationToMainChannel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.context = binding.activity
        displayManager = context?.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    override fun onDetachedFromActivityForConfigChanges() {}

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

    override fun onDetachedFromActivity() {}

    private fun createFlutterEngine(routerName: String): FlutterEngine? {
        if (context == null) return null
        if (FlutterEngineCache.getInstance().get(routerName) == null) {
            val flutterEngine = FlutterEngine(context!!)
            flutterEngine.navigationChannel.setInitialRoute(routerName)
            FlutterInjector.instance().flutterLoader().startInitialization(context!!)
            val path = FlutterInjector.instance().flutterLoader().findAppBundlePath()
            val entrypoint = DartExecutor.DartEntrypoint(path, "secondaryDisplayMain")
            flutterEngine.dartExecutor.executeDartEntrypoint(entrypoint)
            flutterEngine.lifecycleChannel.appIsResumed()
            FlutterEngineCache.getInstance().put(routerName, flutterEngine)
        }
        return FlutterEngineCache.getInstance().get(routerName)
    }

    companion object {
        private const val TAG = "PresentationDisplaysTwoPlugin"
        private var displayManager: DisplayManager? = null
    }
}