package com.memad.moviesmix

import android.app.Application
import android.content.Context
import com.memad.moviesmix.utils.LocaleUtil
import com.memad.moviesmix.utils.Storage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    val storage : Storage by lazy {
        Storage(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.getLocalizedContext(base, Storage(base).getPreferredLocale()))
    }
}