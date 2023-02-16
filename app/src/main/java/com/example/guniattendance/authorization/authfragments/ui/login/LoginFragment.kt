package com.example.guniattendance.authorization.authfragments.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.guniattendance.R
import com.example.guniattendance.databinding.FragmentLoginBinding
import com.example.guniattendance.utils.*
import com.guni.uvpce.moodleapplibrary.repo.ClientAPI

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

        binding = FragmentLoginBinding.bind(view)

        binding.apply {
            btnLogin.setOnClickListener {
                hideKeyboard(requireActivity())
                var recievedMoodleUsername = moodleusernameText.text
                var recievedmoodlePassword = moodlepasswordText.text

                var checkValidInfo = "http://202.131.126.214/login/token.php?username=${recievedMoodleUsername}&password=${recievedmoodlePassword}&service=moodle_mobile_app"
                var res = viewModel.sentHttpRequest(checkValidInfo)
                if(res == true){
                    //Go to registration page:
                    findNavController().navigate(
                    LoginFragmentDirections
                        .actionLoginFragmentToStudentRegisterFragment())
                } else {
                    //Wrong Username or Password:
                    context?.let { it1 -> ClientAPI.showErrorBox(it1, "Error", "Incorrect Username or Password", "OK") }
                }
            }
        }

    }

}