package com.example.guniattendance.authorization.authfragments.ui.launcherscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLauncherScreenBinding


class LauncherScreenFragment : Fragment(R.layout.fragment_launcher_screen) {

    private lateinit var binding: FragmentLauncherScreenBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLauncherScreenBinding.bind(view)

        binding.apply {

            btnRegisterStudent.setOnClickListener {
                findNavController().navigate(
                    LauncherScreenFragmentDirections
                        .actionLauncherScreenFragmentToStudentRegisterFragment()
                )
            }

            btnLogin.setOnClickListener {
                findNavController().navigate(
                    LauncherScreenFragmentDirections
                        .actionLauncherScreenFragmentToLoginFragment()
                )
            }

        }

    }
}