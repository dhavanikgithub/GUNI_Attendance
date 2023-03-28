package com.example.guniattendance.student.studentfragments.ui.attendancesuccess

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentAttendanceSuccessBinding

class AttendanceSuccessFragment : Fragment(R.layout.fragment_attendance_success) {

    private lateinit var viewModel: AttendanceSuccessViewModel
    private lateinit var binding: FragmentAttendanceSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attendance_success, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AttendanceSuccessViewModel::class.java]

        binding = FragmentAttendanceSuccessBinding.bind(view)
        val mediaPlayer = MediaPlayer.create(requireContext(),R.raw.attendance_success_sound)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.animAttendanceDone.visibility=View.VISIBLE
            binding.animAttendanceDone.playAnimation()
            mediaPlayer.start()
            binding.tvSucess.text = resources.getString(R.string.success_attendance_message)
        },300)
    }

}