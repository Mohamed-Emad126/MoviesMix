package com.memad.moviesmix.data.remote

import com.memad.moviesmix.models.AuthResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.POST
import retrofit2.http.Query

interface StartClient {
    @POST("/3/authentication/guest_session/new")
    suspend fun getStarted(@Query("api_key") apiKey: String): ApiResponse<AuthResponse>
}