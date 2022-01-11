package com.memad.moviesmix

import android.app.Application
import android.content.Context
import com.memad.moviesmix.utils.SharedPreferencesHelper
import com.memad.moviesmix.utils.toLangIfDiff
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var preferencesHelper: SharedPreferencesHelper
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(
            base.toLangIfDiff(
                preferencesHelper.read("langPref", "sys")!!
            )
        )
    }
}