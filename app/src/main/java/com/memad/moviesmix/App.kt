package com.memad.moviesmix

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.memad.moviesmix.utils.LocaleUtil
import com.memad.moviesmix.utils.Storage
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(
            LocaleUtil.getLocalizedContext(
                base,
                Storage(base).getPreferredLocale()
            )
        )
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .setWorkerFactory(workerFactory)
        .build()

}