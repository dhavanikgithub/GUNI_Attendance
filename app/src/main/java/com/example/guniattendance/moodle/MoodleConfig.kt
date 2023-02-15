package com.example.guniattendance.moodle

import android.content.Context
import androidx.preference.PreferenceManager
import com.uvpce.attendance_moodle_api_library.repo.ModelRepository

class MoodleConfig {
    companion object{

        suspend fun getModelRepo(context: Context):ModelRepository {
            return ModelRepository.getModelRepo(context)
        }
        suspend fun refreshModelRepo(context: Context){
            ModelRepository.getModelRepo(context)
        }
    }

}