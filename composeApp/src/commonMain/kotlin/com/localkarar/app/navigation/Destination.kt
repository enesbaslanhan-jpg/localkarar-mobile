package com.localkarar.app.navigation

sealed interface Destination {
    object Login : Destination
    object Home : Destination
    object Courses : Destination
    object DecisionTools : Destination
    object AiMentor : Destination
    object Calculations : Destination
    object News : Destination
    object Updates : Destination
    object Saved : Destination
    object Progress : Destination
    object Profile : Destination
    data class CourseDetail(val courseId: Int) : Destination
    data class LessonReader(val courseId: Int, val lessonId: Int) : Destination
    data class DecisionSession(val sessionId: String) : Destination
}

