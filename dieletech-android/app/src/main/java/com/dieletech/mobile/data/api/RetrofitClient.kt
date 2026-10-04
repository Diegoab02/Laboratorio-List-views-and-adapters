package com.dieletech.mobile.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 = "localhost" del PC visto desde el emulador de Android
    private const val BASE_URL = "http://10.0.2.2:8080/"

    /**
     * Adjunta el JWT a toda peticion que no sea de autenticacion.
     * El backend protege /api/purchases, /api/lessons, /api/users y
     * /api/admin, asi que sin esta cabecera esas llamadas responden 401.
     */
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val path = original.url.encodedPath
        val token = SessionManager.currentToken

        val request = if (!token.isNullOrBlank() && !path.startsWith("/api/auth/")) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    val api: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
