package com.memad.moviesmix.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.*

private fun Context.isAppLangDiff(prefLang: String): Boolean {
    val appConfig: Configuration = this.resources.configuration
    val sysConfig: Configuration = Resources.getSystem().configuration

    val appLang: String = appConfig.localeCompat.language
    val sysLang: String = sysConfig.localeCompat.language

    return if ("sys" == prefLang) {
        appLang != sysLang
    } else {
        appLang != prefLang
    }
}

fun Context.toLangIfDiff(lang: String): Context =
    if (this.isAppLangDiff(lang)) {
        this.toLang(lang)
    } else {
        this
    }

fun Context.toLang(toLang: String): Context {
    val config = Configuration()
    val toLocale = langToLocale(toLang)
    Locale.setDefault(toLocale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(toLocale)
        val localeList = LocaleList(toLocale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
    } else {
        config.setLocale(toLocale)
    }
    config.setLayoutDirection(toLocale)
    return this.createConfigurationContext(config)
}

fun langToLocale(toLang: String): Locale = when {
    toLang == "sys" -> Resources.getSystem().configuration.localeCompat
    toLang.contains("ar") -> {
        Locale("ar")
    }
    toLang == "en" -> Locale.ENGLISH
    else -> Resources.getSystem().configuration.localeCompat
}

@Suppress("DEPRECATION")
private val Configuration.localeCompat: Locale
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.locales.get(0)
    } else {
        this.locale
    }

fun Context.recreateTask() {
    this.packageManager
        .getLaunchIntentForPackage(this.packageName)
        ?.let { intent ->
            val restartIntent = Intent.makeRestartActivityTask(intent.component)
            this.startActivity(restartIntent)
            Runtime.getRuntime().exit(0)
        }
}