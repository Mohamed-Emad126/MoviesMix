package com.memad.moviesmix.utils

import javax.inject.Inject
import javax.inject.Singleton

object AccessNative{
    init {
        System.loadLibrary("api-keys")
    }

    external fun getApiKey(): String
}