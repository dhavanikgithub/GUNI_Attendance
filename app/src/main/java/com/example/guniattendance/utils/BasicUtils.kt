package com.example.guniattendance.utils

import android.app.AlertDialog
import android.content.Context

class BasicUtils {
    companion object{

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