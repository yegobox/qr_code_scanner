package net.touchcapture.qr.flutterqr

import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class QRViewFactory(@NonNull private val flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    var qrView: QRView? = null
    private var activity: ActivityPluginBinding? = null

    @Suppress("UNCHECKED_CAST")
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        val params = args as HashMap<String, Any>
        qrView = QRView(context, flutterPluginBinding, activity!!, id, params)
        return qrView!!
    }

    // Activity Aware
    fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        activity = activityPluginBinding
    }

    fun onDetachedFromActivity() {
        activity = null
    }

}