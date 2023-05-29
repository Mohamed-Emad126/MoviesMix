package com.memad.moviesmix.ui.main.viewer.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show()
        }
    }
}