package com.dieletech.mobile.data.api

import android.content.Context

object SessionManager {
    private const val PREF = "dieletech_session"

    /**
     * Copia en memoria del token para que el interceptor de OkHttp pueda
     * adjuntar el Authorization sin depender de un Context.
     */
    @Volatile
    var currentToken: String? = null
        private set

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun save(context: Context, token: String, name: String, email: String, role: String = "STUDENT") {
        prefs(context).edit()
            .putString("token", token)
            .putString("name", name)
            .putString("email", email)
            .putString("role", role)
            .apply()
        currentToken = token
    }

    /** Rehidrata el token en memoria al abrir la app. */
    fun restore(context: Context) {
        currentToken = prefs(context).getString("token", null)
    }

    fun token(context: Context): String? = prefs(context).getString("token", null)
    fun name(context: Context): String = prefs(context).getString("name", "") ?: ""
    fun email(context: Context): String = prefs(context).getString("email", "") ?: ""
    fun role(context: Context): String = prefs(context).getString("role", "STUDENT") ?: "STUDENT"
    fun isLoggedIn(context: Context): Boolean = !token(context).isNullOrBlank()

    fun isStudent(context: Context): Boolean = role(context) == "STUDENT"
    fun isInstructor(context: Context): Boolean = role(context) == "INSTRUCTOR"
    fun isAdmin(context: Context): Boolean = role(context) == "ADMIN"

    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
        currentToken = null
    }
}
