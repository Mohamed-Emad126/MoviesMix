package com.memad.moviesmix.ui.start.start

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.moviesmix.data.remote.StartClient
import com.memad.moviesmix.models.AuthResponse
import com.memad.moviesmix.utils.AccessNative
import com.memad.moviesmix.utils.NetworkStatus
import com.memad.moviesmix.utils.NetworkStatusHelper
import com.memad.moviesmix.utils.Resource
import com.skydoves.sandwich.StatusCode
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onException
import com.skydoves.sandwich.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val startClient: StartClient,
    networkStatusHelper: NetworkStatusHelper
) : ViewModel() {
    private val _createSessionStatus = MutableLiveData<Resource<AuthResponse>>()
    val createSessionStatus: LiveData<Resource<AuthResponse>> = _createSessionStatus

    val networkStatus: StateFlow<NetworkStatus> = networkStatusHelper.networkStatus.stateIn(
        initialValue = NetworkStatus.Unknown,
        scope = viewModelScope,
        started = WhileSubscribed(5000)
    )
    fun createSession() {
        _createSessionStatus.value = Resource.Loading()
        viewModelScope.launch {
            val response = startClient.getStarted(AccessNative.getApiKey())
            response.onSuccess {
                _createSessionStatus.value = Resource.Success(data)
            }
                .onError {
                    _createSessionStatus.value =
                        when (statusCode) {
                            StatusCode.InternalServerError -> Resource.Error("InternalServerError")
                            StatusCode.BadGateway -> Resource.Error("BadGateway")
                            else -> Resource.Error("$statusCode(${statusCode.code}): ${message()}")
                        }
                }
                .onException {
                    _createSessionStatus.value = Resource.Error("Something went wrong!")
                }
        }
    }
}