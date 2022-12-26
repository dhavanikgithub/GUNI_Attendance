package com.example.guniattendance.student.studentfragments.ui.studenthome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guniattendance.data.entity.Attendance
import com.example.guniattendance.data.entity.Faculty
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.student.repository.StudentRepository
import com.example.guniattendance.utils.Events
import com.example.guniattendance.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val repository: StudentRepository
) : ViewModel() {

    private val _userStatus = MutableLiveData<Events<Resource<Student>>>()
    val userStatus: LiveData<Events<Resource<Student>>> = _userStatus

    fun getUser() {
        _userStatus.postValue(Events(Resource.Loading()))

        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getCurUser()
            _userStatus.postValue(Events(user))
        }

    }

    private val _attendanceStatus = MutableLiveData<Events<Resource<Attendance>>>()
    val attendanceStatus: LiveData<Events<Resource<Attendance>>> = _attendanceStatus

    fun getActiveAttendance() {
        _attendanceStatus.postValue(Events(Resource.Loading()))

        viewModelScope.launch(Dispatchers.IO) {
            val attendance = repository.getActiveAttendance()
            _attendanceStatus.postValue(Events(attendance))
        }
    }

    private val _attendanceStatusForOther =
        MutableLiveData<Events<Resource<Pair<String, Attendance>>>>()
    val attendanceStatusForOther: LiveData<Events<Resource<Pair<String, Attendance>>>> =
        _attendanceStatusForOther

    fun getActiveAttendanceForOther(enrolment: String) {

        val error = if (enrolment.isEmpty()) {
            "emptyEnrol"
        } else if (enrolment.length != 11) {
            "enrol"
        } else {
            null
        }

        error?.let {
            _attendanceStatusForOther.postValue(Events(Resource.Error(error)))
            return
        }

        _attendanceStatusForOther.postValue(Events(Resource.Loading()))

        viewModelScope.launch(Dispatchers.IO) {
            val attendance = repository.getActiveAttendance()
            val students = repository.getStudentByEnrol(enrolment)
            if (students.data!!.isEmpty()) {
                _attendanceStatusForOther.postValue(Events(Resource.Error("Please enter correct enrolment number")))
            } else {
                _attendanceStatusForOther.postValue(
                    Events(
                        Resource.Success(
                            Pair(
                                students.data[0].uid,
                                attendance.data!!
                            )
                        )
                    )
                )
            }
        }
    }

    fun removeObservers() {
        _attendanceStatus.value = null
        _attendanceStatus.postValue(null)
        _attendanceStatusForOther.value = null
        _attendanceStatusForOther.postValue(null)
        _userStatus.value = null
        _userStatus.postValue(null)
    }

}