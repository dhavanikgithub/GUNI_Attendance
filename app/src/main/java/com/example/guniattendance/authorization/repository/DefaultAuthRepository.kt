package com.example.guniattendance.authorization.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import com.example.guniattendance.data.entity.Faculty
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.utils.Constants
import com.example.guniattendance.utils.Resource
import com.example.guniattendance.utils.safeCall
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class DefaultAuthRepository : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val faculties = FirebaseFirestore.getInstance().collection("faculty")
    private val students = FirebaseFirestore.getInstance().collection("student")

    override suspend fun registerStudent(
        enrolment: String,
        name: String,
        email: String,
//        phone: String,
//        branch: String,
//        sem: Int,
//        pin: String,
//        lec: String,
//        lab: String,
        bitmap: Bitmap,
        profilePicUri: Uri
    ): Resource<Student> = withContext(Dispatchers.IO) {
        safeCall {
            val result = auth.createUserWithEmailAndPassword(email, "123").await()
            auth.currentUser?.updateProfile(
                userProfileChangeRequest {
                    displayName = name
                    photoUri = profilePicUri
                }
            )

            val baos = ByteArrayOutputStream()

            val resizedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                Constants.DST_WIDTH,
                Constants.DST_HEIGHT, true
            )

            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val byteArray: String =
                Base64.encodeToString(
                    baos.toByteArray(),
                    Base64.DEFAULT
                )

            val student = Student(
//                phone = phone,
                email = email,
                uid = result.user!!.uid,
                byteArray = byteArray,
//                branch = branch,
//                sem = sem,
                enrolment = enrolment,
//                lecture = lec,
//                lab = lab,
                name = name
            )

            students.document(result.user!!.uid).set(student).await()
            Resource.Success(student)
        }
    }

    override suspend fun login(email: String, pin: String): Resource<AuthResult> =
        withContext(Dispatchers.IO) {
            safeCall {
                val result = auth.signInWithEmailAndPassword(email, pin).await()
                Resource.Success(result)
            }
        }

    override suspend fun loginUser(email: String, pin: String): Resource<String> =
        withContext(Dispatchers.IO) {
            safeCall {
                val result = auth.signInWithEmailAndPassword(email, pin).await()
                val uid = result.user!!.uid
                val faculty = faculties.document(uid).get().await().toObject(Faculty::class.java)
                if (faculty == null) {
                    Resource.Success("student")
                } else {
                    Resource.Success("faculty")
                }
            }
        }
}