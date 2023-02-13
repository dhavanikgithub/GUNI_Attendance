package com.example.guniattendance.authorization.authfragments.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guniattendance.authorization.repository.AuthRepository
import com.example.guniattendance.utils.Events
import com.example.guniattendance.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginStatus = MutableLiveData<Events<Resource<String>>>()
    val loginStatus: LiveData<Events<Resource<String>>> = _loginStatus

    fun login(email: String, pin: String) {
        val error = if (email.isEmpty()) {
            "emptyEmail"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "email"
        } else if (pin.length != 6) {
            "pin"
        } else null

        error?.let {
            _loginStatus.postValue(Events(Resource.Error(error)))
            return
        }

        _loginStatus.postValue(Events(Resource.Loading()))

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.loginUser(email, pin)
            _loginStatus.postValue(Events(result))
        }

    }

    fun sentHttpRequest(url: String): Boolean{
        val url = URL(url)
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "POST"
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        con.doOutput = true
        con.doInput = true
        val wr = DataOutputStream(con.outputStream)
        wr.flush()
        wr.close()
        val `is`: InputStream = con.inputStream
        val rd = BufferedReader(InputStreamReader(`is`))
        var line: String?
        val response = StringBuilder()
        while (rd.readLine().also { line = it } != null) {
            response.append(line)
            response.append('\r')
        }
        rd.close()
        val res = response.toString()
        if(res.indexOf("token") != -1){
            return true
        }

        return false
    }

}