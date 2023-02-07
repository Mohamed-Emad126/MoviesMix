package com.memad.moviesmix.ui.main

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.navigation.*
import com.google.android.material.transition.MaterialSharedAxis
import com.memad.moviesmix.R
import com.memad.moviesmix.databinding.ActivityMainBinding
import com.memad.moviesmix.ui.main.search.SearchFragmentDirections
import com.memad.moviesmix.ui.main.settings.SettingsFragmentDirections
import com.memad.moviesmix.utils.Constants.DARK
import com.memad.moviesmix.utils.Constants.LIGHT
import com.memad.moviesmix.utils.LocaleUtil
import com.memad.moviesmix.utils.SharedPreferencesHelper
import com.memad.moviesmix.utils.Storage
import dagger.hilt.android.AndroidEntryPoint
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
            R.id.settingsFragment,
            R.id.movieDescriptionFragment
        )
    }
    private val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        applyCurrentTheme()
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

    private fun changeTheme(theme: Int) {
        AppCompatDelegate.setDefaultNightMode(theme)
        delegate.applyDayNight()
    }

    private fun applyCurrentTheme() {
        changeTheme(
            when (preferencesHelper.darkMode) {
                DARK -> {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
                LIGHT -> {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                else -> {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            }
        )
    }


    private fun navigateTo(axis: Int, directions: NavDirections) {
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
            slideDownAppBar()
            slideUpBottomBarr()
        } else {
            slideUpAppBar()
            slideDownBottomBar()
        }
    }


    private fun slideUpAppBar(view: View = binding.mainAppbar) {
        view.animate().apply {
            translationYBy(0F)
            translationY(view.height.toFloat() * -1)
            duration = resources.getInteger(R.integer.duration_large).toLong()
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                view.visibility = View.GONE
            }

            override fun onAnimationEnd(p0: Animator) {
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

    private fun slideDownBottomBar(view: View = binding.smoothBottomBar) {
        view.animate().apply {
            translationYBy(0F)
            translationY(view.height.toFloat())
            duration = resources.getInteger(R.integer.duration_large).toLong()
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                view.visibility = View.GONE
            }

            override fun onAnimationEnd(p0: Animator) {

            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

    private fun slideDownAppBar(view: View = binding.mainAppbar) {
        view.animate().apply {
            translationYBy(view.height.toFloat())
            translationY(0F)
            duration = resources.getInteger(R.integer.duration_large).toLong()
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(p0: Animator) {
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

    private fun slideUpBottomBarr(view: View = binding.smoothBottomBar) {
        view.animate().apply {
            translationYBy(view.height.toFloat())
            translationY(0F)
            duration = resources.getInteger(R.integer.duration_large).toLong()
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(p0: Animator) {
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        oldPrefLocaleCode = Storage(newBase).getPreferredLocale()
        applyOverrideConfiguration(LocaleUtil.getLocalizedConfiguration(oldPrefLocaleCode))
        super.attachBaseContext(newBase)
    }

    override fun onResume() {
        val currentLocaleCode = Storage(this).getPreferredLocale()
        if (oldPrefLocaleCode != currentLocaleCode) {
            recreate()
            oldPrefLocaleCode = currentLocaleCode
        }
        super.onResume()
    }

}