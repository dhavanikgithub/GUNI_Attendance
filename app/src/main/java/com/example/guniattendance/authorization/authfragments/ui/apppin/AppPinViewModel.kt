package com.example.guniattendance.authorization.authfragments.ui.apppin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guniattendance.authorization.repository.AuthRepository
import com.example.guniattendance.utils.Events
import com.example.guniattendance.utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppPinViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginStatus = MutableLiveData<Events<Resource<AuthResult>>>()
    val loginStatus: LiveData<Events<Resource<AuthResult>>> = _loginStatus

    fun login(pin: String) {
        val error = if (pin.length != 6) {
            "pin"
        } else null

        error?.let {
            _loginStatus.postValue(Events(Resource.Error(error)))
            return
        }

        _loginStatus.postValue(Events(Resource.Loading()))

        viewModelScope.launch(Dispatchers.IO) {
            val email = Firebase.auth.currentUser?.email!!
            val result = repository.login(email, pin)
            _loginStatus.postValue(Events(result))
        }

    }


}