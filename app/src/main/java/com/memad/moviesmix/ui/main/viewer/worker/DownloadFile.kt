package com.memad.moviesmix.ui.main.viewer.worker

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Thread.sleep
import java.time.Duration
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
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("IMG${Date().time}.jpg")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "IMG${Date().time}.jpg"
            )

        val id = downloadManager.enqueue(request)
        sleep(Duration.ofSeconds(2).toMillis())
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            return when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    DownloadManager.STATUS_SUCCESSFUL.toLong()
                }
                else -> {
                    DownloadManager.STATUS_FAILED.toLong()
                }
            }
        }
        return DownloadManager.STATUS_FAILED.toLong()
    }
}

interface Downloader {
    fun downloadFile(url: String): Long
}