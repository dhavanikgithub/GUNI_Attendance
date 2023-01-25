package com.example.guniattendance.moodle

import android.app.AlertDialog
import android.content.Context

class ClientAPI {
    companion object{
        val attandanceToken = "697859a828111d63c3f68543ac986827"
        val coreToken = "4f3c9f8f0404a7db50825391c295937e"
        val uploadToken = "8d29dd97dd7c93b0e3cdd43d4b797c87"
        var Url = "http://202.131.126.214"
        var defaultUrl = "http://202.131.126.214/webservice/rest/server.php"
        val userDefaultPicURL =
            "http://202.131.126.214/webservice/pluginfile.php/89/user/icon/f1?token=4f3c9f8f0404a7db50825391c295937e"
        fun showErrorBox(context: Context, title: String, msg: String, negB:String = "", posB:String = "", cancelable: Boolean = true){
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder
                .setMessage(msg)
                .setCancelable(cancelable)
                .setNegativeButton(negB) { dialog, id ->
                    dialog.cancel()
                }
                .setPositiveButton(posB) { dialog, id ->
                    dialog.cancel()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

}