package com.example.guniattendance

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.example.guniattendance.student.studentfragments.ui.studenthome.StudentHomeFragmentDirections

class ScannerActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView : CodeScannerView
    private lateinit var FLayout : FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)


        scannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        FLayout = findViewById(R.id.FLayout)
        initScanner()
    }

    private fun initScanner(){
        codeScanner.apply{
            codeScanner.camera = CodeScanner.CAMERA_BACK
            codeScanner.autoFocusMode = AutoFocusMode.SAFE
            codeScanner.formats = CodeScanner.ALL_FORMATS
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isTouchFocusEnabled = false

            codeScanner.decodeCallback = DecodeCallback {
                runOnUiThread {
                    //Code to Next Activity i.e. StudentHome Should be here
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.FLayout) as NavHostFragment
                    Toast.makeText(this@ScannerActivity,it.text,Toast.LENGTH_SHORT).show()
                    FLayout.visibility = View.VISIBLE
                    navHostFragment.findNavController().navigate(
                        StudentHomeFragmentDirections
                            .actionStudentHomeFragmentToTakeAttendanceFragment("123","123")
                    )
                }
            }

            codeScanner.errorCallback = ErrorCallback {
                runOnUiThread {
                    //Handle Errors Here
                }
            }

        }
        scannerView.setOnClickListener {
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
}