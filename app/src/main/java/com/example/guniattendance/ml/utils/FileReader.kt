package com.example.guniattendance.ml.utils

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.example.guniattendance.ml.utils.models.FaceNetModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.guni.uvpce.moodleapplibrary.util.BitmapUtils
import kotlinx.coroutines.*

class FileReader(private var faceNetModel: FaceNetModel) {

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setMinFaceSize(0.1F)
        .build()
    // realTimeOpts = Options
    // Instance of Face Detector
    private val detector = FaceDetection.getClient(realTimeOpts)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var numImagesWithNoFaces = 0
    private var imageCounter = 0
    private var numImages = 0
    private var data = ArrayList<Pair<String, Bitmap>>()
//    private var imgData = ArrayList<Pair<String, FloatArray>>()
    private lateinit var callback: ProcessCallback

    // imageData will be provided to the MainActivity via ProcessCallback ( see the run() method below ) and finally,
    // used by the FrameAnalyser class.
    private val imageData = ArrayList<Pair<String,FloatArray>>()

    val TAG = "FileReader"

    // Given the Bitmaps, extract face embeddings from then and deliver the processed embedding to ProcessCallback.
    fun run(data: ArrayList<Pair<String, Bitmap>>, callback: ProcessCallback) {
        Log.i(TAG, "run: data size:${data.size}")
        numImages = data.size
        this.data = data
        this.callback = callback
        scanImage(data[imageCounter].first, data[imageCounter].second)
    }


    interface ProcessCallback {
        fun onProcessCompleted(data: ArrayList<Pair<String, FloatArray>>, numImagesWithNoFaces: Int)
    }


    // Crop faces and produce embeddings ( using FaceNet ) from given image.
    // Store the embedding in imageData
    private fun scanImage(name: String, image: Bitmap) {
        // Create InputImage obj
        Log.i(TAG, "detectFaceFromImg: 3")
        // InputImage obj with array
        /*val inputImage = InputImage.fromByteArray(
            BitmapUtils.bitmapToNV21ByteArray(image),
            image.width,
            image.height,
            0,
            InputImage.IMAGE_FORMAT_NV21
        )*/
        Log.i(TAG, "scanImage: input name=$name & image.width=${image.width}")
        detector.process(InputImage.fromBitmap(image,0))
            .addOnSuccessListener { faces ->
                Log.i(TAG, "scanImage: Total Detected Faces: ${faces.size}")
                if (faces.size != 0) {
                    Log.i(TAG, "scanImage: face size:${faces.size}")
                    coroutineScope.launch {
                        Log.i(TAG, "detectFaceFromImg: face size:${faces.size}")
                        /*
                        boundingBox - In object detection, we usually use a bounding box to describe the spatial
                        location of an object. The bounding box is rectangular, which is determined
                        by the x and y coordinates of the upper-left corner of the rectangle and the
                        such coordinates of the lower-right corner.
                        */
                        val embedding = getEmbedding(image, faces[0].boundingBox)
                        imageData.add(Pair(name, embedding))
                        // Embedding stored, now proceed to the next image.
                        if (imageCounter + 1 != numImages) {
                            imageCounter += 1
                            scanImage(data[imageCounter].first, data[imageCounter].second)
                        } else {
                            // Processing done, reset the file reader.
                            callback.onProcessCompleted(imageData, numImagesWithNoFaces)
                            reset()
                        }
                    }
                } else {
                    // The image
                    // contains no faces, proceed to the next one.
                    numImagesWithNoFaces += 1
                    if (imageCounter + 1 != numImages) {
                        imageCounter += 1
                        scanImage(data[imageCounter].first, data[imageCounter].second)
                    } else {
                        callback.onProcessCompleted(imageData, numImagesWithNoFaces)
                        reset()
                    }
                }
            }
            .addOnFailureListener {e ->
                // Task failed
                Log.i(TAG, "detectFaceFromImg: addOnFailureListener ${e.message}")
            }
    }

    fun runToDetectFaces(data: ArrayList<Pair<String, Bitmap>>, callback: ProcessCallback) {
        Log.i(TAG, "run: data size:${data.size}")
        Log.i(TAG, "runToDetectFaces: 2")
        numImages = data.size
        // Store the image list in activity member variable
        this.data = data
        // Store the callback in member variable
        this.callback = callback
        // data[imageCounter].first - Enrollment No
        // data[imageCounter].second - Image
        detectFaceFromImg(data[imageCounter].first, data[imageCounter].second)
    }

    private fun detectFaceFromImg(name: String, image: Bitmap) {
        // Create InputImage obj
        Log.i(TAG, "detectFaceFromImg: 3")
        // InputImage obj with array
        /*val inputImage = InputImage.fromByteArray(
            BitmapUtils.bitmapToNV21ByteArray(image),
            image.width,
            image.height,
            0,
            InputImage.IMAGE_FORMAT_NV21
        )*/
        Log.i(TAG, "detectFaceFromImg: input name=$name & image.width=${image.width}")

        // Process the image
        detector.process(InputImage.fromBitmap(image,0))
            .addOnSuccessListener { faces ->
                // Task completed successfully
                if (faces.size != 0) {
                    Log.i(TAG, "scanImage: face size:${faces.size}")
                    coroutineScope.launch {
                        /*
                        boundingBox - In object detection, we usually use a bounding box to describe the spatial
                        location of an object. The bounding box is rectangular, which is determined
                        by the x and y coordinates of the upper-left corner of the rectangle and the
                        such coordinates of the lower-right corner.
                        */
                        // Embedding stored, now proceed to the next image.
                        val embedding = getEmbedding(image, faces[0].boundingBox)
                        imageData.add(Pair(name, embedding))
                        Log.i(TAG, "detectFaceFromImg: face size:${faces.size}")
                        if (imageCounter + 1 != numImages) {
                            imageCounter += 1
                            scanImage(data[imageCounter].first, data[imageCounter].second)
                        } else {
                            // Processing done, reset the file reader.
                            callback.onProcessCompleted(imageData, numImagesWithNoFaces)
                            reset()
                        }
                    }
                }
                else {
                    // The image
                    // contains no faces, proceed to the next one.
                    numImagesWithNoFaces += 1
                    if (imageCounter + 1 != numImages) {
                        imageCounter += 1
                        scanImage(data[imageCounter].first, data[imageCounter].second)
                    } else {
                        callback.onProcessCompleted(imageData, numImagesWithNoFaces)
                        reset()
                    }
                }
            }
            .addOnFailureListener {e ->
                // Task failed
                Log.i(TAG, "detectFaceFromImg: addOnFailureListener ${e.message}")
            }
    }


    // Suspend function for running the FaceNet model
    // Take two parameters, first is image and second is box position, which comes on the face while detecting.
    /*withContext( Dispatchers.Default ) - Inside the body of get, call withContext (Dispatchers.IO) to create a block that runs on the IO thread pool.
    Any code you put inside that block always executes via the IO dispatcher. Since withContext is itself a suspend function,
    the function get is also a suspend function.*/
    private suspend fun getEmbedding(image: Bitmap, bbox : Rect ) : FloatArray = withContext( Dispatchers.Default ) {
        Log.i(TAG, "getEmbedding: 3")
        Log.i(ContentValues.TAG, "getEmbedding")
        return@withContext faceNetModel.getFaceEmbedding(
            BitmapUtils.cropRectFromBitmap(
                image,
                bbox
            )
        )
    }
    private fun reset() {
        imageCounter = 0
        numImages = 0
        numImagesWithNoFaces = 0
        data.clear()
    }


}