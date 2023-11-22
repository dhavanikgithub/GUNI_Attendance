package com.example.guniattendance.student.studentfragments.ui.leave.leavehome

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLeaveHomeBinding

class LeaveHomeFragment : Fragment() {

    companion object {
        fun newInstance() = LeaveHomeFragment()
    }
    private lateinit var binding:FragmentLeaveHomeBinding
    private lateinit var viewModel: LeaveHomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leave_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= FragmentLeaveHomeBinding.bind(view)
        binding.apply {
            btnLeaveLogout.setOnClickListener {
                // Obtain a reference to SharedPreferences
                val sharedPreferences = requireActivity().getSharedPreferences("student", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                findNavController().popBackStack()
                findNavController().navigate(R.id.leaveLoginFragment)
            }
            btnApplyForLeave.setOnClickListener {
                findNavController().navigate(R.id.leaveRequestFragment)
            }

            btnLeaveStatus.setOnClickListener {
                findNavController().navigate(R.id.leaveStatusFragment)
            }
        }
    }

}