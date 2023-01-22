package com.example.guniattendance.authorization

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.guniattendance.R
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val file1 = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/facenet.tflite")
        val file2 = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/mask_detector.tflite")
        if (!file1.exists()) {
            startDownload("https://raw.githubusercontent.com/ParthBhuva97/assets/main/facenet.tflite","facenet.tflite")
            val currentFile = File("/storage/emulated/0/Download/facenet.tflite")
            val newFile = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/facenet.tflite")
            currentFile.renameTo(newFile)
            Toast.makeText(this,"Download Started",Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this,"Assets Found",Toast.LENGTH_SHORT).show()
        }
        if(!file2.exists()){
            startDownload("https://raw.githubusercontent.com/ParthBhuva97/assets/main/mask_detector.tflite","mask_detector.tflite")
            val currentFile = File("/storage/emulated/0/Download/mask_detector.tflite")
            val newFile = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/mask_detector.tflite")
            currentFile.renameTo(newFile)
            Toast.makeText(this,"Download Started",Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this,"Assets Found",Toast.LENGTH_SHORT).show()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container_auth) as NavHostFragment

        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.splashScreenFragment,
                R.id.appPinFragment,
                R.id.launcherScreenFragment
            )
        ).build()

        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun startDownload(url: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(fileName) // Title of the Download Notification
        request.setDescription("Downloading") // Description of the Download Notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

}