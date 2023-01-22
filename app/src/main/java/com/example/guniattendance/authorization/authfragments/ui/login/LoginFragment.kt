package com.example.guniattendance.authorization.authfragments.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLoginBinding
import com.example.guniattendance.student.StudentActivity
import com.example.guniattendance.utils.EventObserver
import com.example.guniattendance.utils.hideKeyboard
import com.example.guniattendance.utils.showProgress
import com.example.guniattendance.utils.snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        subscribeToObserve()

        binding = FragmentLoginBinding.bind(view)

        binding.apply {
            btnLogin.setOnClickListener {
                hideKeyboard(requireActivity())
                viewModel.login(etEmail.text?.trim().toString(), pinView.text.toString())
            }
        }

    }

    private fun subscribeToObserve() {
        viewModel.loginStatus.observe(viewLifecycleOwner, EventObserver(
            onError = { error ->
                showProgress(
                    activity = requireActivity(),
                    bool = false,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
                when (error) {
                    "emptyEmail" -> {
                        binding.etEmail.error = "Email cannot be empty"
                    }
                    "email" -> {
                        binding.etEmail.error = "Enter a valid email"
                    }
                    "pin" -> {
                        snackbar("Pin should be of 6 length")
                    }
                    else -> snackbar(error)
                }
            },
            onLoading = {
                showProgress(
                    activity = requireActivity(),
                    bool = true,
                    parentLayout = binding.parentLayout,
                    loading = binding.lottieAnimation
                )
            }
        ) { role ->
            showProgress(
                activity = requireActivity(),
                bool = false,
                parentLayout = binding.parentLayout,
                loading = binding.lottieAnimation
            )
            binding.apply {
                etEmail.setText("")
                pinView.setText("")
            }
            snackbar("Logged in successfully!!")
            if (role == "student") {
                Intent(requireActivity(), StudentActivity::class.java).also { intent ->
                    startActivity(intent)
                    requireActivity().finish()
                }
            } else {
                FirebaseAuth.getInstance().signOut()
                snackbar("No user found with this email address")
            }
        })
    }

}