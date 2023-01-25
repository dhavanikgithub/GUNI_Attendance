package com.example.guniattendance.moodle.model

import java.text.SimpleDateFormat

class MoodleSession(val course: MoodleCourse,val id:String,val attendanceid:String,val groupid:String,val sessdate:String,val duration:String) {
    val durationMinutes= duration.toLong() / 60;
    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")
    val sessDate=simpleDateFormat.format(sessdate.trim().toLong()*1000)
    val sessend=(sessdate.toLong() + duration.toLong())
    val sessEnd=simpleDateFormat.format(sessend.toLong()*1000)

    override fun toString(): String {
        return "\nid=$id \nattendanceid=$attendanceid \ngroupid=$groupid \nsessdate=$sessDate \nsessend=$sessEnd \nduration=$durationMinutes mins\n"
    }
}