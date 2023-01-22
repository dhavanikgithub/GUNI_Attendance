package com.example.guniattendance.student.repository

import com.example.guniattendance.data.entity.Attendance
import com.example.guniattendance.data.entity.Student
import com.example.guniattendance.utils.Resource
import com.example.guniattendance.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DefaultStudentRepository : StudentRepository {

    private val auth = FirebaseAuth.getInstance()
    private val students = FirebaseFirestore.getInstance().collection("student")

    private val cal = Calendar.getInstance()
    private val df: DateFormat = SimpleDateFormat("dd-MM-yyyy")
    private val date = df.format(cal.time)

    override suspend fun getCurUser(): Resource<Student> = withContext(Dispatchers.IO) {
        safeCall {
            val curUser = students.document(auth.currentUser!!.uid).get().await()
                .toObject(Student::class.java)!!
            Resource.Success(curUser)
        }
    }

    override suspend fun getActiveAttendance(): Resource<Attendance> = withContext(Dispatchers.IO) {
        safeCall {

            val curUser = getCurUser().data!!

            val attendances =
                FirebaseFirestore.getInstance().collection("attendance").document("date")
                    .collection(date.toString())
                    .document("semester")
                    .collection(curUser.sem.toString())

            val attendance = Attendance()

            var tempAttendance = if (curUser.branch == "AI") {
                attendances
                    .whereEqualTo("sem", curUser.sem)
                    .whereEqualTo("subject", "ML Ops")
                    .whereEqualTo("enabled", true)
                    .get().await().toObjects(Attendance::class.java)
            } else {
                arrayListOf()
            }

            if (tempAttendance.isEmpty()) {
                tempAttendance = attendances
                    .whereEqualTo("sem", curUser.sem)
                    .whereEqualTo("className", curUser.lecture)
                    .whereEqualTo("enabled", true)
                    .get().await().toObjects(Attendance::class.java)
            }

            if (tempAttendance.isEmpty()) {
                tempAttendance = attendances
                    .whereEqualTo("sem", curUser.sem)
                    .whereEqualTo("className", curUser.lab)
                    .whereEqualTo("enabled", true)
                    .get().await().toObjects(Attendance::class.java)
            }

            if (tempAttendance.isEmpty()) {
                Resource.Success(attendance)
            } else {
                Resource.Success(tempAttendance[0])
            }
        }
    }

    override suspend fun addStudentToAttendance(uid: String, name: String): Resource<String> =
        withContext(Dispatchers.IO) {
            safeCall {

                val curUser = getCurUser().data!!

                val attendances =
                    FirebaseFirestore.getInstance().collection("attendance").document("date")
                        .collection(
                            date.toString()
                        ).document("semester")
                        .collection(curUser.sem.toString())

                attendances.document(uid).update(
                    "students", FieldValue.arrayUnion(name)
                ).await()
                Resource.Success(name)
            }
        }

    override suspend fun getStudent(uid: String): Resource<Student> =
        withContext(Dispatchers.IO) {
            safeCall {
                val student = students.document(uid).get().await()
                    .toObject(Student::class.java)
                Resource.Success(student!!)
            }
        }

    override suspend fun getStudentByEnrol(enrolment: String): Resource<List<Student>> =
        withContext(Dispatchers.IO) {
            safeCall {
                val student = students.whereEqualTo("enrolment", enrolment).get().await()
                    .toObjects(Student::class.java)
                Resource.Success(student)
            }
        }

}