package com.example.guniattendance.student.studentfragments.ui.scanner

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.bumptech.glide.manager.SupportRequestManagerFragment
import com.example.guniattendance.R
import com.example.guniattendance.student.studentfragments.ui.studenthome.StudentHomeFragmentDirections

class ScannerViewModel : ViewModel() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView : CodeScannerView

}