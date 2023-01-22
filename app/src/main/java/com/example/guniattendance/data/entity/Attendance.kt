package com.example.guniattendance.data.entity

data class Attendance(
    val uid: String = "",
    val className: String = "",
    val sem: Int = 0,
    val enabled: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val subject: String = "",
    val students: ArrayList<String> = arrayListOf(),
    val type: String = "",
    val facultyUid: String = "",
    val date: String = ""
)
