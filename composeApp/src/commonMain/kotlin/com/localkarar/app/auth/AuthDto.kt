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
    val password: String,
    val acceptedLegal: Boolean = true
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
    val coverUrl: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val websiteUrl: String? = null,
    val onboardingCompleted: Boolean = false,
    val emailVerified: Boolean = false
)

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String? = null,
    val user: UserDto
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val token: String,
    val refreshToken: String
)

@Serializable
data class SessionDto(
    val token: String,
    val refreshToken: String? = null,
    val user: UserDto
)

@Serializable
data class ProfileUpdateDto(
    val id: Int,
    val name: String,
    val bio: String? = null,
    val location: String? = null,
    val websiteUrl: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null
)

@Serializable
data class ConsentItemDto(
    val documentType: String,
    val version: String,
    val acceptedAt: String? = null
)

@Serializable
data class MissingConsentDto(
    val type: String,
    val version: String,
    val title: String
)

@Serializable
data class ConsentsResponseDto(
    val accepted: List<ConsentItemDto> = emptyList(),
    val missing: List<MissingConsentDto> = emptyList()
)

@Serializable
data class LegalDocumentDto(
    val type: String,
    val version: String,
    val title: String,
    val summary: String? = null,
    val requiredAtSignup: Boolean = false,
    val publishedAt: String? = null
)

@Serializable
data class LegalDocumentsResponseDto(
    val documents: List<LegalDocumentDto> = emptyList()
)

@Serializable
data class AuthSuccessResponse(
    val success: Boolean = true,
    val message: String? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val alreadyVerified: Boolean = false,
    val expiresInMinutes: Double? = null
)
