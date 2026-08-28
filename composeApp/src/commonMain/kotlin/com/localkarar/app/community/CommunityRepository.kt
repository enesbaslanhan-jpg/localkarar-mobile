package com.localkarar.app.community

import com.localkarar.app.network.ApiConfig
import com.localkarar.app.network.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CommunityRepository(
    private val client: HttpClient,
    private val baseUrl: String = ApiConfig.baseUrl
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val communityBase = "$baseUrl/community"
    private val socialBase = "$baseUrl/community/social"

    // ==========================================
    // 1. COMMUNITY FEED & POSTS
    // ==========================================

    suspend fun getFeed(type: String?, cursor: String?): Result<CommunityFeedResponseDto> {
        return try {
            val response = client.get(communityBase) {
                if (!type.isNullOrBlank()) parameter("type", type)
                if (!cursor.isNullOrBlank()) parameter("cursor", cursor)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<CommunityFeedResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Gönderiler yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPost(postId: String): Result<PostDetailResponseDto> {
        return try {
            val response = client.get("$communityBase/post/$postId")
            if (response.status.isSuccess()) {
                Result.success(response.body<PostDetailResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Gönderi yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(
        metin: String,
        mediaId: String? = null,
        parentId: String? = null,
        quotedPostId: String? = null
    ): Result<CreateCommunityPostResponseDto> {
        return try {
            val response = client.post("$communityBase/posts") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCommunityPostRequestDto(
                        metin = metin,
                        mediaId = mediaId,
                        parentId = parentId,
                        quotedPostId = quotedPostId
                    )
                )
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<CreateCommunityPostResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Gönderi oluşturulamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val response = client.delete("$communityBase/$postId")
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(errorMessage(response, "Gönderi kaldırılamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likePost(postId: String): Result<InteractionResponseDto> {
        return try {
            val response = client.post("$communityBase/$postId/like")
            if (response.status.isSuccess()) {
                Result.success(response.body<InteractionResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Beğeni kaydedilemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikePost(postId: String): Result<InteractionResponseDto> {
        return try {
            val response = client.delete("$communityBase/$postId/like")
            if (response.status.isSuccess()) {
                Result.success(response.body<InteractionResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Beğeni kaldırılamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bookmarkPost(postId: String): Result<InteractionResponseDto> {
        return try {
            val response = client.post("$communityBase/$postId/bookmark")
            if (response.status.isSuccess()) {
                Result.success(response.body<InteractionResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Kaydetme başarısız")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unbookmarkPost(postId: String): Result<InteractionResponseDto> {
        return try {
            val response = client.delete("$communityBase/$postId/bookmark")
            if (response.status.isSuccess()) {
                Result.success(response.body<InteractionResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Kayıt kaldırılamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportPost(postId: String, reason: String, details: String?): Result<Unit> {
        return try {
            val response = client.post("$communityBase/$postId/reports") {
                contentType(ContentType.Application.Json)
                setBody(ReportPostRequestDto(reason = reason, details = details))
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(errorMessage(response, "Şikayet iletilemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. MEDIA UPLOAD
    // ==========================================

    suspend fun uploadMedia(fileName: String, bytes: ByteArray, mimeType: String): Result<MediaUploadResponseDto> {
        return try {
            val response = client.submitFormWithBinaryData(
                url = "$communityBase/media",
                formData = formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            )
            if (response.status.isSuccess()) {
                Result.success(response.body<MediaUploadResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Medya yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun discardMedia(mediaId: String): Result<Unit> {
        return try {
            val response = client.delete("$communityBase/media/$mediaId")
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(errorMessage(response, "Medya silinemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 3. OWN PROFILE SUMMARY & LISTS
    // ==========================================

    suspend fun getOwnSummary(): Result<OwnSummaryDto> {
        return try {
            val response = client.get("$communityBase/me/summary")
            if (response.status.isSuccess()) {
                Result.success(response.body<OwnSummaryDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Profil özeti yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnList(liste: String): Result<List<CommunityPostDto>> {
        return try {
            val response = client.get("$communityBase/me/$liste")
            if (response.status.isSuccess()) {
                val data = response.body<OwnListResponseDto>()
                Result.success(data.posts)
            } else {
                Result.failure(Exception(errorMessage(response, "Liste yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOtherUserPosts(userId: Int, tur: String? = null): Result<List<CommunityPostDto>> {
        return try {
            val response = client.get("$communityBase/people/$userId/posts") {
                if (!tur.isNullOrBlank()) parameter("tur", tur)
            }
            if (response.status.isSuccess()) {
                val data = response.body<OwnListResponseDto>()
                Result.success(data.posts)
            } else {
                Result.failure(Exception(errorMessage(response, "Gönderiler yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 4. PEOPLE & SOCIAL PROFILES
    // ==========================================

    suspend fun getPeople(q: String = ""): Result<PeopleResponseDto> {
        return try {
            val response = client.get("$socialBase/people") {
                if (q.isNotBlank()) parameter("q", q)
            }
            if (response.status.isSuccess()) {
                Result.success(response.body<PeopleResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Kişiler yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOtherProfile(userId: Int): Result<OtherProfileResponseDto> {
        return try {
            val response = client.get("$socialBase/people/$userId/profile")
            if (response.status.isSuccess()) {
                Result.success(response.body<OtherProfileResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Profil yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfileFollowers(userId: Int): Result<List<PersonDto>> {
        return try {
            val response = client.get("$socialBase/people/$userId/followers")
            if (response.status.isSuccess()) {
                val data = response.body<ProfilePeopleResponseDto>()
                Result.success(data.people)
            } else {
                Result.failure(Exception(errorMessage(response, "Takipçiler yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfileFollowing(userId: Int): Result<List<PersonDto>> {
        return try {
            val response = client.get("$socialBase/people/$userId/following")
            if (response.status.isSuccess()) {
                val data = response.body<ProfilePeopleResponseDto>()
                Result.success(data.people)
            } else {
                Result.failure(Exception(errorMessage(response, "Takip edilenler yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun follow(personId: Int): Result<Boolean> {
        return try {
            val response = client.post("$socialBase/people/$personId/follow")
            if (response.status.isSuccess()) {
                val data = response.body<FollowActionResponseDto>()
                Result.success(data.following)
            } else {
                Result.failure(Exception(errorMessage(response, "Takip işlemi başarısız")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollow(personId: Int): Result<Boolean> {
        return try {
            val response = client.delete("$socialBase/people/$personId/follow")
            if (response.status.isSuccess()) {
                val data = response.body<FollowActionResponseDto>()
                Result.success(data.following)
            } else {
                Result.failure(Exception(errorMessage(response, "Takip bırakılamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun block(personId: Int): Result<Boolean> {
        return try {
            val response = client.post("$socialBase/people/$personId/block")
            if (response.status.isSuccess()) {
                val data = response.body<BlockActionResponseDto>()
                Result.success(data.blocked)
            } else {
                Result.failure(Exception(errorMessage(response, "Engelleme işlemi başarısız")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblock(personId: Int): Result<Boolean> {
        return try {
            val response = client.delete("$socialBase/people/$personId/block")
            if (response.status.isSuccess()) {
                val data = response.body<BlockActionResponseDto>()
                Result.success(data.blocked)
            } else {
                Result.failure(Exception(errorMessage(response, "Engel kaldırılamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportUser(personId: Int, reason: String, details: String?): Result<Unit> {
        return try {
            val response = client.post("$socialBase/people/$personId/report") {
                contentType(ContentType.Application.Json)
                setBody(UserReportRequestDto(reason = reason, details = details))
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(errorMessage(response, "Kullanıcı şikayet edilemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 5. THREADS & MESSAGES
    // ==========================================

    suspend fun getThreads(): Result<List<CommunityThreadDto>> {
        return try {
            val response = client.get("$socialBase/threads")
            if (response.status.isSuccess()) {
                val data = response.body<ThreadsResponseDto>()
                Result.success(data.threads)
            } else {
                Result.failure(Exception(errorMessage(response, "Sohbetler yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createThread(memberIds: List<Int>, name: String? = null): Result<CommunityThreadDto> {
        return try {
            val response = client.post("$socialBase/threads") {
                contentType(ContentType.Application.Json)
                setBody(CreateThreadRequestDto(name = name, memberIds = memberIds))
            }
            if (response.status.isSuccess()) {
                val data = response.body<CreateThreadResponseDto>()
                Result.success(data.thread)
            } else {
                Result.failure(Exception(errorMessage(response, "Sohbet başlatılamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inviteDecision(threadId: String, karar: String): Result<String> {
        return try {
            val response = client.post("$socialBase/threads/$threadId/invite/$karar")
            if (response.status.isSuccess()) {
                val data = response.body<InviteDecisionResponseDto>()
                Result.success(data.durum)
            } else {
                Result.failure(Exception(errorMessage(response, "Davet işlemi başarısız")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(threadId: String): Result<List<ThreadMessageDto>> {
        return try {
            val response = client.get("$socialBase/threads/$threadId/messages")
            if (response.status.isSuccess()) {
                val data = response.body<MessagesResponseDto>()
                Result.success(data.messages)
            } else {
                Result.failure(Exception(errorMessage(response, "Mesajlar yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(threadId: String, body: String): Result<ThreadMessageDto> {
        return try {
            val response = client.post("$socialBase/threads/$threadId/messages") {
                contentType(ContentType.Application.Json)
                setBody(SendThreadMessageRequestDto(body = body))
            }
            if (response.status.isSuccess()) {
                val data = response.body<SendMessageResponseDto>()
                Result.success(data.message)
            } else {
                Result.failure(Exception(errorMessage(response, "Mesaj gönderilemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 6. NOTIFICATIONS
    // ==========================================

    suspend fun getNotifications(): Result<CommunityNotificationsResponseDto> {
        return try {
            val response = client.get("$socialBase/notifications")
            if (response.status.isSuccess()) {
                Result.success(response.body<CommunityNotificationsResponseDto>())
            } else {
                Result.failure(Exception(errorMessage(response, "Bildirimler yüklenemedi")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationsRead(): Result<Int> {
        return try {
            val response = client.post("$socialBase/notifications/read")
            if (response.status.isSuccess()) {
                val data = response.body<MarkReadResponseDto>()
                Result.success(data.okundu)
            } else {
                Result.failure(Exception(errorMessage(response, "Bildirimler okundu yapılamadı")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ERROR PARSER
    // ==========================================

    private suspend fun errorMessage(response: HttpResponse, defaultMsg: String): String {
        return try {
            val text = response.bodyAsText()
            if (text.isBlank()) return "$defaultMsg (${response.status.value})"
            val element = json.parseToJsonElement(text).jsonObject
            element["error"]?.jsonPrimitive?.contentOrNull
                ?: element["message"]?.jsonPrimitive?.contentOrNull
                ?: "$defaultMsg (${response.status.value})"
        } catch (_: Exception) {
            "$defaultMsg (${response.status.value})"
        }
    }
}