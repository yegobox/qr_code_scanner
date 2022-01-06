package net.touchcapture.qr.flutterqr

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.NonNull
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView

class QRView(
    @NonNull private val context: Context,
    @NonNull private val flutterPluginBinding: FlutterPlugin.FlutterPluginBinding,
    @NonNull private val activityPluginBinding: ActivityPluginBinding,
    @NonNull private val id: Int,
    @NonNull private val params: HashMap<String, Any>
) :
    PlatformView,
    MethodChannel.MethodCallHandler, PluginRegistry.RequestPermissionsResultListener,
    Application.ActivityLifecycleCallbacks{

    private val channel: MethodChannel = MethodChannel(
        flutterPluginBinding.binaryMessenger,
        "net.touchcapture.qr.flutterqr/qrview_$id"
    )

    // Custom barcode view with resultpoints
    private var barcodeViewCustom: CustomDecoratedBarcodeView? = null

    // Variables for app state
    private var isTorchOn: Boolean = false
    private var isPaused: Boolean = false

    private val cameraRequestId = 513469796
    private val cameraFacingBack = 0
    private val cameraFacingFront = 1

    init {
        channel.setMethodCallHandler(this)
        activityPluginBinding.activity.application?.registerActivityLifecycleCallbacks(this)
        activityPluginBinding.addRequestPermissionsResultListener(this)
    }

    override fun getView(): View {
        if (barcodeViewCustom == null) {
            barcodeViewCustom =
                CustomDecoratedBarcodeView(activityPluginBinding.activity, params["enableDots"] as Boolean, params["enableLaser"] as Boolean)
        }

        if (!isPaused) barcodeViewCustom!!.resume()

        if (params["cameraFacing"] as Int == cameraFacingFront) {
            barcodeViewCustom?.cameraSettings?.requestedCameraId = cameraFacingFront
        }

        barcodeViewCustom!!.getBarcodeView()?.framingRectSize =
            Size(convertDpToPixels(params["width"] as Double), convertDpToPixels( params["height"] as Double))

        // TODO: Make scanArea change according to size of cutout
        // This can be done after a new version of https://github.com/journeyapps/zxing-android-embedded is released

//        changeScanArea(params["width"] as Double, params["height"] as Double, 0.0, null)
//        if (params["enableLaser"] as Int == 1) {
//            barcodeViewCustom!!.viewFinder!!.setLaserVisibility(true)
//        }
//
//         TODO: Make resultpoints invisible if wanted. Will be implemented in next release of zxing-android-embedded
//        if (params["enableDots"] as Int == 1) {
//        }
//        }
//        } else {
//            if (!isPaused) barcodeViewCustom!!.resume()
//        }
        return barcodeViewCustom!!.apply {}
    }

    override fun dispose() {
        barcodeViewCustom?.pause()
        barcodeViewCustom = null
    }

    // MethodCallHandler
    @Suppress("UNCHECKED_CAST")
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "startScan" -> startScan(call.arguments as? List<Int>, result)
            "stopScan" -> stopScan()
            "flipCamera" -> flipCamera(result)
            "toggleFlash" -> toggleFlash(result)
            "pauseCamera" -> pauseCamera(result)
            // Stopping camera is the same as pausing camera
            "stopCamera" -> pauseCamera(result)
            "resumeCamera" -> resumeCamera(result)
            "requestPermissions" -> checkAndRequestPermission(result)
            "getCameraInfo" -> getCameraInfo(result)
            "getFlashInfo" -> getFlashInfo(result)
            "getSystemFeatures" -> getSystemFeatures(result)
            "changeScanArea" -> changeScanArea(
                call.argument<Double>("scanAreaWidth")!!,
                call.argument<Double>("scanAreaHeight")!!,
                call.argument<Double>("cutOutBottomOffset")!!,
                result,
            )
            else -> result.notImplemented()
        }
    }

    // ActivityLifecycleCallbacks
    override fun onActivityPaused(p0: Activity) {
        barcodeViewCustom?.pause()
//        if (p0 == activityPluginBinding.activity) barcodeViewCustom?.pause()
//        if (p0 == activityPluginBinding.activity && !isPaused && hasCameraPermission()) {
//            barcodeViewCustom?.pause()
//        }
    }

    override fun onActivityResumed(p0: Activity) {
        barcodeViewCustom?.resume()
//        if (p0 == activityPluginBinding.activity && !isPaused && hasCameraPermission()) {
//            barcodeViewCustom?.resume()
//        }
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    // Functions for changing barcodeView
    private fun changeScanArea(
        dpScanAreaWidth: Double,
        dpScanAreaHeight: Double,
        cutOutBottomOffset: Double,
        result: MethodChannel.Result?
    ) {
//        if (barcodeViewCustom == null) return barCodeViewNotSet(result, "changeScanArea")
//
//        barcodeViewCustom!!.getBarcodeView()?.framingRectSize =
//            Size(convertDpToPixels(dpScanAreaWidth), convertDpToPixels(dpScanAreaHeight))

        // TODO: Implement custom framingRectSize to support bottomOffset
//        barcodeViewCustom!!.getBarcodeView()?.framingRectSize = (
//                convertDpToPixels(dpScanAreaWidth),
//        convertDpToPixels(dpScanAreaHeight),
//        convertDpToPixels(dpCutOutBottomOffset),
//        )
        result?.success(true)
    }

    private fun getCameraInfo(result: MethodChannel.Result) {
        if (barcodeViewCustom == null) return barCodeViewNotSet(result, "getCameraInfo")

        result.success(barcodeViewCustom!!.cameraSettings?.requestedCameraId)
    }

    private fun flipCamera(result: MethodChannel.Result) {
        if (barcodeViewCustom == null) return barCodeViewNotSet(result, "getCameraInfo")
        if (barcodeViewCustom!!.cameraSettings == null) return result.error("404", "Camera settings not found", "flipCamera")

        barcodeViewCustom!!.pause()
        val settings = barcodeViewCustom!!.cameraSettings!!

        if (settings.requestedCameraId == cameraFacingFront) {
            settings.requestedCameraId = cameraFacingBack
        } else {
            settings.requestedCameraId = cameraFacingFront
        }

        barcodeViewCustom!!.cameraSettings = settings
        barcodeViewCustom!!.resume()
        result.success(settings.requestedCameraId)
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean {
        return hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun getFlashInfo(result: MethodChannel.Result) {
        result.success(isTorchOn)
    }

    private fun toggleFlash(result: MethodChannel.Result) {
        if (barcodeViewCustom == null) return barCodeViewNotSet(result, "toggleFlash")

        if (hasFlash()) {
            barcodeViewCustom!!.getBarcodeView()?.setTorch(!isTorchOn)
            isTorchOn = !isTorchOn
            result.success(isTorchOn)
        } else {
            result.error("404", "This device doesn't support flash", "toggleFlash")
        }

    }

    /**
     * Controls for the camera
     */
    private fun pauseCamera(result: MethodChannel.Result) {
        if (barcodeViewCustom == null) return barCodeViewNotSet(result, "pauseCamera")

        if (barcodeViewCustom!!.getBarcodeView()!!.isPreviewActive) {
            isPaused = true
            barcodeViewCustom!!.pause()
        }
        result.success(true)
    }

    private fun resumeCamera(result: MethodChannel.Result) {
        if (barcodeViewCustom == null) return barCodeViewNotSet(result, "resumeCamera")

        if (!barcodeViewCustom!!.getBarcodeView()!!.isPreviewActive) {
            isPaused = false
            barcodeViewCustom!!.resume()
        }
        result.success(true)
    }

    /**
     * Controls for the scanner
     */
    private fun startScan(arguments: List<Int>?, result: MethodChannel.Result) {
        val allowedBarcodeTypes = mutableListOf<BarcodeFormat>()
        try {
            checkAndRequestPermission(result)

            arguments?.forEach {
                allowedBarcodeTypes.add(BarcodeFormat.values()[it])
            }
        } catch (e: java.lang.Exception) {
            result.error(null, null, null)
        }

        barcodeViewCustom?.decodeContinuous(
            object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult) {
                    if (allowedBarcodeTypes.size == 0 || allowedBarcodeTypes.contains(result.barcodeFormat)) {
                        val code = mapOf(
                            "code" to result.text,
                            "type" to result.barcodeFormat.name,
                            "rawBytes" to result.rawBytes
                        )
                        channel.invokeMethod("onRecognizeQR", code)
                    }

                }

                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
            }
        )
    }

    private fun stopScan() {
        barcodeViewCustom?.getBarcodeView()?.stopDecoding()
    }

    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private fun getSystemFeatures(result: MethodChannel.Result) {
        try {
            result.success(
                mapOf(
                    "hasFrontCamera" to hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT),
                    "hasBackCamera" to hasSystemFeature(PackageManager.FEATURE_CAMERA),
                    "hasFlash" to hasFlash(),
                    "activeCamera" to barcodeViewCustom?.cameraSettings?.requestedCameraId
                )
            )
        } catch (e: Exception) {
            result.error(null, e.message, e.stackTrace)
        }
    }

    /**
     * Functions for checking permissions
     */
    private fun hasCameraPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                activityPluginBinding.activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndRequestPermission(result: MethodChannel.Result?) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (activityPluginBinding.activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    channel.invokeMethod("onPermissionSet", true)
                } else {
                    activityPluginBinding.activity.requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        cameraRequestId + this.id
                    )
                }
            }
            else -> {
                // We should have permissions on older OS versions
                channel.invokeMethod("onPermissionSet", true)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == cameraRequestId + this.id) {
            return if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                channel.invokeMethod("onPermissionSet", true)
                true
            } else {
                channel.invokeMethod("onPermissionSet", false)
                false
            }
        }
        return false
    }

    /**
     * Other helper functions
     */
    private fun barCodeViewNotSet(result: MethodChannel.Result, functionName: String?) {
        result.error("404", "No barcode view found", functionName)
    }

    private fun convertDpToPixels(dp: Double) =
        (dp * context.resources.displayMetrics.density).toInt()

    private fun hasSystemFeature(feature: String): Boolean {
        return activityPluginBinding.activity.packageManager
            .hasSystemFeature(feature)
    }
}

