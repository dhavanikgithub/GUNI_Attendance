package com.example.guniattendance.ml.utils.models

import com.example.guniattendance.ml.utils.models.ModelInfo
import java.io.File

class Models {
    companion object {
        val FACENET = ModelInfo(
            "FaceNet",
            File("/storage/emulated/0/Android/data/com.example.guniattendance/files/facenet.tflite"),
            0.4f,
            10f,
            128,
            160
        )

    }
}