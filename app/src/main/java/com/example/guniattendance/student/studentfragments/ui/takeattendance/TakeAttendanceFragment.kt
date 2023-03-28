package com.example.guniattendance.student.studentfragments.ui.takeattendance

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.ScrollingMovementMethod
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentTakeAttendanceBinding
import com.example.guniattendance.facemodel.FaceNetModel
import com.example.guniattendance.facemodel.Models
import com.example.guniattendance.facemodel.utils.FileReader
import com.example.guniattendance.facemodel.utils.FrameAnalyser
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.BasicUtils
import com.example.guniattendance.utils.ImageUtils
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

@AndroidEntryPoint
class TakeAttendanceFragment : Fragment(R.layout.fragment_take_attendance) {

    private lateinit var binding: FragmentTakeAttendanceBinding
    private lateinit var viewModel: TakeAttendanceViewModel
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
    val TAG = "TakeAttendanceFragment"
    lateinit var countTimer:CountDownTimer
    companion object {

        lateinit var logTextView : TextView
//        fun setMessage( message : String ) {
//            logTextView.text = message
//        }
    }

    override fun onResume() {
        countTimer = object : CountDownTimer(70000, 1000) {

            // Callback function, fired on regular interval
            override fun onTick(millisUntilFinished: Long) {

            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                countTimer.cancel()
                findNavController().popBackStack()
            }
        }.start()
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
                images.add(Pair(userInfo.getString("lastname"), profileImage!!))
                fileReader.run( images , fileReaderCallback )
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
            frameAnalyser.run(data, frameAnalyserCallback)
        }
    }

    private val frameAnalyserCallback = object : FrameAnalyser.ResultCallback {
        override fun onResultGot(name: String) {
            Log.d("TAG_ATTENDANCE", "onResultGot: here")
            requireActivity().runOnUiThread {
                cameraProviderFuture.get().unbindAll()
                Log.d("TAG_ATTENDANCE", "onResultGot: $name")
                Log.i(TAG,"User Info: "+userInfo.toString(4))
                Log.i(TAG,"Attendance Data: "+attendanceData!!.toString(4))
                MainScope().launch {
                    try{
                        val moodleRepo = MoodleConfig.getModelRepo(requireContext())

                        val res = moodleRepo.takePresentAttendance(compressString(attendanceData.toString()),userInfo.getString("userId"))
                        if(res)
                        {
                            val bundle= Bundle()
                            bundle.putString("userInfo",userInfo.toString())
                            bundle.putString("attendanceData",attendanceData.toString())
                            findNavController().popBackStack(R.id.attendanceInfoFragment,true)
                            findNavController().navigate(R.id.successAttendanceFragment,bundle)
                        }
                        else{
                            BasicUtils.errorDialogBox(requireContext(),"Attendance not taken","Your attendance is not marked, please try again!")
                        }
                    }
                    catch (ex:Exception)
                    {
//                        snackbar("${ex.message}")
                        Log.e(TAG,"MarkAttendance Error: ${ex.message}")
                        findNavController().popBackStack()
                    }
                }

            }
        }
    }
    fun compressString(input: String): String {
        val baos = ByteArrayOutputStream()
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        //deflater.level = Deflater.BEST_COMPRESSION
        val dos = DeflaterOutputStream(baos, deflater)
        dos.write(input.toByteArray())
        dos.close()
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }

    override fun onDetach() {
        countTimer.cancel()
        super.onDetach()
    }

    override fun onDestroy() {
        countTimer.cancel()
        super.onDestroy()
    }

    override fun onDestroyView() {
        countTimer.cancel()
        super.onDestroyView()
    }

    override fun onPause() {
        countTimer.cancel()
        super.onPause()
    }
}