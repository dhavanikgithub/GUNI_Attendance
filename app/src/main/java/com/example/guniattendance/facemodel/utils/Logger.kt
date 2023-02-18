package com.example.guniattendance.facemodel.utils

import com.example.guniattendance.student.studentfragments.ui.takeattendance.TakeAttendanceFragment

// Logs message using log_textview present in activity_main.xml
class Logger {

    companion object {

        fun log( message : String ) {
            TakeAttendanceFragment.setMessage(  TakeAttendanceFragment.logTextView.text.toString() + "\n" + ">> $message" )
            // To scroll to the last message
            // See this SO answer -> https://stackoverflow.com/a/37806544/10878733
            while ( TakeAttendanceFragment.logTextView.canScrollVertically(1) ) {
                TakeAttendanceFragment.logTextView.scrollBy(0, 10)
            }
        }

    }

}