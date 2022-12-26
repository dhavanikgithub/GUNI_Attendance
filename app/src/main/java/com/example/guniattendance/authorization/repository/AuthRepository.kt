package com.example.guniattendance.authorization.repository

import android.graphics.Bitmap
import android.net.Uri
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.utils.Resource
import com.google.firebase.auth.AuthResult

interface AuthRepository {

    suspend fun registerStudent(
        enrolment: String,
        name: String,
        email: String,
        phone: String,
        branch: String,
        sem: Int,
        pin: String,
        lec: String,
        lab: String,
        bitmap: Bitmap,
        profilePicUri: Uri
    ): Resource<Student>

    suspend fun login(
        email: String,
        pin: String
    ): Resource<AuthResult>

    suspend fun loginUser(
        email: String,
        pin: String
    ): Resource<String>

}