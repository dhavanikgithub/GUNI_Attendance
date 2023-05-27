package com.example.guniattendance.utils

import android.app.AlertDialog
import android.content.Context
import com.guni.uvpce.moodleapplibrary.model.QRMessageData
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.*

class BasicUtils {

    companion object{

        fun convertDateToString(date: LocalDate): String {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            return date.format(formatter)
        }
        fun getDaysBetween(startDate: LocalDate, endDate: LocalDate): Long {
            return ChronoUnit.DAYS.between(startDate, endDate)
        }
        fun getDaysBetween(startDate: String, endDate: String): Long {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

            try {
                val start = LocalDate.parse(startDate, formatter)
                val end = LocalDate.parse(endDate, formatter)
                return ChronoUnit.DAYS.between(start, end)
            } catch (e: DateTimeParseException) {
                return -1
            }

        }

        fun validateDateRange(startDate: LocalDate, endDate: LocalDate): Boolean {
            return !endDate.isBefore(startDate)
        }

        fun validateDateRange(startDate: String, endDate: String): Boolean {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

            try {
                val start = LocalDate.parse(startDate, formatter)
                val end = LocalDate.parse(endDate, formatter)
                return !end.isBefore(start)
            } catch (e: DateTimeParseException) {
                return false
            }
        }

        fun isStringJSONArray(stringData:String):Boolean
        {
            return try{
                JSONArray(stringData)
                true
            } catch (ex:Exception) {
                false
            }
        }

        fun isStringJSONObject(stringData:String):Boolean
        {
            return try{
                JSONObject(stringData)
                true
            } catch (ex:Exception) {
                false
            }
        }
        fun getQRText(data: QRMessageData):String{
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val simpleTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return "Course: ${data.courseName} (" +
                    "Group: ${data.groupName})\n\n" +
                    "Session Date:${simpleDateFormat.format(data.sessionStartDate*1000)}\n" +
                    " From:${simpleTimeFormat.format(data.sessionStartDate*1000)}" +
                    " To:${simpleTimeFormat.format(data.sessionEndDate*1000)}\n\n"+
                    "Attendance Date:${simpleDateFormat.format(data.attendanceStartDate*1000)}\n" +
                    " From:${simpleTimeFormat.format(data.attendanceStartDate*1000)}"+
                    " To:${simpleTimeFormat.format(data.attendanceEndDate*1000)}\n" +
                    "Duration: ${data.attendanceDuration}Minutes"
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