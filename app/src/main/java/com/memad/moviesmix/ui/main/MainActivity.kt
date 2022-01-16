package com.memad.moviesmix.ui.main

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.Gravity.BOTTOM
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.*
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.ActivityMainBinding
import com.memad.moviesmix.ui.main.search.SearchFragmentDirections
import com.memad.moviesmix.ui.main.settings.SettingsFragmentDirections
import com.memad.moviesmix.ui.main.settings.SettingsViewModel
import com.memad.moviesmix.utils.*
import com.memad.moviesmix.utils.Constants.BATTERY_SAVER
import com.memad.moviesmix.utils.Constants.DARK
import com.memad.moviesmix.utils.Constants.LANG_PREF
import com.memad.moviesmix.utils.Constants.LIGHT
import com.memad.moviesmix.utils.Constants.SYSTEM_DEFAULT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var oldPrefLocaleCode: String
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var preferencesHelper: SharedPreferencesHelper
    private val fragmentsWithoutNavigation: MutableList<Int> by lazy {
        mutableListOf(
            R.id.searchFragment,
            R.id.settingsFragment
        )
    }
    private val settingsViewModel by viewModels<SettingsViewModel>()
    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MoviesMix)
        applyCurrentTheme()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.themeState.collect {
                    changeTheme(
                        when (it) {
                            ThemeState.BatterySaver -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                            ThemeState.Dark -> AppCompatDelegate.MODE_NIGHT_YES
                            ThemeState.Light -> AppCompatDelegate.MODE_NIGHT_NO
                            ThemeState.SystemDefault -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    )
                }
            }
        }

        navController = Navigation.findNavController(this, R.id.main_nav_host_fragment)
        //checkIsFirstMovieRated()
        setupSmoothBar()
        navController.addOnDestinationChangedListener(this)

        binding.mainToolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.search_item -> {
                    navigateTo(
                        MaterialSharedAxis.Y,
                        SearchFragmentDirections.actionGlobalSearchFragment()
                    )
                }
                R.id.setting_item -> {
                    navigateTo(
                        MaterialSharedAxis.X,
                        SettingsFragmentDirections.actionGlobalSettingsFragment()
                    )
                }
            }
            true
        }
    }

    private fun changeTheme(theme: Int) {
        delegate.localNightMode = theme
    }

    private fun applyCurrentTheme() {
        changeTheme(
            when (preferencesHelper.darkMode) {
                BATTERY_SAVER -> {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
                SYSTEM_DEFAULT -> {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                DARK -> {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
                LIGHT -> {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                else -> {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
        )
    }


    private fun navigateTo(axis: Int, directions: NavDirections) {
        slideUp(binding.mainAppbar, View.GONE)
        slideDown(binding.smoothBottomBar, View.GONE)
        currentNavigationFragment?.apply {
            exitTransition = MaterialSharedAxis(axis, true).apply {
                duration = resources.getInteger(R.integer.duration_large).toLong()
            }
            reenterTransition = MaterialSharedAxis(axis, false).apply {
                duration = resources.getInteger(R.integer.duration_large).toLong()
            }
        }
        findNavController(R.id.main_nav_host_fragment).navigate(directions)
    }

    private fun setupSmoothBar() {
        val popupMenu = PopupMenu(this, binding.smoothBottomBar)
        popupMenu.inflate(R.menu.bottom_bar_menu)
        val menu = popupMenu.menu
        binding.smoothBottomBar.setupWithNavController(menu, navController)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id !in fragmentsWithoutNavigation) {
            slideUp(binding.mainAppbar, View.VISIBLE)
            slideDown(binding.smoothBottomBar, View.VISIBLE)
        }
    }


    private fun slideUp(view: View, visibility: Int) {
        TransitionManager.beginDelayedTransition(
            binding.root,
            Slide(Gravity.TOP).addTarget(view)
                .setDuration(resources.getInteger(R.integer.duration_medium).toLong())
        )
        view.visibility = visibility
    }

    private fun slideDown(view: View, visibility: Int) {
        TransitionManager.beginDelayedTransition(
            binding.root,
            Slide(BOTTOM).addTarget(view)
                .setDuration(resources.getInteger(R.integer.duration_medium).toLong())
        )
        view.visibility = visibility
    }

    override fun attachBaseContext(newBase: Context) {
        oldPrefLocaleCode = Storage(newBase).getPreferredLocale()
        applyOverrideConfiguration(LocaleUtil.getLocalizedConfiguration(oldPrefLocaleCode))
        super.attachBaseContext(newBase)
    }

    override fun onResume() {
        val currentLocaleCode = Storage(this).getPreferredLocale()
        if(oldPrefLocaleCode != currentLocaleCode){
            recreate()
            oldPrefLocaleCode = currentLocaleCode
        }
        super.onResume()
    }

}