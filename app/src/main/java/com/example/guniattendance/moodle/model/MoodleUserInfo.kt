package com.example.guniattendance.moodle.model

import android.graphics.Bitmap

open class BaseUserInfo(var id: String,
                        var username: String,
                        var firstname: String,
                        var lastname: String,
                        var fullname:String,
                        var emailAddress:String,
                        var imageUrl:String){
    //var image: Bitmap = Utility().convertBase64StringToImage(Utility().convertUrlToBase64(imageUrl))
    override fun toString(): String {
        return "\n id=$id \n username=$username \n firstname=$firstname \n lastname=$lastname \n fullname=$fullname \nemailAddress:$emailAddress \n imageURL=$imageUrl"
    }

}


class MoodleUserInfo(var course: MoodleCourse,var group:MoodleGroup,
                     id: String, username: String,
                     firstname: String, lastname: String,
                     fullName:String,emailAddress:String, imageUrl:String):
    BaseUserInfo(id, username,firstname, lastname, fullName,emailAddress, imageUrl){


    override fun toString(): String {
        return super.toString() +"\n"+course.Name+"\n"+group.groupName
    }
}