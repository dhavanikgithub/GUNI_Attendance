package com.example.guniattendance.utils.leave

data class LeaveListData(
    val id:String,
    val studentId:String,
    val studentName:String,
    val proctorId:String,
    val type:String,
    val startDate:String,
    val endDate:String,
    val reason:String,
    val attachment:String,
    var status:String,
    val requestDate:String,
    val requestTime:String
)
