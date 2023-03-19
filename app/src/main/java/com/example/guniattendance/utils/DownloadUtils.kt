package com.example.guniattendance.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.downloader.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File


class DownloadUtils(
    private val downloadingContentText:TextView,
    private val parentLayout:View,
    private val progressLayout:View,
    private val progressBar:ProgressBar,
    private val progressBarText:TextView,
    val requireActivity:Activity,
    val requireParentFragment:Fragment,
    private val downloading_content_statistic_text:TextView,
    private val requireContext:Context,
    val btnDownloadPause: AppCompatButton
    ) {
    private val file1Name="mask_detector.tflite"
    private val file2Name="facenet.tflite"
    private val downloadPath = "/storage/emulated/0/Android/data/com.example.guniattendance/files/Download/"
    private val file2 = File(downloadPath+file2Name)
    private val file1 = File(downloadPath+file1Name)

    private val file2DownloadUrl = "https://drive.google.com/uc?export=download&id=1HLuEmdLWRvJRbLHd59BWwLcSjBewUKWx"
    private val file1DownloadUrl = "https://drive.google.com/uc?export=download&id=1go10n_-KPU-tqEuEEPtT7FAyv0TgCYEr"

    companion object{
        var downloadId=-1
    }
    init {
        btnDownloadPause.setOnClickListener {
            when (btnDownloadPause.text) {
                "Pause" -> {
                    PRDownloader.pause(downloadId)
                    btnDownloadPause.text="Resume"
                    downloadingContentText.text = downloadingContentText.text.toString().replace("is downloading...","download paused")
                }
                "Resume" -> {
                    PRDownloader.resume(downloadId)
                    btnDownloadPause.text="Pause"
                    downloadingContentText.text = downloadingContentText.text.toString().replace("download paused","is downloading...")
                }
                else -> {
                    btnDownloadPause.text="Pause"
                    checkDownload()
                }
            }
        }
    }
    fun start()
    {
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .setReadTimeout(30000)
            .setConnectTimeout(30000)
            .build()
        PRDownloader.initialize(requireContext, config)
        if(!file1.exists())
        {
            RequireDownloadContent(file1DownloadUrl,file1Name,downloadPath)
        }
        else if(file1.length().toInt()!=4790720)
        {
            file1.delete()
            RequireDownloadContent(file1DownloadUrl,file1Name,downloadPath)
        }
        else if(!file2.exists())
        {
            RequireDownloadContent(file2DownloadUrl,file2Name,downloadPath)
        }
        else if(file2.length().toInt()!=23705216){
            file2.delete()
            RequireDownloadContent(file2DownloadUrl,file2Name,downloadPath)
        }
        else{
            parentLayout.visibility=View.VISIBLE
        }
    }
    fun Long.formatBinarySize(): String {
        val kiloByteAsByte = 1.0 * 1024.0
        val megaByteAsByte = 1.0 * 1024.0 * 1024.0
        val gigaByteAsByte = 1.0 * 1024.0 * 1024.0 * 1024.0
        val teraByteAsByte = 1.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0
        val petaByteAsByte = 1.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0
        return when {
            this < kiloByteAsByte -> "${this.toDouble()} B"
            this >= kiloByteAsByte && this < megaByteAsByte -> "${
                String.format(
                    "%.2f",
                    (this / kiloByteAsByte)
                )
            } KB"
            this >= megaByteAsByte && this < gigaByteAsByte -> "${
                String.format(
                    "%.2f",
                    (this / megaByteAsByte)
                )
            } MB"
            this >= gigaByteAsByte && this < teraByteAsByte -> "${
                String.format(
                    "%.2f",
                    (this / gigaByteAsByte)
                )
            } GB"
            this >= teraByteAsByte && this < petaByteAsByte -> "${
                String.format(
                    "%.2f",
                    (this / teraByteAsByte)
                )
            } TB"
            else -> "Bigger than 1024 TB"
        }
    }
    private fun RequireDownloadContent(contentURL:String, contentName:String, contentSavePath:String)
    {
        if(contentName==file1Name)
        {
            downloadingContentText.text="Content 1 is downloading..."
        }
        else{
            downloadingContentText.text="Content 2 is downloading..."
        }
        parentLayout.alpha=0.2f
//        requireActivity.window!!.setFlags(
//            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//        )
        progressLayout.visibility= View.VISIBLE
        progressBar.progress=0
        progressBarText.text="0%"
        downloading_content_statistic_text.text="0/0"

        parentLayout.visibility=View.GONE
        downloadId = PRDownloader.download(contentURL, contentSavePath, contentName)
            .build()
            .setOnStartOrResumeListener {

            }
            .setOnPauseListener {
                if(btnDownloadPause.text=="Pause")
                {
                    btnDownloadPause.text="Resume"
                }
            }
            .setOnCancelListener {
                downloadingContentText.text="Downloading was canceled; please try again."
                btnDownloadPause.text="Retry"
            }
            .setOnProgressListener { progress: Progress? ->
                val persentage = progress!!.currentBytes*100/progress.totalBytes
                val downloadedSize = progress.currentBytes.formatBinarySize()
                val totalSize = progress.totalBytes.formatBinarySize()
                downloading_content_statistic_text.text= "${downloadedSize}/${totalSize}"
                progressBar.progress=persentage.toInt()
                progressBarText.text= "$persentage%"
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    MainScope().launch {
                        if(contentName==file1Name)
                        {
                            Snackbar.make(
                                requireParentFragment.requireView(),
                                "content1 Downloaded",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        else{
                            Snackbar.make(
                                requireParentFragment.requireView(),
                                "content2 Downloaded",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        checkDownload()
                    }
                }
                override fun onError(error: Error) {
                    downloadingContentText.text="Downloading has an error"
                    btnDownloadPause.text="Retry"
                }
            })
    }

    fun checkDownload()
    {
        if(!file1.exists())
        {
            RequireDownloadContent(file1DownloadUrl,file1Name,downloadPath)
        }
        else if(file1.length().toInt()!=4790720){
            RequireDownloadContent(file1DownloadUrl,file1Name,downloadPath)
        }
        else if(!file2.exists())
        {
            RequireDownloadContent(file2DownloadUrl,file2Name,downloadPath)
        }
        else if(file2.length().toInt()!=23705216)
        {
            RequireDownloadContent(file2DownloadUrl,file2Name,downloadPath)
        }
        else
        {
                progressLayout.visibility= View.GONE
                parentLayout.alpha = 1f
                parentLayout.visibility=View.VISIBLE
//                requireActivity.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
}