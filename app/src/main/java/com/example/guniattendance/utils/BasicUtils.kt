package com.example.guniattendance.utils

import android.app.AlertDialog
import android.content.Context
import com.guni.uvpce.moodleapplibrary.model.QRMessageData
import java.text.SimpleDateFormat

class BasicUtils {

    companion object{
        fun getQRText(data: QRMessageData):String{
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
            val simpleTimeFormat = SimpleDateFormat("hh:mm a")
            return "Course: ${data.courseName} (" +
                    "Group: ${data.groupName})\n\n" +
                    "Session Date:${simpleDateFormat.format(data.sessionStartDate*1000)}\n" +
                    " From:${simpleTimeFormat.format(data.sessionStartDate*1000)}" +
                    " To:${simpleTimeFormat.format(data.sessionEndDate*1000)}\n\n"+
                    "Attendance Date:${simpleDateFormat.format(data.attendanceStartDate*1000)}\n" +
                    " From:${simpleTimeFormat.format(data.attendanceStartDate*1000)}"+
                    " To:${simpleTimeFormat.format(data.attendanceEndDate*1000)}\n" +
                    "Duration: ${data.attendanceDuration}mins"
        }

        fun errorDialogBox(
            context: Context,
            title: String,
            message: String
        ){
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle(title)
            alertDialog.setMessage(message)
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

            alertDialog.create().show()
        }
    }
}