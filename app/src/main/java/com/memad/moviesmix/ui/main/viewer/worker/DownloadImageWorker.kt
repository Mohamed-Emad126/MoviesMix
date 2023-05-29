package com.memad.moviesmix.ui.main.viewer.worker

import android.app.DownloadManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DownloadImageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val downloadFile: Downloader
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val imageUrl = inputData.getString("imageUrl") ?: return Result.failure()
        return try {
            val status = downloadFile.downloadFile(imageUrl)
            if (status == DownloadManager.STATUS_SUCCESSFUL.toLong()) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}