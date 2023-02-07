package com.example.guniattendance.moodle

import android.content.Context
import androidx.preference.PreferenceManager
import com.uvpce.attendance_moodle_api_library.repo.ModelRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MoodleConfig {

    companion object{
        var url = "http://202.131.126.214"
        private lateinit var reslt: ModelRepository
        fun getModelRepo(context: Context):ModelRepository {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            url = sharedPreferences.getString("ServerList", "202.131.126.214")!!

            MainScope().launch {
                reslt = ModelRepository.getModelRepo(context)
            }
             return reslt
        }
    }

}