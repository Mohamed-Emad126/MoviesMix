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
import com.memad.moviesmix.ui.main.MainActivity


@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityStartBinding

    @Inject
    @SessionKey
    lateinit var sessionKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (sessionKey.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        setTheme(R.style.Theme_MoviesMix)
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

}