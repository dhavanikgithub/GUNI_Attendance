package com.example.guniattendance.authorization.authfragments.ui.registerstudent

import android.Manifest
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.guniattendance.R
import com.example.guniattendance.authorization.authfragments.ui.launcherscreen.LauncherScreenFragment
import com.example.guniattendance.databinding.FragmentStudentRegisterBinding
import com.example.guniattendance.ml.google.FaceDetectorProcessor
import com.example.guniattendance.ml.google.support.GraphicOverlay
import com.example.guniattendance.ml.google.support.PreferenceUtils
import com.example.guniattendance.ml.utils.FileReader
import com.example.guniattendance.ml.utils.FrameAnalyserAttendance
import com.example.guniattendance.ml.utils.models.FaceNetModel
import com.example.guniattendance.ml.utils.models.Models
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.utils.*
import com.example.guniattendancefaculty.moodle.model.BaseUserInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.jianastrero.capiche.doIHave
import com.jianastrero.capiche.iNeed
import com.uvpce.attendance_moodle_api_library.util.BitmapUtils
import com.uvpce.attendance_moodle_api_library.util.Utility
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class StudentRegisterFragment : Fragment(R.layout.fragment_student_register) {

    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentStudentRegisterBinding
    private lateinit var viewModel: StudentRegisterViewModel
    private var curImageUri: Uri = Uri.EMPTY
    private var sem = 0
    lateinit var userid: String
    lateinit var userName: String
    private lateinit var userInfo: BaseUserInfo
    private var imgURL: String = ""
    private lateinit var faceNetModel: FaceNetModel
    private lateinit var frameAnalyser: FrameAnalyserAttendance
    private var progressDialog: CustomProgressDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[StudentRegisterViewModel::class.java]
        if(progressDialog==null)
        {
            progressDialog= CustomProgressDialog(requireContext(),requireActivity())
        }
        subscribeToObserve()

        binding = FragmentStudentRegisterBinding.bind(view)
        binding.apply {
            //Setting automatically the values of fields..
            val enrol = LauncherScreenFragment.studentEnrolment
            enrollmentText.setText(enrol)
            enrollmentText.isEnabled = false

            MainScope().launch {
                try {
                    progressDialog!!.start("Preparing Info....")
                    val result = MoodleConfig.getModelRepo(requireActivity()).getUserInfo(enrol)
                    userid = result.id
                    userName = result.username
                    nameText.setText(result.lastname)
                    nameText.isEnabled = false

                    emailText.setText(result.emailAddress)
                    emailText.isEnabled = false
                } catch (e: java.lang.Exception) {
                    activity?.let { BasicUtils.errorDialogBox(it, "getUserInfo Error","getUserInfo Error, Contact Administrator!"+e.message) }
                }
                finally {
                    progressDialog!!.stop()
                }
            }

            btnRegister.setOnClickListener {
                MainScope().launch {

                    try{
                        if(curImageUri == Uri.EMPTY)
                        {
                            BasicUtils.errorDialogBox(requireContext(),"Error","Upload Image")
                            return@launch
                        }
                        progressDialog!!.start("Uploading....")
                        val res = MoodleConfig.getModelRepo(requireActivity()).uploadStudentPicture(requireContext(),userid,curImageUri)
                        Log.i("Successfully updated the profile picture:", res.toString(4))
                        progressDialog!!.stop()
                        val images = java.util.ArrayList<Pair<String, Bitmap>>()
                        //userInfo = MoodleConfig.getModelRepo(requireContext()).getUserInfo(LauncherScreenFragment.studentEnrolment)
                        //imgURL = userInfo.imageUrl
                        //images.add(Pair(userid, Utility().convertUrlToBitmap(imgURL)!!))
                        images.add(Pair(userName,BitmapUtils.getBitmapFromUri(requireContext().contentResolver, curImageUri)))
                        faceNetModel = FaceNetModel(requireContext(), Models.FACENET, true)
                        //frameAnalyser = FrameAnalyserAttendance(requireContext(), bboxOverlay, faceNetModel)
                        val fileReader = FileReader(faceNetModel)
                        fileReader.runToDetectFaces(images, fileReaderCallback)
                        /*try {
                            val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(requireContext())
                            val processor = FaceDetectorProcessor(requireContext(), faceDetectorOptions) {
                                Toast.makeText(
                                    context,
                                    "Image Processed: Face Detected: ${it.isNotEmpty()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            //processor.processBitmap()

                            processor.detectInImage(
                                BitmapUtils.getBitmapFromUri(requireContext().contentResolver, curImageUri)).addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "detectInImage:Image Processed: Face Detected: ${it.isNotEmpty()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Can not create image processor: " + e.localizedMessage,
                                Toast.LENGTH_LONG
                            ).show()

                        }*/
                        try{
                            findNavController().navigate(StudentRegisterFragmentDirections.actionStudentRegisterFragmentToStudentHomeFragment())
                        } catch (e: Exception){
                            Log.e(TAG, "StudentRegisterFragment: ${e.message}", )
                        }

                    } catch (e: Exception){
                        snackbar("Unknown Error, Contact Administrator!")
                        Log.e(TAG, "onViewCreated: ${e.message}", )
//                        BasicUtils.errorDialogBox()
                    }
                }
            }
            ivImage.setOnClickListener {
                requireActivity().doIHave(
                    Manifest.permission.CAMERA,
                    onGranted = {
                        startCrop()
                    },
                    onDenied = {
                        requestPermission()
                    })
            }

        }

    }

    private fun requestPermission() {
        requireActivity().iNeed(
            Manifest.permission.CAMERA,
            onGranted = {
                startCrop()
            },
            onDenied = {
                snackbar("Camera permission needed to access features")
            }
        )
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            curImageUri = result.uriContent!!
            viewModel.setCurrentImageUri(curImageUri)
        } else {
            val exception = result.error
            snackbar(exception.toString())
        }
    }

    private fun startCrop() {
        cropImage.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1, 1)
                setCropShape(CropImageView.CropShape.OVAL)
                setOutputCompressQuality(70)
                setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                setImageSource(includeGallery = false, includeCamera = true)
            }
        )
    }

    private fun faceProcessing(callback:FileReader.ProcessCallback){
        val images = java.util.ArrayList<Pair<String, Bitmap>>()

        images.add(Pair(userName,BitmapUtils.getBitmapFromUri(requireContext().contentResolver, curImageUri)))
        faceNetModel = FaceNetModel(requireContext(), Models.FACENET, true)
        //frameAnalyser = FrameAnalyserAttendance(requireContext(), bboxOverlay, faceNetModel)
        val fileReader = FileReader(faceNetModel)
        Log.i(TAG, "onViewCreated: ${images.size}")
        fileReader.runToDetectFaces(images, callback)
    }
    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(
            data: ArrayList<Pair<String, FloatArray>>,
            numImagesWithNoFaces: Int
        ) {
            Toast.makeText(context, "Image Processed: Face Detected: ${data.size > 0}", Toast.LENGTH_SHORT).show()
            if(data.size > 0)
            {
                glide.load(curImageUri).into(binding.ivImage)
            }
            else{
                BasicUtils.errorDialogBox(requireContext(),"Error","Face is not detected in image.")
                curImageUri = Uri.EMPTY
            }
            //frameAnalyser.run(data, frameAnalyserCallback)
        }
    }
    private fun subscribeToObserve() {
        viewModel.curImageUri.observe(viewLifecycleOwner) {
            binding.tvCaptureImage.isVisible = it == Uri.EMPTY
            if (it == Uri.EMPTY) {
                binding.ivImage.setImageResource(R.drawable.ic_round_person_24)
            } else {
                curImageUri = it
                faceProcessing(fileReaderCallback)

            }
        }

        viewModel.registerStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
//                showProgress(
//                    activity = requireActivity(),
//                    bool = false,
//                    parentLayout = binding.parentLayout,
//                    loading = binding.lottieAnimation
//                )
                progressDialog!!.stop()
                when (it) {
                    "emptyEmail" -> {
                        binding.emailText.error = "Email cannot be empty"
                    }
                    "email" -> {
                        binding.emailText.error = "Enter a valid email"
                    }
                    "name" -> {
                        binding.nameText.error = "Name cannot be empty"
                    }
                    "uri" -> {
                        snackbar("Capture your image!")
                    }
                    else -> snackbar(it)
                }
            },
            onLoading = {
//                showProgress(
//                    activity = requireActivity(),
//                    bool = true,
//                    parentLayout = binding.parentLayout,
//                    loading = binding.lottieAnimation
//                )
                progressDialog!!.start("Preparing....")
            }
        ) {
//            binding.apply {
//                emailText.setText("")
//                nameText.setText("")
//                autoCompleteTvSem.setText("")
//                autoCompleteTvLab.setText("")
//                autoCompleteTvLab.setText("")
//                enrollmentText.setText("")
//                phoneText.setText("")
//                pinView.setText("")
//            }

//            showProgress(
//                activity = requireActivity(),
//                bool = false,
//                parentLayout = binding.parentLayout,
//                loading = binding.lottieAnimation
//            )
            progressDialog!!.stop()

            findNavController().navigate(
                StudentRegisterFragmentDirections
                    .actionStudentRegisterFragmentToAppPinFragment("student")
            )
        })
    }

}