package com.localkarar.app.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class PasswordResetConfirmRequest(
    val token: String,
    val newPassword: String
)

@Serializable
data class EmailVerifyConfirmRequest(
    val code: String
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val name: String,
    val role: String,
    val avatarUrl: String? = null,
    val onboardingCompleted: Boolean = false
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class AuthSuccessResponse(
    val success: Boolean = true,
    val message: String? = null,
    val token: String? = null,
    val user: UserDto? = null,
    val alreadyVerified: Boolean = false,
    val expiresInMinutes: Double? = null
)
