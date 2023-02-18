package com.example.guniattendance.ml.google.support

import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.guniattendance.ml.google.support.GraphicOverlay.Graphic


/** Draw camera image to background.  */
class CameraImageGraphic(overlay: GraphicOverlay?, private val bitmap: Bitmap) : Graphic(
    overlay!!
) {
    override fun draw(canvas: Canvas?) {
        canvas!!.drawBitmap(bitmap, getTransformationMatrix(), null)
    }
}