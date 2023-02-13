package com.example.guniattendance.utils

import android.util.Log

inline fun <T> safeCall(action:() -> Resource<T>): Resource<T> {
    return try{
        action()
    } catch(e: Exception) {
        Log.e("safeCall", "This is the Error : ${e}")
        Resource.Error(e.message ?: "An unknown Error occurred")
    }
}