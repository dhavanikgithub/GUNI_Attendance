package com.example.guniattendance.student.studentfragments.ui.attendancesuccess

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentAttendanceSuccessBinding
import com.example.guniattendance.utils.snackbar

class AttendanceSuccessFragment : Fragment(R.layout.fragment_attendance_success) {

    companion object {
        fun newInstance() = AttendanceSuccessFragment()
    }

    private lateinit var viewModel: AttendanceSuccessViewModel
    private lateinit var binding: FragmentAttendanceSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attendance_success, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AttendanceSuccessViewModel::class.java]

        binding = FragmentAttendanceSuccessBinding.bind(view)
        val mediaPlayer = MediaPlayer.create(requireContext(),R.raw.attendance_success_sound)
        try{
            val callback = object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(R.id.studentHomeFragment,false)
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(callback)
        }
        catch (ex:Exception)
        {
            snackbar((ex.message).toString())
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.animAttendanceDone.visibility=View.VISIBLE
            binding.animAttendanceDone.playAnimation()
            mediaPlayer.start()
            binding.tvSucess.text="Your Attendance Marked Successfully"
        },300)
    }

}