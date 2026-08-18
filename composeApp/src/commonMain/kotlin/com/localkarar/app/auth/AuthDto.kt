package com.localkarar.app.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val name: String,
    val role: String,
    val onboardingCompleted: Boolean = false
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserDto
)
