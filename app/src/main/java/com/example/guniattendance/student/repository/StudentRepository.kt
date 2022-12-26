package com.example.guniattendance.student.repository

import com.example.guniattendance.data.entity.Attendance
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.utils.Resource

interface StudentRepository {

    suspend fun getCurUser(): Resource<Student>

    suspend fun getActiveAttendance(): Resource<Attendance>

    suspend fun addStudentToAttendance(uid: String, name: String): Resource<String>

    suspend fun getStudent(uid: String): Resource<Student>

    suspend fun getStudentByEnrol(enrolment: String): Resource<List<Student>>

}