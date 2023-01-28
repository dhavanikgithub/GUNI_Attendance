package com.example.guniattendance.utils

import android.util.Log

inline fun <T> safeCall(action:() -> Resource<T>): Resource<T> {
    return try{
        action()
    } catch(e: Exception) {
        Log.i("ERROR", "This is the Error : ${e.message}")
        Resource.Error(e.message ?: "An unknown Error occurred")
    }
}