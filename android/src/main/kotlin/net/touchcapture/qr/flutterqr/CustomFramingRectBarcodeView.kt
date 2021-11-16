package net.touchcapture.qr.flutterqr

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.Size

class CustomFramingRectBarcodeView : BarcodeView {

    private var bottomOffset = BOTTOM_OFFSET_NOT_SET_VALUE

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * Calculate framing rectangle, relative to the preview frame.
     *
     * Note that the SurfaceView may be larger than the container.
     *
     * Override this for more control over the framing rect calculations.
     *
     * @param container this container, with left = top = 0
     * @param surface   the SurfaceView, relative to this container
     * @return the framing rect, relative to this container
     */
     override fun calculateFramingRect(container: Rect?, surface: Rect?): Rect? {
        // intersection is the part of the container that is used for the preview
        val intersection = Rect(container)
        val intersects = intersection.intersect(surface!!)
        if (framingRectSize != null) {
            // Specific size is specified. Make sure it's not larger than the container or surface.
            val horizontalMargin = Math.max(0, (intersection.width() - framingRectSize.width) / 2)
            val verticalMargin = Math.max(0, (intersection.height() - framingRectSize.height) / 2)
            intersection.inset(horizontalMargin, verticalMargin)
            return intersection
        }
        // margin as 10% (default) of the smaller of width, height
        val margin =
            Math.min(intersection.width() * marginFraction, intersection.height() * marginFraction)
                .toInt()
        intersection.inset(margin, margin)
        if (intersection.height() > intersection.width()) {
            // We don't want a frame that is taller than wide.
            intersection.inset(0, (intersection.height() - intersection.width()) / 2)
        }
        return intersection
    }

//    override fun calculateFramingRect(container: Rect, surface: Rect): Rect {
//        val containerArea = Rect(container)
//        val intersects =
//            containerArea.intersect(surface) //adjusts the containerArea (code from super.calculateFramingRect)
//        val scanAreaRect = super.calculateFramingRect(container, surface)
//        if (bottomOffset != BOTTOM_OFFSET_NOT_SET_VALUE) { //if the setFramingRect function was called, then we shift the scan area by Y
//            val scanAreaRectWithOffset = Rect(scanAreaRect)
//            scanAreaRectWithOffset.bottom -= bottomOffset
//            scanAreaRectWithOffset.top -= bottomOffset
//            val belongsToContainer = scanAreaRectWithOffset.intersect(containerArea)
//            if (belongsToContainer) {
//                return scanAreaRectWithOffset
//            }
//        }
//        return scanAreaRect
//    }

    fun setFramingRect(rectWidth: Int, rectHeight: Int, bottomOffset: Int) {
        this.bottomOffset = bottomOffset
        framingRectSize = Size(rectWidth, rectHeight)
    }

    companion object {
        private const val BOTTOM_OFFSET_NOT_SET_VALUE = -1
    }
}