package com.example.guniattendance.student.studentfragments.ui.studenthome

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.guniattendance.R
import com.example.guniattendance.SettingsActivity
import com.example.guniattendance.authorization.AuthActivity
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.databinding.FragmentStudentHomeBinding
import com.example.guniattendance.utils.Constants.ALLOWED_RADIUS
import com.example.guniattendance.utils.EventObserver
import com.example.guniattendance.utils.showProgress
import com.example.guniattendance.utils.snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jianastrero.capiche.doIHave
import com.jianastrero.capiche.iNeed
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StudentHomeFragment : Fragment(R.layout.fragment_student_home) {

    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentStudentHomeBinding
    private lateinit var viewModel: StudentHomeViewModel
    private var uid = ""
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var curLocation: Location? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[StudentHomeViewModel::class.java]
        subscribeToObserve()

        binding = FragmentStudentHomeBinding.bind(view)

        requireActivity().doIHave(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            onGranted = {
                curLocation = getLocation()
            },
            onDenied = {
                requestPermission()
            })

        viewModel.getUser()

        binding.apply {
//            layoutTakeAttendance.setOnClickListener {
//                viewModel.getActiveAttendance()
//            }
            

            btnSetting.setOnClickListener{
                Toast.makeText(context,"Settings",Toast.LENGTH_SHORT).show()
                val i = Intent(context, SettingsActivity::class.java)
                startActivity(i)
            }

            btnSignOut.setOnClickListener {
//                FirebaseAuth.getInstance().signOut()
//                Intent(requireActivity(), AuthActivity::class.java).also {
//                    startActivity(it)
//                    requireActivity().finish()
//                }
                try{
                    findNavController().navigate(StudentHomeFragmentDirections.actionStudentHomeFragmentToScannerFragment())
                } catch (e: Exception){
                    Log.e("","Navigate Error"+ e.toString(),e)
                }

            }

//            tvTakeOtherAttendance.setOnClickListener {
//                layoutTakeOtherAttendance.isVisible = true
//                layoutTakeAttendance.isVisible = false
//                tvGoBack.isVisible = true
//                tvTakeOtherAttendance.isVisible = false
//            }
//
//            tvGoBack.setOnClickListener {
//                layoutTakeOtherAttendance.isVisible = false
//                layoutTakeAttendance.isVisible = true
//                tvGoBack.isVisible = false
//                tvTakeOtherAttendance.isVisible = true
//            }
//
//            btnTakeOtherAttendance.setOnClickListener {
//                viewModel.getActiveAttendanceForOther(etEnrol.text.trim().toString())
//            }

        }
    }

    private fun subscribeToObserve() {

        viewModel.removeObservers()

        viewModel.userStatus.observe(viewLifecycleOwner, EventObserver(
            onError = { error ->
                showProgress(
                    activity = requireActivity(),
                    bool = false,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
                snackbar(error)
            },
            onLoading = {
                showProgress(
                    activity = requireActivity(),
                    bool = true,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
            }
        ) { user ->
//            Log.i("updateUI", user.byteArray+", "+user.uid)
            updateUI(user)
        })

        viewModel.attendanceStatus.observe(viewLifecycleOwner, EventObserver(
            onError = {
                showProgress(
                    activity = requireActivity(),
                    bool = false,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
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
            if (it.uid != "") {
                if (curLocation != null) {
                    Log.d(
                        "TAG_LOCATION",
                        "subscribeToObserve: ${curLocation?.latitude}\t${curLocation?.longitude}"
                    )
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        it.latitude,
                        it.longitude,
                        curLocation!!.latitude,
                        curLocation!!.longitude,
                        results
                    )
                    val distanceInMeters = results[0]
                    Log.d("TAG_LOCATION", "subscribeToObserve: $distanceInMeters")
//                    if (distanceInMeters <= ALLOWED_RADIUS) {
//                        findNavController().navigate(
//                            StudentHomeFragmentDirections
//                                .actionStudentHomeFragmentToTakeAttendanceFragment(
//                                    uid = uid,
//                                    attendanceUid = it.uid
//                                )
//                        )
//                    } else {
//                        snackbar("Outside class")
//                    }

                } else {
                    curLocation = getLocation()
                    snackbar("Fetching location, Please try again")
                }
            } else {
                snackbar("No active attendance for you for now")
            }
        })

        viewModel.attendanceStatusForOther.observe(viewLifecycleOwner, EventObserver(
            onError = {
                showProgress(
                    activity = requireActivity(),
                    bool = false,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
//                when (it) {
//                    "emptyEnrolment" -> {
//                        binding.etEnrol.error = "Please enter enrolment number"
//                    }
//                    "enrolment" -> {
//                        binding.etEnrol.error = "Enrolment number should be of length 11"
//                    }
//                    else -> {
//                        snackbar(it)
//                    }
//                }
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
            val attendance = it.second
            if (attendance.uid != "") {
                if (curLocation != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        attendance.latitude,
                        attendance.longitude,
                        curLocation!!.latitude,
                        curLocation!!.longitude,
                        results
                    )
                    val distanceInMeters = results[0]
//                    if (distanceInMeters <= ALLOWED_RADIUS) {
//                        findNavController().navigate(
//                            StudentHomeFragmentDirections
//                                .actionStudentHomeFragmentToTakeAttendanceFragment(
//                                    uid = it.first,
//                                    attendanceUid = attendance.uid
//                                )
//                        )
//                    } else {
//                        snackbar("Outside class")
//                    }

                } else {
                    curLocation = getLocation()
                    snackbar("Fetching location, Please try again")
                }
            } else {
                snackbar("No active attendance for you for now")
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(user: Student) {
        binding.apply {
            uid = user.uid

            tvName.text = user.name
            val decodedByteArray: ByteArray = Base64.decode(user.byteArray, Base64.DEFAULT)
            glide.load(decodedByteArray).into(ivImage)
//            tvClass.text = "Class : ${user.lecture} \t Lab : ${user.lab}"
        }
        showProgress(
            activity = requireActivity(),
            bool = false,
            parentLayout = binding.parentLayout,
            loading = binding.lottieAnimation
        )
    }

    private fun requestPermission() {
        requireActivity().iNeed(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            onGranted = {
                curLocation = getLocation()
            },
            onDenied = {
                snackbar("Location permission needed to access features")
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager


        locationListener = object : LocationListener {

            override fun onLocationChanged(p0: Location) {
                curLocation = p0
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        return try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            Log.d("TAG_ERROR", "getLocation: ${e.message}")
            null
        }

    }

}