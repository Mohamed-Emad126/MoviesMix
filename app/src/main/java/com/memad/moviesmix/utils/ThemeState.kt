package com.memad.moviesmix.utils

sealed class ThemeState {
    object Dark : ThemeState()
    object Light : ThemeState()
    object BatterySaver : ThemeState()
    object SystemDefault : ThemeState()
}