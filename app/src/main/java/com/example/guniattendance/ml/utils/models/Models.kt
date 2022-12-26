package com.example.guniattendance.ml.utils.models

import com.example.guniattendance.ml.utils.models.ModelInfo

class Models {
    companion object {

        val FACENET = ModelInfo(
            "FaceNet",
            "facenet.tflite",
            0.4f,
            10f,
            128,
            160
        )

    }
}