package com.example.guniattendance.student.studentfragments.ui.takeattendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.student.repository.StudentRepository
import com.example.guniattendance.utils.Events
import com.example.guniattendance.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TakeAttendanceViewModel @Inject constructor(
    private val repository: StudentRepository
) : ViewModel() {

    private val _attendanceStatus = MutableLiveData<Events<Resource<String>>>()
    val attendanceStatus: LiveData<Events<Resource<String>>> = _attendanceStatus

    fun addAttendance(uid: String, name: String) {
        _attendanceStatus.postValue(Events(Resource.Loading()))

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.addStudentToAttendance(uid, name)
            _attendanceStatus.postValue(Events(result))
        }
    }

    private val _studentListStatus = MutableLiveData<Events<Resource<Student>>>()
    val studentListStatus: LiveData<Events<Resource<Student>>> = _studentListStatus

    fun getStudent(uid: String) {
        _studentListStatus.postValue(Events(Resource.Loading()))

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getStudent(uid)
            _studentListStatus.postValue(Events(result))
        }
    }

    fun removeObservers() {
        _attendanceStatus.value = null
        _attendanceStatus.postValue(null)
        _studentListStatus.value = null
        _studentListStatus.postValue(null)
    }

}