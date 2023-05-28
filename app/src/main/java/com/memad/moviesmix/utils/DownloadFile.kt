package com.memad.moviesmix.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadFile @Inject constructor(
    @ApplicationContext val context: Context
) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/jpeg")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("IMG${Date().time}.jpg")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "IMG${Date().time}.jpg")
        return downloadManager.enqueue(request)
    }
}

interface Downloader {
    fun downloadFile(url: String): Long
}