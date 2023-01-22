package com.example.guniattendance.authorization.authfragments.ui.apppin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentAppPinBinding
import com.example.guniattendance.student.StudentActivity
import com.example.guniattendance.utils.EventObserver
import com.example.guniattendance.utils.hideKeyboard
import com.example.guniattendance.utils.showProgress
import com.example.guniattendance.utils.snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AppPinFragment : Fragment(R.layout.fragment_app_pin) {

    private lateinit var binding: FragmentAppPinBinding
    private lateinit var viewModel: AppPinViewModel
    private val args: AppPinFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppPinViewModel::class.java]
        subscribeToObserve()

        binding = FragmentAppPinBinding.bind(view)

        binding.apply {
            fabLogin.setOnClickListener {
                hideKeyboard(requireActivity())
                viewModel.login(pinView.text.toString())
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
        ) {
            showProgress(
                activity = requireActivity(),
                bool = false,
                parentLayout = binding.parentLayout,
                loading = binding.lottieAnimation
            )
            binding.apply {
                pinView.setText("")
            }
            snackbar("Logged in successfully!!")
            if (args.role == "student") {
                Intent(requireActivity(), StudentActivity::class.java).also { intent ->
                    startActivity(intent)
                    requireActivity().finish()
                }
            } else {
                Firebase.auth.signOut()
                snackbar("No user found with entered email address")
            }
        })

    }

}