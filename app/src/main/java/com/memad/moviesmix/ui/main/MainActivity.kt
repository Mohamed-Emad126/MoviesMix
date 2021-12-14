package com.memad.moviesmix.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.Gravity.BOTTOM
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.ActivityMainBinding
import com.memad.moviesmix.ui.main.search.SearchFragmentDirections
import com.memad.moviesmix.ui.main.settings.SettingsFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList
import android.view.animation.TranslateAnimation
import androidx.transition.Slide


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private val fragmentsWithoutNavigation: MutableList<Int> by lazy {
        mutableListOf(
            R.id.searchFragment,
            R.id.settingsFragment
        )
    }
    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MoviesMix)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
            Slide(Gravity.BOTTOM).addTarget(view)
                .setDuration(resources.getInteger(R.integer.duration_medium).toLong())
        )
        view.visibility = visibility
    }

}