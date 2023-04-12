package com.example.guniattendance.ml.google.support

import android.content.Context
import android.content.SharedPreferences
import android.util.Size
import androidx.preference.PreferenceManager
import com.example.guniattendance.ml.google.support.CameraSource.SizePair
import com.google.mlkit.vision.face.FaceDetectorOptions


class PreferenceUtils {
    companion object{
        fun shouldHideDetectionInfo(context: Context): Boolean {
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            val prefKey: String = "R.string.pref_key_info_hide"//context.getString("R.string.pref_key_info_hide")
            return sharedPreferences.getBoolean(prefKey, false)
        }
        fun isCameraLiveViewportEnabled(context: Context): Boolean {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val prefKey = "pref_key_camera_live_viewport"//context.getString(R.string.pref_key_camera_live_viewport)
            return sharedPreferences.getBoolean(prefKey, false)
        }
        fun getFaceDetectorOptions(context: Context): FaceDetectorOptions {
            val landmarkMode: Int =
                FaceDetectorOptions.LANDMARK_MODE_NONE

            val contourMode: Int =
                FaceDetectorOptions.CONTOUR_MODE_ALL

            val classificationMode: Int =
                FaceDetectorOptions.CLASSIFICATION_MODE_NONE

            val performanceMode: Int =
                FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE


            val enableFaceTracking =
                false

            val minFaceSize = 0.1F
            val optionsBuilder = FaceDetectorOptions.Builder()
                .setLandmarkMode(landmarkMode)
                .setContourMode(contourMode)
                .setClassificationMode(classificationMode)
                .setPerformanceMode(performanceMode)
                .setMinFaceSize(minFaceSize)
            if (enableFaceTracking) {
                optionsBuilder.enableTracking()
            }
            return optionsBuilder.build()
        }

        fun getCameraPreviewSizePair(context: Context, cameraId: Int): SizePair? {

                return null

        }
        fun getCameraXTargetResolution(context: Context, lensfacing: Int): Size? {

                return null

        }
    }
}