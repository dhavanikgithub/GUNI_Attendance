package com.example.guniattendance.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

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
            alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener {
                    dialog, _ -> dialog.dismiss()
            })

            alertDialog.create().show()
        }
    }
}