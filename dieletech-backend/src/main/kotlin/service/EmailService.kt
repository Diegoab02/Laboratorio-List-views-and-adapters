package com.dieletech.backend.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.url}") private val appUrl: String
) {

    fun sendVerificationCodeEmail(to: String, name: String, code: String) {
        val message = SimpleMailMessage().apply {
            setTo(to)
            subject = "Tu código de verificación — Dieletech"
            text = """
                Hola $name,

                Gracias por registrarte en Dieletech — Tu ruta hacia tu 1° en Tech.

                Tu código de verificación es:

                    $code

                Ingresa este código en la aplicación para activar tu cuenta.
                El código es válido por 24 horas.

                Si no creaste esta cuenta, ignora este correo.

                Equipo Dieletech
            """.trimIndent()
        }
        mailSender.send(message)
    }

    fun sendVerificationEmail(to: String, name: String, token: String) {
        // Legacy link-based verification (kept for web frontend compatibility)
        val link = "http://localhost:8080/api/auth/verify?token=$token"
        val message = SimpleMailMessage().apply {
            setTo(to)
            subject = "Verifica tu cuenta en Dieletech"
            text = """
                Hola $name,

                Gracias por registrarte en Dieletech — Tu ruta hacia tu 1° en Tech.

                Haz clic en el siguiente enlace para verificar tu cuenta:
                $link

                Este enlace estará disponible por 24 horas.

                Equipo Dieletech
            """.trimIndent()
        }
        mailSender.send(message)
    }

    fun sendPasswordResetEmail(to: String, name: String, code: String) {
        val message = SimpleMailMessage().apply {
            setTo(to)
            subject = "Recupera tu contraseña — Dieletech"
            text = """
                Hola $name,

                Recibimos una solicitud para restablecer tu contraseña.

                Tu código de recuperación es:

                    $code

                Ingresa este código en la aplicación para crear una nueva contraseña.
                El código expira en 30 minutos.

                Desde la web puedes ir directo a:
                $appUrl/reset-password?email=$to

                Si no solicitaste esto, ignora este correo.

                Equipo Dieletech
            """.trimIndent()
        }
        mailSender.send(message)
    }
}
