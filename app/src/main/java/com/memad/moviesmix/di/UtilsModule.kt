package com.memad.moviesmix.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.memad.moviesmix.di.annotations.SessionKey
import com.memad.moviesmix.models.AuthResponse
import com.memad.moviesmix.ui.main.viewer.worker.DownloadFile
import com.memad.moviesmix.ui.main.viewer.worker.Downloader
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.SharedPreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilsModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val mainKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            Constants.APP_PREF,
            mainKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @SessionKey
    @Singleton
    @Provides
    fun provideSessionKey(sharedPreferencesHelper: SharedPreferencesHelper, gson: Gson): String {
        val session = sharedPreferencesHelper.read(
            Constants.SESSION,
            gson.toJson(AuthResponse("", "", false))
        )
        return gson.fromJson(session, AuthResponse::class.java).guest_session_id.trim()
    }

    @Singleton
    @Provides
    fun provideDownload(@ApplicationContext context: Context): Downloader {
        return DownloadFile(context)
    }
}