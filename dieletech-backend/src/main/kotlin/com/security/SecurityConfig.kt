package com.dieletech.backend.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtFilter: JwtFilter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it
                    // ── Publico ──
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    // Verificacion publica de certificados (HU-11)
                    .requestMatchers("/api/certificates/verify/**").permitAll()

                    // La evaluacion se declara ANTES del catalogo publico:
                    // Spring Security aplica la primera regla que coincide.
                    .requestMatchers("/api/courses/*/quiz", "/api/courses/*/quiz/**").authenticated()

                    // Tareas practicas (HU-37). Mismo motivo: si esta regla
                    // fuera despues del GET publico de /api/courses/**, el
                    // enunciado quedaria abierto a cualquiera.
                    .requestMatchers("/api/courses/*/assignments", "/api/courses/*/assignments/**").authenticated()

                    // Catalogo publico (HU-04, HU-05)
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/courses/**").permitAll()

                    // ── Requiere sesion ──
                    // La compra exige cuenta registrada (HU-06)
                    .requestMatchers("/api/purchases/**").authenticated()
                    // Progreso de lecciones (HU-07, HU-08)
                    .requestMatchers("/api/lessons/**").authenticated()
                    // Perfil del usuario (HU-14)
                    .requestMatchers("/api/users/**").authenticated()
                    // Certificados del titular (HU-11)
                    .requestMatchers("/api/certificates/**").authenticated()
                    // Tarea concreta y entregas del estudiante (HU-37, HU-38)
                    .requestMatchers("/api/assignments/**").authenticated()

                    // ── Requiere rol ──
                    // Gestion de usuarios: solo ADMIN
                    .requestMatchers("/api/admin/users/**").hasRole("ADMIN")
                    // Resto del panel: INSTRUCTOR o ADMIN (HU-09, HU-13)
                    .requestMatchers("/api/admin/**").hasAnyRole("INSTRUCTOR", "ADMIN")

                    .anyRequest().authenticated()
            }
            /*
             * Sin estos manejadores Spring Security responde 403 tanto al
             * usuario sin sesion como al que tiene sesion pero le falta el
             * rol. El cliente no puede distinguir "inicia sesion" de "no
             * tienes permiso", y termina mostrando 403 en toda la aplicacion.
             */
            .exceptionHandling {
                it.authenticationEntryPoint(unauthorizedEntryPoint())
                it.accessDeniedHandler(forbiddenHandler())
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /** No hay sesion valida: 401 para que el cliente redirija al login. */
    @Bean
    fun unauthorizedEntryPoint() = AuthenticationEntryPoint { _, response, _ ->
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            """{"status":401,"message":"Tu sesion expiro o no has iniciado sesion."}"""
        )
    }

    /** Hay sesion pero falta el rol: 403 con un mensaje que el usuario entiende. */
    @Bean
    fun forbiddenHandler() = AccessDeniedHandler { _, response, _ ->
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            """{"status":403,"message":"Tu cuenta no tiene permiso para esta accion."}"""
        )
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
