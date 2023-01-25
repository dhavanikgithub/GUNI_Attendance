package com.example.guniattendance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

class ScannerActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView : CodeScannerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        scannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

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
                    Toast.makeText(this@ScannerActivity,it.text,Toast.LENGTH_SHORT).show()
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