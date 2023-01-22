package com.example.guniattendance.authorization.authfragments.ui.registerstudent

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentStudentRegisterBinding
import com.example.guniattendance.utils.Constants.DATA
import com.example.guniattendance.utils.EventObserver
import com.example.guniattendance.utils.showProgress
import com.example.guniattendance.utils.snackbar
import com.jianastrero.capiche.doIHave
import com.jianastrero.capiche.iNeed
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StudentRegisterFragment : Fragment(R.layout.fragment_student_register) {

    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentStudentRegisterBinding
    private lateinit var viewModel: StudentRegisterViewModel
    private var curImageUri: Uri = Uri.EMPTY
    private var sem = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[StudentRegisterViewModel::class.java]
        subscribeToObserve()

        binding = FragmentStudentRegisterBinding.bind(view)

        val semesters = requireActivity().resources.getStringArray(R.array.semester)
        val arrayAdapterSem = ArrayAdapter(
            requireContext(),
            androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
            semesters
        )

        val branch = requireActivity().resources.getStringArray(R.array.branch)
        val arrayAdapterBranch = ArrayAdapter(
            requireContext(),
            androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
            branch
        )

        binding.apply {

            autoCompleteTvBranch.setAdapter(arrayAdapterBranch)

            autoCompleteTvSem.setAdapter(arrayAdapterSem)

            autoCompleteTvSem.setOnItemClickListener { _, _, i, _ ->

                autoCompleteTvClass.setText("")
                autoCompleteTvLab.setText("")

                sem = i + 1

                val classes = DATA[i].second
                val labs = DATA[i].third

                val arrayAdapterClass = ArrayAdapter(
                    requireContext(),
                    androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
                    classes
                )

                val arrayAdapterLabs = ArrayAdapter(
                    requireContext(),
                    androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
                    labs
                )

                autoCompleteTvClass.setAdapter(arrayAdapterClass)
                autoCompleteTvLab.setAdapter(arrayAdapterLabs)

                tlClass.isVisible = true
                tlLab.isVisible = true
            }

            btnRegister.setOnClickListener {

                viewModel.register(
                    enrolment = etEnrol.text?.trim().toString(),
                    name = etName.text?.trim().toString(),
                    email = etEmail.text?.trim().toString(),
                    phone = etPhone.text?.trim().toString(),
                    branch = autoCompleteTvBranch.text?.trim().toString(),
                    sem = sem,
                    pin = pinView.text.toString(),
                    lec = autoCompleteTvClass.text?.trim().toString(),
                    lab = autoCompleteTvLab.text?.trim().toString()
                )
            }
            val storeArray=ArrayList<String>()
            storeArray.add(etEnrol.text.toString())
            storeArray.add(etName.text.toString())
            storeArray.add(etEmail.text.toString())
            storeArray.add(etPhone.text.toString())
            storeArray.add(autoCompleteTvBranch.text.toString())
            storeArray.add(sem.toString())
            storeArray.add(pinView.text.toString())
            storeArray.add(autoCompleteTvClass.text.toString())
            storeArray.add(autoCompleteTvLab.text.toString())




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

    private fun subscribeToObserve() {
        viewModel.curImageUri.observe(viewLifecycleOwner) {
            binding.tvCaptureImage.isVisible = it == Uri.EMPTY
            if (it == Uri.EMPTY) {
                binding.ivImage.setImageResource(R.drawable.ic_round_person_24)
            } else {
                curImageUri = it
                glide.load(curImageUri).into(binding.ivImage)
            }
        }

        viewModel.registerStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                showProgress(
                    activity = requireActivity(),
                    bool = false,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
                when (it) {
                    "emptyEmail" -> {
                        binding.etEmail.error = "Email cannot be empty"
                    }
                    "email" -> {
                        binding.etEmail.error = "Enter a valid email"
                    }
                    "emptyPhone" -> {
                        binding.etPhone.error = "Phone number cannot be empty"
                    }
                    "phone" -> {
                        binding.etPhone.error = "Enter a valid phone number"
                    }
                    "emptyBranch" -> {
                        binding.autoCompleteTvBranch.error = "Please enter branch"
                    }
                    "name" -> {
                        binding.etName.error = "Name cannot be empty"
                    }
                    "sem" -> {
                        binding.autoCompleteTvSem.error = "Please select semester"
                    }
                    "pin" -> {
                        snackbar("Pin should be of 6 length")
                    }
                    "uri" -> {
                        snackbar("Capture your image")
                    }
                    "emptyEnrolment" -> {
                        binding.etEnrol.error = "Please enter enrolment number"
                    }
                    "enrolment" -> {
                        binding.etEnrol.error = "Enrolment number should be of length 11"
                    }
                    "emptyLec" -> {
                        binding.autoCompleteTvClass.error = "Please enter your class"
                    }
                    "emptyLab" -> {
                        binding.autoCompleteTvLab.error = "Please enter your lab"
                    }
                    else -> snackbar(it)
                }
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
            binding.apply {
                etEmail.setText("")
                etName.setText("")
                autoCompleteTvSem.setText("")
                autoCompleteTvLab.setText("")
                autoCompleteTvLab.setText("")
                etEnrol.setText("")
                etPhone.setText("")
                pinView.setText("")
            }

            showProgress(
                activity = requireActivity(),
                bool = false,
                parentLayout = binding.parentLayout,
                loading = binding.lottieAnimation
            )

            findNavController().navigate(
                StudentRegisterFragmentDirections
                    .actionStudentRegisterFragmentToAppPinFragment("student")
            )
        })
    }

}