package com.example.guniattendance.student.studentfragments.ui.scanner

import android.Manifest
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.example.guniattendance.R
import com.example.guniattendance.SettingsActivity
import com.example.guniattendance.authorization.AuthActivity
import com.example.guniattendance.databinding.FragmentScannerBinding
import com.example.guniattendance.databinding.FragmentStudentHomeBinding
import com.example.guniattendance.student.studentfragments.ui.studenthome.StudentHomeFragmentDirections
import com.example.guniattendance.student.studentfragments.ui.studenthome.StudentHomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jianastrero.capiche.doIHave

class ScannerFragment : Fragment(R.layout.fragment_scanner) {

    private lateinit var binding: FragmentScannerBinding
    private lateinit var viewModel: ScannerViewModel
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var codeScanner: CodeScanner
//    private lateinit var fragScannerView: CodeScannerView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ScannerViewModel::class.java]
        binding = FragmentScannerBinding.bind(view)


        binding.apply {
            codeScanner = context?.let { CodeScanner(it, fragScannerView) }!!

            codeScanner.apply{
                codeScanner.camera = CodeScanner.CAMERA_BACK
                codeScanner.autoFocusMode = AutoFocusMode.SAFE
                codeScanner.formats = CodeScanner.ALL_FORMATS
                scanMode = ScanMode.SINGLE
                isAutoFocusEnabled = true
                isTouchFocusEnabled = false

                codeScanner.decodeCallback = DecodeCallback {
                    handler.post(Runnable {
                        // Edited by Jaydeepsinh
                        val encryptedMsg = it.text
                        val encodedData = encryptedMsg.split("+")[0]
                        //Code to Next Activity i.e. StudentHome Should be here
                        //val navHostFragment: NavHostFragment = FragmentManager.findFragment(view) as NavHostFragment
                        Toast.makeText(context, encryptedMsg, Toast.LENGTH_SHORT).show()
                       // binding.fragFlayout.visibility = View.VISIBLE
                            findNavController().navigate(
                                ScannerFragmentDirections.actionScannerFragmentToTakeAttendanceFragment2("123","123")
                            )

                    })
                }

                codeScanner.errorCallback = ErrorCallback {
                    handler.post(Runnable {
                        //Handle Errors Here
                    })

                    }
                }

            }
            binding.fragScannerView.setOnClickListener {
                codeScanner.startPreview()
            }
        }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }
}

