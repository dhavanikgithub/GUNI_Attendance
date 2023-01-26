package com.example.guniattendance.moodle

import android.content.Context
import com.uvpce.attendance_moodle_api_library.repo.ModelRepository

class MoodleConfig {
    companion object{
        var Url = "http://202.131.126.214"
        fun getModelRepo(context: Context):ModelRepository {return ModelRepository(context,Url)}
    }
}