package com.example.guniattendance.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import com.downloader.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class DownloadUtils(
    val downloadingContentText:TextView,
    val parentLayout:View,
    val progressLayout:View,
    val progressBar:ProgressBar,
    val progressBarText:TextView,
    val requireActivity:Activity,
    val lifecycleScope:LifecycleCoroutineScope,
    val requireParentFragment:Fragment,
    val downloading_content_statistic_text:TextView,
    val requireContext:Context
    ) {
    private val file1Name="mask_detector.tflite"
    private val file2Name="facenet.tflite"
    private val downloadPath = "/storage/emulated/0/Android/data/com.example.guniattendance/files/Download/"
    private val file2 = File(downloadPath+file2Name)
    private val file1 = File(downloadPath+file1Name)

    private val file2DownloadUrl = "https://drive.google.com/uc?export=download&id=1HLuEmdLWRvJRbLHd59BWwLcSjBewUKWx"
    private val file1DownloadUrl = "https://drive.google.com/uc?export=download&id=1go10n_-KPU-tqEuEEPtT7FAyv0TgCYEr"

    private var downloadId=-1
    suspend fun start()
    {
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
            Snackbar.make(
                requireParentFragment.requireView(),
                "Assets Found",
                Snackbar.LENGTH_LONG
            ).show()
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
    private suspend fun RequireDownloadContent(contentURL:String, contentName:String, contentSavePath:String)
    {
        if(contentName==file1Name)
        {
            downloadingContentText.text="Content 1 Downloading...."
        }
        else{
            downloadingContentText.text="Content 2 Downloading...."
        }
        parentLayout.alpha=0.2f
        requireActivity.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        progressLayout.visibility= View.VISIBLE
        progressBar.progress=0
        progressBarText.text="0%"
        downloading_content_statistic_text.text="0/0"
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .setReadTimeout(30000)
            .setConnectTimeout(30000)
            .build()
        PRDownloader.initialize(requireContext, config)
        downloadId = PRDownloader.download(contentURL, contentSavePath, contentName)
            .build()
            .setOnStartOrResumeListener {

            }
            .setOnPauseListener {
                MainScope().launch {
                    checkDownload()
                }
            }
            .setOnCancelListener {
                MainScope().launch {
                    checkDownload()
                }
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
                    MainScope().launch {
                        checkDownload()
                    }
                }
            })

    }
    suspend fun checkDownload()
    {
        if(!file1.exists())
        {
            RequireDownloadContent(file1DownloadUrl,file1Name,downloadPath)
        }
        else if(file1.length().toInt()!=4790720){
            file1.delete()
            RequireDownloadContent(file1DownloadUrl,file1Name,downloadPath)
        }
        else if(!file2.exists())
        {
            RequireDownloadContent(file2DownloadUrl,file2Name,downloadPath)
        }
        else if(file2.length().toInt()!=23705216)
        {
            file2.delete()
            RequireDownloadContent(file2DownloadUrl,file2Name,downloadPath)
        }
        else
        {
            withContext(Dispatchers.Main)
            {
                progressLayout.visibility= View.GONE
                parentLayout.alpha = 1f
                requireActivity.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }
}