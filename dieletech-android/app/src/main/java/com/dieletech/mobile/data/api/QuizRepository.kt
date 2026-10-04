package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.*

/** HU-10 · Evaluación final del curso. */
object QuizRepository {
    private val api = RetrofitClient.api

    suspend fun get(courseId: Long): QuizInfoNet? = try {
        api.getQuiz(courseId)
    } catch (_: Exception) {
        null
    }

    suspend fun submit(courseId: Long, answers: List<SubmitAnswerRequest>): Result<QuizResultNet> = try {
        Result.success(api.submitQuiz(courseId, SubmitQuizRequest(answers)))
    } catch (e: retrofit2.HttpException) {
        val body = e.response()?.errorBody()?.string().orEmpty()
        val msg = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.getOrNull(1)
            ?: body.ifBlank { "No se pudo registrar el intento (HTTP ${e.code()})" }
        Result.failure(IllegalStateException(msg))
    } catch (_: Exception) {
        Result.failure(IllegalStateException("No hay conexion con el servidor"))
    }
}
