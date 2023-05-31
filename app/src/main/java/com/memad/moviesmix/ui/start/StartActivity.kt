package com.memad.moviesmix.ui.start

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.ActivityStartBinding
import com.memad.moviesmix.di.annotations.SessionKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.memad.moviesmix.ui.main.MainActivity
import com.memad.moviesmix.utils.Constants
import com.memad.moviesmix.utils.SharedPreferencesHelper


@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityStartBinding

    @Inject
    @SessionKey
    lateinit var sessionKey: String

    @Inject
    lateinit var preferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        applyCurrentTheme()
        if (sessionKey.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //setTheme(R.style.Theme_MoviesMix)
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun changeTheme(theme: Int) {
        AppCompatDelegate.setDefaultNightMode(theme)
        delegate.applyDayNight()
    }

    private fun applyCurrentTheme() {
        changeTheme(
            when (preferencesHelper.darkMode) {
                Constants.DARK -> {
                    AppCompatDelegate.MODE_NIGHT_YES
                }

                Constants.LIGHT -> {
                    AppCompatDelegate.MODE_NIGHT_NO
                }

                else -> {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            }
        )
    }

}