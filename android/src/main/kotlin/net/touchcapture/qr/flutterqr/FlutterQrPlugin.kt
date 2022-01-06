package net.touchcapture.qr.flutterqr

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

class FlutterQrPlugin : FlutterPlugin, ActivityAware {

    private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding

    private lateinit var qrViewFactory: QRViewFactory

    /** Plugin registration embedding v2 */
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        qrViewFactory = QRViewFactory(flutterPluginBinding)
        this.flutterPluginBinding = flutterPluginBinding
        flutterPluginBinding.platformViewRegistry
            .registerViewFactory(
                "net.touchcapture.qr.flutterqr/qrview", qrViewFactory
            )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }

    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        qrViewFactory.onAttachedToActivity(activityPluginBinding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        qrViewFactory.onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
        qrViewFactory.onAttachedToActivity(activityPluginBinding)
    }

    override fun onDetachedFromActivity() {
        qrViewFactory.onDetachedFromActivity()
    }
}
