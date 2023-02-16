package com.example.guniattendance.student.studentfragments.ui.takeattendance

import android.Manifest
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.guniattendance.R
import com.example.guniattendance.authorization.authfragments.ui.launcherscreen.LauncherScreenFragment
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.databinding.FragmentTakeAttendanceBinding
import com.example.guniattendance.ml.utils.models.FaceNetModel
import com.example.guniattendance.ml.utils.models.Models
import com.example.guniattendance.ml.utils.FileReader
import com.example.guniattendance.ml.utils.FrameAnalyserAttendance
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.BasicUtils
import com.example.guniattendance.utils.EventObserver
import com.example.guniattendance.utils.showProgress
import com.example.guniattendance.utils.snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.guni.uvpce.moodleapplibrary.model.BaseUserInfo
import com.jianastrero.capiche.doIHave
import com.jianastrero.capiche.iNeed
import com.guni.uvpce.moodleapplibrary.util.BitmapUtils
import com.guni.uvpce.moodleapplibrary.util.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


@AndroidEntryPoint
class TakeAttendanceFragment : Fragment(R.layout.fragment_take_attendance) {

    private lateinit var binding: FragmentTakeAttendanceBinding
    private lateinit var viewModel: TakeAttendanceViewModel
//    private val args: TakeAttendanceFragmentArgs by navArgs()

    private lateinit var frameAnalyser: FrameAnalyserAttendance
    private lateinit var faceNetModel: FaceNetModel

    private lateinit var fileReader: FileReader
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    // Use the device's GPU to perform faster computations.
    private val useGpu = true

    private val modelInfo = Models.FACENET
    var userId:String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[TakeAttendanceViewModel::class.java]
        Log.i(TAG, "onViewCreated: 1")
        subscribeToObserve()
        Log.i(TAG, "onViewCreated: 9")

        binding = FragmentTakeAttendanceBinding.bind(view)
/*
        requireActivity().doIHave(
            Manifest.permission.CAMERA,
            onGranted = {
                startCameraPreview()
            },
            onDenied = {
                requestPermission()
            })
        viewModel.getStudent(args.uid)
        showProgress(
            activity = requireActivity(),
            bool = true,
            parentLayout = binding.parentLayout,
            loading = binding.lottieAnimation
        )*/
        requestPermission()
        binding.apply {
            Log.i(TAG, "onViewCreated: 10")
            bboxOverlay.setWillNotDraw(false)
            bboxOverlay.setZOrderOnTop(true)

            faceNetModel = FaceNetModel(requireContext(), modelInfo, useGpu)
            frameAnalyser = FrameAnalyserAttendance(requireContext(), bboxOverlay, faceNetModel)
            fileReader = FileReader(faceNetModel)
            MainScope().launch {
                Log.i(TAG, "onViewCreated: 11")
                //var result = MoodleConfig.getModelRepo(requireActivity()).getUserInfo(LauncherScreenFragment.studentEnrolment)
                viewModel.getStudent(requireContext(), LauncherScreenFragment.studentEnrolment)
            }

//            userId = arguments?.getString("userId")!!
//            Log.i(TAG, "onViewCreated: $userId")

        }
    }

    private fun subscribeToObserve() {
        Log.i(TAG, "subscribeToObserve: 2")
        viewModel.removeObservers()
        Log.i(TAG, "subscribeToObserve: 4")

        /*viewModel.attendanceStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                showProgress(
                    activity = requireActivity(),
                    bool = false,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
                Log.i("TAG_ATTENDANCE", "subscribeToObserve: $it")
                snackbar(it)
            },
            onLoading = {
                showProgress(
                    activity = requireActivity(),
                    bool = true,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
            }
        ) {
            showProgress(
                activity = requireActivity(),
                bool = false,
                parentLayout = binding.parentLayout,
                loading = binding.lottieAnimation
            )
            snackbar("Attendance added for $it")
//            findNavController().navigateUp()
        })*/

        viewModel.studentListStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                Log.i(TAG, "subscribeToObserve: 5")
                showProgress(
                    activity = requireActivity(),
                    bool = true,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
                snackbar(it)
            },
            onLoading = {
                Log.i(TAG, "subscribeToObserve: 6")
                showProgress(
                    activity = requireActivity(),
                    bool = true,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
            }
        ) {
            Log.i(TAG, "subscribeToObserve: 7")
            showProgress(
                activity = requireActivity(),
                bool = false,
                parentLayout = binding.parentLayout,
                loading = binding.lottieAnimation
            )
            Log.i(TAG, "subscribeToObserve: 8")
            start(it)
            MainScope().launch {
                faceProcessing(it)
            }

        })
    }

    private fun requestPermission() {
        requireActivity().iNeed(
            Manifest.permission.CAMERA,
            onGranted = {
                startCameraPreview()
            },
            onDenied = {
                snackbar("Camera permission needed to access features")
            }
        )
    }

    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()
        // CameraSelector - wraps a set of filters for the camera you want to use
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser)
        var camera = cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector,
            preview,
            imageFrameAnalysis
        )

    }

    private fun start(student: BaseUserInfo) {

        val enrolNo = student.username

        //val decodedByteArray: ByteArray = Utility().convertUrlToBase64(student.imageUrl).toByteArray()
        //val bitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)

        val images = java.util.ArrayList<Pair<String, Bitmap>>()
        images.add(Pair(enrolNo, viewModel.repo!!.getURLtoBitmap(student.imageUrl)!!))
        fileReader.run(images, fileReaderCallback)
    }

    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(
            data: ArrayList<Pair<String, FloatArray>>,
            numImagesWithNoFaces: Int
        ) {
            //requestPermission()
            Log.i(TAG, "onProcessCompleted: ${data.size}")
            if(data.size > 0)
                frameAnalyser.run(data, frameAnalyserCallback)
        }
    }

    private val frameAnalyserCallback = object : FrameAnalyserAttendance.ResultCallback {
        override fun onResultGot(name: String) {
            Log.d("TAG_ATTENDANCE", "onResultGot: here")
            requireActivity().runOnUiThread {
                cameraProviderFuture.get().unbindAll()
                Log.d("TAG_ATTENDANCE", "onResultGot: $name")
                showProgress(
                    activity = requireActivity(),
                    bool = true,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
//                viewModel.addAttendance(args.attendanceUid, name)
            }
        }
    }

    private suspend fun faceProcessing(student: BaseUserInfo){
        val images = java.util.ArrayList<Pair<String, Bitmap>>()

//        images.add(Pair(student.username,Utility().convertUrlToBitmap(student.imageUrl)!!))
        images.add(Pair(student.username,MoodleConfig.getModelRepo(requireContext()).getURLtoBitmap(student.imageUrl)!!))
        faceNetModel = FaceNetModel(requireContext(), Models.FACENET, true)
        //frameAnalyser = FrameAnalyserAttendance(requireContext(), bboxOverlay, faceNetModel)

        val fileReader = FileReader(faceNetModel)
        Log.i(TAG, "faceProcessing: ${images.size}")
        fileReader.runToDetectFaces(images, fileReaderCallback1)
    }
    private val fileReaderCallback1 = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(
            data: ArrayList<Pair<String, FloatArray>>,
            numImagesWithNoFaces: Int
        ) {
            //Toast.makeText(context, "Image Processed: Face Detected: ${data.size > 0}", Toast.LENGTH_SHORT).show()
            if(data.size > 0)
            {
                Toast.makeText(requireContext(),"Successfully detect face",Toast.LENGTH_LONG).show()
            }
            else{
                BasicUtils.errorDialogBox(requireContext(),"Error","Face is not detected in image.")

            }
            //frameAnalyser.run(data, frameAnalyserCallback)
        }
    }
}