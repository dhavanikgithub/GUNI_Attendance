package com.example.guniattendance.student.studentfragments.ui.scanner

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.*
import com.example.guniattendance.R
import com.example.guniattendance.authorization.authfragments.ui.launcherscreen.LauncherScreenFragment
import com.example.guniattendance.databinding.FragmentScannerBinding
import com.example.guniattendance.utils.BasicUtils
import com.guni.uvpce.moodleapplibrary.model.QRMessageData
import org.json.JSONException


class ScannerFragment : Fragment(R.layout.fragment_scanner) {

    private lateinit var binding: FragmentScannerBinding
    private lateinit var viewModel: ScannerViewModel
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var codeScanner: CodeScanner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

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
                isTouchFocusEnabled = true

                codeScanner.decodeCallback = DecodeCallback {
                    handler.post(Runnable {
                        try {
                            val encryptedMsg = it.text
                            try {
                                val qRmsg = QRMessageData.getQRMessageObject(encryptedMsg)
                                Log.i("qRMessageData", qRmsg.toString())
                                val bundle = bundleOf("qrData" to qRmsg.toString(),"userId" to LauncherScreenFragment.studentEnrolment)
//                                findNavController().navigate(R.id.action_scannerFragment_to_attendanceInfoFragment, bundle)
                            }catch (e: JSONException){
                                BasicUtils.errorDialogBox(requireContext(),"QR Scan Error","Retry to scan QR code again")
                                Log.e(TAG, "onViewCreated: JsonException:$encryptedMsg", e)
                            }
                        }
                        catch (e:Exception){
                            BasicUtils.errorDialogBox(requireContext(),"Error","Error:${e}")
                            Log.e(TAG, "onViewCreated: Exception while generating QRMessage object:${e.message}", e)
                        }

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

