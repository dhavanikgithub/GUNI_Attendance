package com.example.guniattendance.authorization

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.guniattendance.R
import com.example.guniattendance.utils.LiveNetworkMonitor
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        snackbar=Snackbar.make(
            findViewById(android.R.id.content),
            "",
            Snackbar.LENGTH_INDEFINITE
        )
        val connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val liveNetworkMonitor = LiveNetworkMonitor(connectivityManager)
        liveNetworkMonitor.observe(this) { showNetworkMessage(it) }

    }

    private fun showNetworkMessage(isConnected:Boolean)
    {
        if(!isConnected)
        {
            snackbar=Snackbar.make(
                findViewById(android.R.id.content),
                "No internet connection",
                Snackbar.LENGTH_LONG
            )
            snackbar.view.setBackgroundColor(Color.parseColor("#FF3939"))
            snackbar.setTextColor(Color.WHITE)
            snackbar.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            snackbar.show()
        }
        else{
            snackbar.dismiss()
        }
    }
}