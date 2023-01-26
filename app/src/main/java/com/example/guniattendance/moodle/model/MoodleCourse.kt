package com.example.guniattendance.moodle.model

class MoodleCourse(val id:String,val Name:String,val userName:String) {
    val groupList:ArrayList<MoodleGroup> = ArrayList();
    val sessionList:ArrayList<MoodleSession> = ArrayList();
    override fun toString(): String {
        return "id:$id :: name=$Name :: userName=$userName"
    }
}