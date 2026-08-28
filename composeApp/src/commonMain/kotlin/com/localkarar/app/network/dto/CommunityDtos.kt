package com.localkarar.app.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommunityAuthorDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val avatarStoredName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null
)

@Serializable
data class CommunityMediaDto(
    val id: String,
    val originalName: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Int? = null,
    val kind: String? = null,
    val url: String? = null
)

@Serializable
data class QuotedPostDto(
    val id: String,
    val kaldirildi: Boolean = false,
    val summary: String? = null,
    val author: CommunityAuthorDto? = null,
    val media: CommunityMediaDto? = null
)

@Serializable
data class CommunityPostDto(
    val id: String,
    val authorId: Int? = null,
    val author: CommunityAuthorDto? = null,
    val postType: String = "user",
    val title: String? = null,
    val summary: String = "",
    val content: String? = null,
    val category: String? = null,
    val status: String = "published",
    val publishedAt: String? = null,
    val createdAt: String? = null,
    val parentId: String? = null,
    val quotedPostId: String? = null,
    val media: CommunityMediaDto? = null,
    val quotedPost: QuotedPostDto? = null,
    val replies: List<CommunityPostDto> = emptyList(),
    val begeniSayisi: Int = 0,
    val yanitSayisi: Int = 0,
    val alintiSayisi: Int = 0,
    val begendim: Boolean = false,
    val kaydettim: Boolean = false,
    val moderationReason: String? = null
)

@Serializable
data class CommunityFeedResponseDto(
    val posts: List<CommunityPostDto> = emptyList(),
    val nextCursor: String? = null
)

@Serializable
data class PostDetailResponseDto(
    val post: CommunityPostDto,
    val parent: CommunityPostDto? = null
)

@Serializable
data class CreateCommunityPostRequestDto(
    val metin: String = "",
    val mediaId: String? = null,
    val parentId: String? = null,
    val quotedPostId: String? = null
)

@Serializable
data class CreateCommunityPostResponseDto(
    val post: CommunityPostDto? = null,
    val message: String? = null
)

@Serializable
data class InteractionResponseDto(
    val aktif: Boolean = false,
    val sayi: Int = 0
)

@Serializable
data class SimpleSuccessDto(
    val success: Boolean = true
)

@Serializable
data class ReportPostRequestDto(
    val reason: String,
    val details: String? = null
)

@Serializable
data class OwnSummaryDto(
    val paylasim: Int = 0,
    val begeni: Int = 0,
    val kayit: Int = 0,
    val takipci: Int = 0,
    val takipEdilen: Int = 0
)

@Serializable
data class OwnListResponseDto(
    val posts: List<CommunityPostDto> = emptyList()
)

// === SOCIAL DTOS ===

@Serializable
data class PersonDto(
    val id: Int,
    val name: String,
    val role: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class PeopleResponseDto(
    val people: List<PersonDto> = emptyList(),
    val followingIds: List<Int> = emptyList(),
    val blockedIds: List<Int> = emptyList()
)

@Serializable
data class OtherProfileDto(
    val id: Int,
    val name: String,
    val role: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val websiteUrl: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val katilma: String? = null,
    val kendisi: Boolean = false
)

@Serializable
data class OtherProfileSayilarDto(
    val paylasim: Int = 0,
    val takipci: Int = 0,
    val takipEdilen: Int = 0
)

@Serializable
data class OtherProfileResponseDto(
    val profil: OtherProfileDto,
    val sayilar: OtherProfileSayilarDto,
    val takipEdiyorum: Boolean = false
)

@Serializable
data class ProfilePeopleResponseDto(
    val people: List<PersonDto> = emptyList()
)

@Serializable
data class FollowActionResponseDto(
    val following: Boolean = false
)

@Serializable
data class BlockActionResponseDto(
    val blocked: Boolean = false
)

@Serializable
data class UserReportRequestDto(
    val reason: String,
    val details: String? = null
)

@Serializable
data class UserReportResponseDto(
    val report: UserReportInfoDto? = null
)

@Serializable
data class UserReportInfoDto(
    val id: String,
    val status: String = "open"
)

// === THREADS & MESSAGES ===

@Serializable
data class ThreadMemberDto(
    val threadId: String? = null,
    val userId: Int,
    val role: String = "member",
    val status: String = "joined",
    val joinedAt: String? = null,
    val user: CommunityAuthorDto? = null
)

@Serializable
data class ThreadMessageDto(
    val id: String,
    val threadId: String,
    val senderId: Int,
    val body: String,
    val createdAt: String? = null,
    val sender: CommunityAuthorDto? = null
)

@Serializable
data class CommunityThreadDto(
    val id: String,
    val name: String? = null,
    val isGroup: Boolean = false,
    val createdById: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val durumum: String = "joined",
    val members: List<ThreadMemberDto> = emptyList(),
    val messages: List<ThreadMessageDto> = emptyList()
)

@Serializable
data class ThreadsResponseDto(
    val threads: List<CommunityThreadDto> = emptyList()
)

@Serializable
data class CreateThreadRequestDto(
    val name: String? = null,
    val memberIds: List<Int>
)

@Serializable
data class CreateThreadResponseDto(
    val thread: CommunityThreadDto
)

@Serializable
data class SendThreadMessageRequestDto(
    val body: String
)

@Serializable
data class SendMessageResponseDto(
    val message: ThreadMessageDto
)

@Serializable
data class MessagesResponseDto(
    val messages: List<ThreadMessageDto> = emptyList()
)

@Serializable
data class InviteDecisionResponseDto(
    val durum: String
)

// === NOTIFICATIONS ===

@Serializable
data class NotificationPostDto(
    val id: String,
    val ozet: String? = null
)

@Serializable
data class NotificationActorDto(
    val id: Int,
    val name: String,
    val avatarUrl: String? = null
)

@Serializable
data class CommunityNotificationDto(
    val id: String,
    val type: String,
    val createdAt: String? = null,
    val readAt: String? = null,
    val postId: String? = null,
    val threadId: String? = null,
    val post: NotificationPostDto? = null,
    val actor: NotificationActorDto? = null
)

@Serializable
data class CommunityNotificationsResponseDto(
    val unread: Int = 0,
    val items: List<CommunityNotificationDto> = emptyList()
)

@Serializable
data class MarkReadResponseDto(
    val okundu: Int = 0
)

// === MEDIA UPLOAD ===

@Serializable
data class MediaUploadResponseDto(
    val media: CommunityMediaDto
)