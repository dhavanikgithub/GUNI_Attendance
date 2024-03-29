package com.example.guniattendance.authorization.authfragments.ui.registerstudent

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentStudentRegisterBinding
import com.example.guniattendance.moodle.MoodleConfig
import com.example.guniattendance.student.StudentActivity
import com.example.guniattendance.utils.BasicUtils
import com.example.guniattendance.utils.CustomProgressDialog
import com.example.guniattendance.utils.snackbar
import com.jianastrero.capiche.doIHave
import com.jianastrero.capiche.iNeed
import dagger.hilt.android.AndroidEntryPoint
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
//    private var sem = 0
    var userid: String? =  null
    var userName: String? = null
    private var customProgressDialog:CustomProgressDialog?=null
//    private lateinit var userInfo: BaseUserInfo
//    private var imgURL: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[StudentRegisterViewModel::class.java]

        subscribeToObserve()

        binding = FragmentStudentRegisterBinding.bind(view)
        if(customProgressDialog==null)
        {
            customProgressDialog= CustomProgressDialog(requireContext())
        }

        /*val callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                findNavController().navigate(StudentRegisterFragmentDirections.actionStudentRegisterFragmentToLauncherScreenFragment())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)*/

        binding.apply {
            val localStudentData = requireActivity().getSharedPreferences("studentData", 0)
            val enrol = localStudentData.getString("studentEnrolment", "")
            //Setting automatically the values of fields..
            enrollmentText.setText(enrol)
            enrollmentText.isEnabled = false

            MainScope().launch {
                try {
                    customProgressDialog!!.start("Preparing Info....")
                    val result = MoodleConfig.getModelRepo(requireActivity()).getUserInfo(enrol!!)
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
                    customProgressDialog!!.stop()
                }
            }

            btnRegister.setOnClickListener {
                customProgressDialog!!.start("Uploading....")
                MainScope().launch {
                    try{
                        if(curImageUri == Uri.EMPTY)
                        {
                            customProgressDialog!!.stop()
                            BasicUtils.errorDialogBox(requireContext(),"Error","Upload Image")
                            return@launch
                        }
                        val res = MoodleConfig.getModelRepo(requireActivity()).uploadStudentPicture(requireContext(),
                            userid!!,curImageUri)
                        Log.i("Successfully updated the profile picture:", res.toString(4))
                        customProgressDialog!!.stop()
                        try{
                            Intent(
                                requireActivity(),
                                StudentActivity::class.java
                            ).also { intent ->
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        } catch (e: Exception){
                            Log.e(TAG, "StudentRegisterFragment: ${e.message}")
                        }

                    } catch (e: Exception){
                        customProgressDialog!!.stop()
                        snackbar("Unknown Error, Contact Administrator!")
                        Log.e(TAG, "onViewCreated: ${e.message}")
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
                try{
                    startCrop()
                }
                catch(ex:Exception)
                {
                    snackbar(ex.message.toString())
                }

            },
            onDenied = {
                snackbar("Camera permission needed to access features")
            }
        )
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        try{
            if (result.isSuccessful) {
                curImageUri = result.uriContent!!
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, curImageUri)
                binding.ivImage.setImageBitmap(bitmap)
                viewModel.setCurrentImageUri(curImageUri)

            } else {
                val exception = result.error
                snackbar(exception.toString())
            }
        }
        catch(ex:Exception)
        {
            snackbar(ex.message.toString())
        }

    }

    private fun startCrop() {
        try{
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
        catch(ex:Exception)
        {
            snackbar(ex.message.toString())
        }

    }

    private fun subscribeToObserve() {
        viewModel.curImageUri.observe(viewLifecycleOwner) {
            binding.tvCaptureImage.isVisible = it == Uri.EMPTY
            if (it == Uri.EMPTY) {
                binding.ivImage.setImageResource(R.drawable.ic_round_person_24)
            } else {
                curImageUri = it
            }
        }
    }

}