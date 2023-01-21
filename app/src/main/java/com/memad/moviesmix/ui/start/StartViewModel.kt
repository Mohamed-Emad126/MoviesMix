package com.memad.moviesmix.ui.start

import androidx.lifecycle.ViewModel
import com.memad.moviesmix.data.remote.StartClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    startClient: StartClient
): ViewModel() {
    
}