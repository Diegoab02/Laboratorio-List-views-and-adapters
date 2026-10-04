package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.ChangePasswordRequest
import com.dieletech.mobile.data.model.UpdateProfileRequest
import com.dieletech.mobile.data.model.UserProfileNet

/** HU-14 · Perfil de usuario completo. */
object ProfileRepository {
    private val api = RetrofitClient.api

    suspend fun me(): UserProfileNet? = try {
        api.getProfile()
    } catch (_: Exception) {
        null
    }

    suspend fun update(req: UpdateProfileRequest): Result<UserProfileNet> = try {
        Result.success(api.updateProfile(req))
    } catch (e: retrofit2.HttpException) {
        Result.failure(IllegalStateException(parseMessage(e) ?: "No se pudo actualizar"))
    } catch (_: Exception) {
        Result.failure(IllegalStateException("Sin conexion con el servidor"))
    }

    suspend fun changePassword(req: ChangePasswordRequest): Result<String> = try {
        val resp = api.changePassword(req)
        Result.success(resp["message"] ?: "Contraseña actualizada")
    } catch (e: retrofit2.HttpException) {
        Result.failure(IllegalStateException(parseMessage(e) ?: "No se pudo cambiar la contraseña"))
    } catch (_: Exception) {
        Result.failure(IllegalStateException("Sin conexion con el servidor"))
    }

    private fun parseMessage(e: retrofit2.HttpException): String? {
        val body = e.response()?.errorBody()?.string().orEmpty()
        return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(body)?.groupValues?.getOrNull(1)
            ?: body.ifBlank { null }
    }
}
