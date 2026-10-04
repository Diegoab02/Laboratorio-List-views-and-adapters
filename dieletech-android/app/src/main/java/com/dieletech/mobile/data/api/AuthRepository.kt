package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.*

object AuthRepository {
    private val api = RetrofitClient.api

    suspend fun register(name: String, email: String, password: String, role: String = "STUDENT"): Result<String> = try {
        val resp = api.register(RegisterRequest(name, email, password, role))
        if (resp.isSuccessful) {
            Result.success(resp.body()?.string() ?: "Registro exitoso.")
        } else {
            Result.failure(Exception(resp.errorBody()?.string() ?: "No se pudo registrar"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("No hay conexión con el servidor"))
    }

    suspend fun login(email: String, password: String): Result<AuthResponseNet> = try {
        val resp = api.login(LoginRequest(email, password))
        val body = resp.body()
        if (resp.isSuccessful && body != null) {
            Result.success(body)
        } else {
            val errorMsg = resp.errorBody()?.string() ?: "Credenciales incorrectas"
            Result.failure(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Result.failure(Exception("No hay conexión con el servidor"))
    }

    suspend fun verifyCode(email: String, code: String): Result<String> = try {
        val resp = api.verifyCode(VerifyCodeRequest(email, code))
        if (resp.isSuccessful) {
            Result.success(resp.body()?.string() ?: "Cuenta verificada.")
        } else {
            Result.failure(Exception(resp.errorBody()?.string() ?: "Código incorrecto"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("No hay conexión con el servidor"))
    }

    suspend fun resendCode(email: String): Result<String> = try {
        val resp = api.resendCode(ResendCodeRequest(email))
        if (resp.isSuccessful) {
            Result.success(resp.body()?.string() ?: "Código reenviado.")
        } else {
            Result.failure(Exception(resp.errorBody()?.string() ?: "Error al reenviar"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("No hay conexión con el servidor"))
    }

    suspend fun forgotPassword(email: String): Result<String> = try {
        val resp = api.forgotPassword(ForgotPasswordRequest(email))
        if (resp.isSuccessful) {
            Result.success(resp.body()?.string() ?: "Si el correo esta registrado, enviamos instrucciones.")
        } else {
            Result.failure(Exception(resp.errorBody()?.string() ?: "No se pudo procesar la solicitud"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("No hay conexion con el servidor"))
    }

    suspend fun resetPassword(code: String, newPassword: String): Result<String> = try {
        val resp = api.resetPassword(ResetPasswordRequest(code, newPassword))
        if (resp.isSuccessful) {
            Result.success(resp.body()?.string() ?: "Contrasena actualizada.")
        } else {
            Result.failure(Exception(resp.errorBody()?.string() ?: "Codigo invalido o expirado"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("No hay conexion con el servidor"))
    }
}
