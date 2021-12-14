package com.memad.moviesmix.models

data class AuthResponse(
    val expires_at: String,
    val guest_session_id: String,
    val success: Boolean
)