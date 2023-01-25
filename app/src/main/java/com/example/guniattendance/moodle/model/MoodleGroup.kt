package com.example.guniattendance.moodle.model

class MoodleGroup(val course:MoodleCourse,val groupid:String,val groupName:String) {

    override fun toString(): String {
        return "\ngroupid=$groupid \ngroupname=$groupName"
    }
}