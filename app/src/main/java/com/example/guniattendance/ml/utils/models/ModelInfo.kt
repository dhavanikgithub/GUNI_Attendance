package com.example.guniattendance.ml.utils.models

import java.io.File

data class ModelInfo(
    val name: String,
    val assetsFilename: File,
    val cosineThreshold: Float,
    val l2Threshold: Float,
    val outputDims: Int,
    val inputDims: Int
)
