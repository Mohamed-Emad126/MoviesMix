package com.memad.moviesmix.utils

object AccessNative {
    init {
        System.loadLibrary("api-keys")
    }

    external fun getApiKey(): String
}