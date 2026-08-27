package com.localkarar.app.courses

import com.localkarar.app.auth.SecureStorage
import com.localkarar.app.network.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CourseRepository(
    private val httpClient: HttpClient,
    private val secureStorage: SecureStorage
) {


    suspend fun getCourses(
        page: Int = 1,
        pageSize: Int = 12,
        category: String? = null,
        search: String? = null,
        level: String? = null
    ): Result<CoursesListResponse> {
        return try {
            val response = httpClient.get("/courses") {
                url {
                    parameters.append("page", page.toString())
                    parameters.append("pageSize", pageSize.toString())
                    category?.takeIf { it.isNotBlank() }?.let { parameters.append("category", it) }
                    search?.takeIf { it.isNotBlank() }?.let { parameters.append("search", it) }
                    level?.takeIf { it.isNotBlank() }?.let { parameters.append("level", it) }
                }
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Courses fetch failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<String>> {
        return try {
            val response = httpClient.get("/courses/categories")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyEnrollments(): Result<EnrollmentsListResponse> {
        return try {
            val response = httpClient.get("/enrollments/my")

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Enrollments fetch failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCourseDetail(courseId: Int): Result<CourseDetailResponse> {
        return try {
            val response = httpClient.get("/courses/$courseId")

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Course detail fetch failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLessonDetail(courseId: Int, lessonId: Int): Result<LessonDetailResponse> {
        return try {
            val response = httpClient.get("/courses/$courseId/lessons/$lessonId")

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Lesson detail fetch failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enrollCourse(courseId: Int): Result<Unit> {
        return try {
            val response = httpClient.post("/enrollments") {
                contentType(ContentType.Application.Json)
                setBody(EnrollRequest(courseId))
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Enrollment failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markLessonViewed(lessonId: Int): Result<Unit> {
        return try {
            val response = httpClient.post("/learning/lesson-view") {
                contentType(ContentType.Application.Json)
                setBody(LessonViewRequest(lessonId))
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Lesson view tracking failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markReadingComplete(courseId: Int, lessonId: Int): Result<Unit> {
        return try {
            val response = httpClient.post("/learning/reading-complete") {
                contentType(ContentType.Application.Json)
                setBody(ReadingCompleteRequest(lessonId, courseId))
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Lesson reading completion failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPracticalCard(code: String): Result<PracticalCardDetailResponse> {
        return try {
            val response = httpClient.get("/practical-cards/$code")

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Practical card fetch failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
