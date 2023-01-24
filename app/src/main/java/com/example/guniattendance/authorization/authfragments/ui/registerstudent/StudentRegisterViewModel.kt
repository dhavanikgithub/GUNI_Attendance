package com.example.guniattendance.authorization.authfragments.ui.registerstudent

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guniattendance.authorization.repository.AuthRepository
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.utils.BitmapUtils
import com.example.guniattendance.utils.Events
import com.example.guniattendance.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StudentRegisterViewModel @Inject constructor(
    private val context: Context,
    private val repository: AuthRepository
) : ViewModel() {

    private val students = FirebaseFirestore.getInstance().collection("student")

    private val _registerStatus = MutableLiveData<Events<Resource<Student>>>()
    val registerStatus: LiveData<Events<Resource<Student>>> = _registerStatus

    private val _curImageUri = MutableLiveData<Uri>()
    val curImageUri: LiveData<Uri> = _curImageUri

    fun setCurrentImageUri(uri: Uri) {
        _curImageUri.postValue(uri)
    }

    fun register(
        enrolment: String,
        name: String,
        email: String,
    ) {

        val error = if (email.isEmpty()) {
            "emptyEmail"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "email"
//        } else if (phone.isEmpty()) {
//            "emptyPhone"
//        } else if (!Patterns.PHONE.matcher(phone).matches()) {
//            "phone"
//        } else if (branch.isEmpty()) {
//            "emptyBranch"
//        } else if (sem == 0) {
//            "sem"
        } else if (name.isEmpty()) {
            "name"
//        } else if (pin.length != 6) {
//            "pin"
        } else if (curImageUri.value == Uri.EMPTY || curImageUri.value == null) {
            "uri"
        } else if (enrolment.isEmpty()) {
            "emptyEnrolment"
        } else if (enrolment.length != 11) {
            "enrolment"
//        } else if (lec.isEmpty()) {
//            "emptyLec"
//        } else if (lab.isEmpty()) {
//            "emptyLab"
        } else null

        error?.let {
            _registerStatus.postValue(Events(Resource.Error(error)))
            return
        }

        _registerStatus.postValue(Events(Resource.Loading()))

        val bitmap = BitmapUtils.getBitmapFromUri(context.contentResolver, curImageUri.value!!)

        viewModelScope.launch(Dispatchers.Main) {

            val student = students.whereEqualTo("enrolment", enrolment).get().await()
                .toObjects(Student::class.java)

            if (student.isNotEmpty()) {
                _registerStatus.postValue(
                    Events(
                        Resource.Error("User already exists with this enrolment number")
                    )
                )
            } else {
                val result = repository.registerStudent(
                    enrolment = enrolment,
                    name = name,
                    email = email,
//                    phone = phone,
//                    branch = branch,
//                    sem = sem,
//                    pin = pin,
                    bitmap = bitmap,
                    profilePicUri = curImageUri.value!!,
//                    lec = lec,
//                    lab = lab
                )
                _registerStatus.postValue(Events(result))
            }
        }

    }

}