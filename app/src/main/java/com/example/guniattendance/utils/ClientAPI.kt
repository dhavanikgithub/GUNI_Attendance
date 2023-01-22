package com.example.guniattendance.utils

import android.content.Context
class ClientAPI(context: Context) {
    var url : String
    protected val attandanceToken = "697859a828111d63c3f68543ac986827"
    protected val coreToken = "4f3c9f8f0404a7db50825391c295937e"

    init {
        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val defaultUrl = "http://202.131.126.214/webservice/rest/server.php"
        url = sharedPref.getString("moodle_url", defaultUrl) ?: defaultUrl
    }
}
