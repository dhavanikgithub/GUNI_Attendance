package com.example.guniattendance.moodle

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.guniattendance.GUNIAttendanceSystem
import com.uvpce.attendance_moodle_api_library.repo.ModelRepository

class MoodleConfig {

    companion object{
        var url = "http://202.131.126.214"
        fun getModelRepo(context: Context):ModelRepository {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            url = sharedPreferences.getString("ServerList", "202.131.126.214")!!
            return ModelRepository(context,"http://$url")
        }
    }

}