package com.example.guniattendance.student.studentfragments.ui.takeattendance

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Size
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentTakeAttendanceBinding
import com.example.guniattendance.facemodel.FaceNetModel
import com.example.guniattendance.facemodel.Models
import com.example.guniattendance.facemodel.utils.FileReader
import com.example.guniattendance.facemodel.utils.FrameAnalyser
import com.example.guniattendance.facemodel.utils.Logger
import com.example.guniattendance.utils.ImageUtils
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.concurrent.Executors

@AndroidEntryPoint
class TakeAttendanceFragment : Fragment(R.layout.fragment_take_attendance) {

    private lateinit var binding: FragmentTakeAttendanceBinding
    private lateinit var viewModel: TakeAttendanceViewModel
    private val SERIALIZED_DATA_FILENAME = "image_data"
    private lateinit var previewView : PreviewView
    private lateinit var frameAnalyser  : FrameAnalyser
    private lateinit var faceNetModel : FaceNetModel
    private lateinit var fileReader : FileReader
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private val useGpu = true
    private val useXNNPack = true
    private val modelInfo = Models.FACENET
    var profileImage:Bitmap? = null
    private lateinit var userInfo:JSONObject
    var attendanceData:JSONObject?=null
    companion object {

        lateinit var logTextView : TextView

        fun setMessage( message : String ) {
            logTextView.text = message
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TakeAttendanceViewModel::class.java]

        binding = FragmentTakeAttendanceBinding.bind(view)
        userInfo = JSONObject(requireArguments().getString("userInfo")!!)
        attendanceData = JSONObject(requireArguments().getString("attendanceData")!!)

        profileImage = ImageUtils.convertStringtoBitmap(requireArguments().getString("profileImage")!!)

        logTextView=binding.logTextview
        previewView=binding.previewView
        logTextView.movementMethod = ScrollingMovementMethod()
        // Necessary to keep the Overlay above the PreviewView so that the boxes are visible.
        val boundingBoxOverlay = binding.bboxOverlay

        boundingBoxOverlay.setWillNotDraw(false)
        boundingBoxOverlay.setZOrderOnTop(true)

        faceNetModel = FaceNetModel( requireContext() , modelInfo , useGpu , useXNNPack )
        frameAnalyser = FrameAnalyser( requireContext() , boundingBoxOverlay , faceNetModel )
        fileReader = FileReader(faceNetModel)

        if (ActivityCompat.checkSelfPermission(requireContext() , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            requestCameraPermission()
        }
        else
        {
            startCameraPreview()
        }
        val images = ArrayList<Pair<String, Bitmap>>()
        try {
            MainScope().launch {
                //val myBitmap = BitmapFactory.decodeResource(getResources(), R.raw.myphoto)
                images.add(Pair(userInfo.getString("lastname"), profileImage!!))
                fileReader.run( images , fileReaderCallback )
                Logger.log( "Detecting faces in ${images.size} images ..." )
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance( requireContext() )
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider) },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder().build()
        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing( CameraSelector.LENS_FACING_FRONT )
            .build()
        preview.setSurfaceProvider( previewView.surfaceProvider )
        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size( 480, 640 ) )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser )
        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview , imageFrameAnalysis  )
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch( Manifest.permission.CAMERA )
    }

    private val cameraPermissionLauncher = registerForActivityResult( ActivityResultContracts.RequestPermission() ) {
            isGranted ->
        if ( isGranted ) {
            startCameraPreview()
        }
        else {
            val alertDialog = AlertDialog.Builder( requireContext() ).apply {
                setTitle( "Camera Permission")
                setMessage( "The app couldn't function without the camera permission." )
                setCancelable( false )
                setPositiveButton( "ALLOW" ) { dialog, _ ->
                    dialog.dismiss()
                    requestCameraPermission()
                }
                setNegativeButton( "CLOSE" ) { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().finish()
                }
                create()
            }
            alertDialog.show()
        }

    }


    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(data: ArrayList<Pair<String, FloatArray>>, numImagesWithNoFaces: Int) {
            frameAnalyser.faceList = data
            saveSerializedImageData( data )
            Logger.log( "Images parsed. Found $numImagesWithNoFaces images with no faces." )
        }
    }


    private fun saveSerializedImageData(data : ArrayList<Pair<String,FloatArray>> ) {
        val serializedDataFile = File( requireContext().filesDir , SERIALIZED_DATA_FILENAME )
        ObjectOutputStream( FileOutputStream( serializedDataFile )  ).apply {
            writeObject( data )
            flush()
            close()
        }
    }

}