package com.example.guniattendance.authorization

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.guniattendance.utils.showProgress
import com.google.firestore.v1.Cursor
import com.google.firestore.v1.CursorOrBuilder
import java.io.File
import java.util.concurrent.Executors

class DownloadModel(val activity: Activity,
                    private var progreeLayout:View,
                    private var progressText:TextView,
                    private var progressBar: ProgressBar,
                    private val parentLayout: View) {
    companion object {
        // Indicate that we would like to update download progress
        private const val UPDATE_DOWNLOAD_PROGRESS = 1
        private var obj:DownloadModel?= null
        fun getDownloadObject(activity: Activity,progreeLayout:View,progressText:TextView,progressBar: ProgressBar,parentLayout: View):DownloadModel{
            if(obj == null){
                obj = DownloadModel(activity,progreeLayout,progressText,progressBar,parentLayout)
            }
            else if(!obj!!.isDownloadStart){
                obj = DownloadModel(activity,progreeLayout,progressText,progressBar,parentLayout)
            }
            return obj!!
        }
        fun destroyObject(){
            obj = null
        }
    }
    var isDownloadStart = false;
    var fileMode1 = true
    var currentFile:File? = null
    var newFile:File? = null
    fun startModelFile1Download(){
        if(isDownloadStart)
            return
        fileMode1 = true
        val file1 = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/Download/facenet.tflite")
        if (!file1.exists()) {
            startDownload("https://raw.githubusercontent.com/ParthBhuva97/assets/main/facenet.tflite","facenet.tflite")
            currentFile = File("/storage/emulated/0/Download/facenet.tflite")
            newFile = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/Download/facenet.tflite")
            currentFile!!.renameTo(newFile)
        }
        else{
            Toast.makeText(activity,"Assets Found", Toast.LENGTH_SHORT).show()
        }
    }
    fun startModelFile2Download(){
        if(isDownloadStart)
            return
        fileMode1 = false
        val file2 = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/Download/mask_detector.tflite")
        if(!file2.exists())
        {
            startDownload("https://raw.githubusercontent.com/ParthBhuva97/assets/main/mask_detector.tflite","mask_detector.tflite")
            currentFile = File("/storage/emulated/0/Download/mask_detector.tflite")

            newFile = File("/storage/emulated/0/Android/data/com.example.guniattendance/files/Download/mask_detector.tflite")
            currentFile!!.renameTo(newFile)
        }
        else{
            Toast.makeText(activity,"Assets Found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startDownload(url: String, fileName: String) {
        if(isDownloadStart)
            return
        isDownloadStart = true
        progressText.text = "1/100"
        showProgress(activity = activity,
            bool = true,
            parentLayout = parentLayout,
            progressBar = progreeLayout
        )
        Toast.makeText(activity,"Download Started", Toast.LENGTH_SHORT).show()
        downloadFile(activity,fileName,url)
    }

    // Use a background thread to check the progress of downloading
    private val executor = Executors.newFixedThreadPool(1)

    // Use a hander to update progress bar on the main thread
    private val mainHandler = Handler(Looper.getMainLooper()) { msg ->
        if (msg.what == UPDATE_DOWNLOAD_PROGRESS) {
            val downloadProgress = msg.arg1
            // Update your progress bar here.
            progressBar.progress = downloadProgress
            progressText.text = "$downloadProgress/100"
            if(downloadProgress >= 100){
                success()
            }
        }
        true
    }
    private fun success(){
        if(!isDownloadStart)
            return
        newFile?.let { currentFile?.renameTo(it) }
        isDownloadStart = false
        if(fileMode1){
            startModelFile2Download()
        }
        else {
            showProgress(
                activity = activity,
                bool = false,
                parentLayout = parentLayout,
                progressBar = progreeLayout
            )
        }
    }

    private fun downloadFile(
        context: Context,
        fileName: String,
        url: String
    ) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setTitle(fileName) // Title of the Download Notification
        request.setDescription("Downloading")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )
        val downloadId = downloadManager.enqueue(request)
        executor.execute {
            var progress = 0
            var isDownloadFinished = false
            while (!isDownloadFinished) {
                val cursor:android.database.Cursor=downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_RUNNING -> {
                            val totalBytes =
                                cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            if (totalBytes > 0) {
                                val downloadedBytes =
                                    cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                progress = (downloadedBytes * 100 / totalBytes).toInt()
                            }
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            progress = 100
                            isDownloadFinished = true
                        }
                        DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_FAILED -> isDownloadFinished = true
                    }
                    val message = Message.obtain()
                    message.what = UPDATE_DOWNLOAD_PROGRESS
                    message.arg1 = progress
                    mainHandler.sendMessage(message)
                }
                cursor.close()
            }
        }
    }

}