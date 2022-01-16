package com.memad.moviesmix.ui.main.settings

import androidx.lifecycle.ViewModel
import com.memad.moviesmix.utils.Constants.BATTERY_SAVER
import com.memad.moviesmix.utils.Constants.DARK
import com.memad.moviesmix.utils.Constants.LIGHT
import com.memad.moviesmix.utils.Constants.SYSTEM_DEFAULT
import com.memad.moviesmix.utils.SharedPreferencesHelper
import com.memad.moviesmix.utils.ThemeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor():ViewModel() {

    @Inject
    lateinit var preferencesHelper: SharedPreferencesHelper
    private val _themeState = MutableStateFlow<ThemeState>(ThemeState.SystemDefault)
    val themeState: StateFlow<ThemeState> = _themeState

    fun changeTheme(themeState: ThemeState){
        _themeState.value = themeState
        when(themeState){
            ThemeState.BatterySaver -> preferencesHelper.darkMode = BATTERY_SAVER
            ThemeState.Dark -> preferencesHelper.darkMode = DARK
            ThemeState.Light -> preferencesHelper.darkMode = LIGHT
            ThemeState.SystemDefault -> preferencesHelper.darkMode = SYSTEM_DEFAULT
        }
    }
}