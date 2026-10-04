package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.Course
import com.dieletech.mobile.data.model.PurchaseRequest
import com.dieletech.mobile.data.model.PurchaseResponse
import com.dieletech.mobile.data.model.toCourse

object CourseRepository {
    private val api = RetrofitClient.api

    /**
     * Sin datos simulados: si el backend no responde se devuelve una lista
     * vacia y la pantalla lo informa. Mostrar un catalogo inventado con
     * cifras de estudiantes falsas era peor que mostrar un error honesto.
     */
    suspend fun getAllCourses(): List<Course> = try {
        api.getCourses().map { it.toCourse() }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getCourseById(id: Long): Course? = try {
        api.getCourse(id).toCourse()
    } catch (e: Exception) {
        null
    }

    suspend fun getMyCourses(email: String): List<Course> = try {
        val ids = api.myPurchases(email).map { it.courseId }.toSet()
        if (ids.isEmpty()) emptyList()
        else api.getCourses().map { it.toCourse() }.filter { it.id in ids }
    } catch (e: Exception) {
        emptyList()
    }

    /**
     * Nunca simula una compra exitosa. Si el backend rechaza la peticion
     * (sin cupos, curso ya comprado, datos invalidos) se propaga el error
     * real para que la pantalla lo muestre.
     */
    suspend fun purchaseCourse(request: PurchaseRequest): PurchaseResponse = try {
        val r = api.purchase(request)
        PurchaseResponse(success = r.success, orderId = r.orderId, message = r.message)
    } catch (e: retrofit2.HttpException) {
        val body = e.response()?.errorBody()?.string().orEmpty()
        val message = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(body)?.groupValues?.getOrNull(1)
            ?: body.ifBlank { "No se pudo completar la compra (HTTP ${e.code()})" }
        PurchaseResponse(success = false, orderId = "", message = message)
    } catch (e: Exception) {
        PurchaseResponse(
            success = false,
            orderId = "",
            message = "No hay conexion con el servidor. Intenta de nuevo."
        )
    }
}
