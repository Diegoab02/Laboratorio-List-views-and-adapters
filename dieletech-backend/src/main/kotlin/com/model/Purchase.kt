package com.dieletech.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "purchases")
class Purchase(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "course_id", nullable = false)
    var courseId: Long,

    @Column(name = "user_email", nullable = false)
    var userEmail: String,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(nullable = false)
    var amount: Double,

    /** Documento de identidad del comprador (obligatorio en checkout). */
    @Column(name = "document_id", nullable = false)
    var documentId: String = "",

    /** Telefono de contacto (obligatorio en checkout). */
    @Column(nullable = false)
    var phone: String = "",

    @Column(name = "payment_method", nullable = false)
    var paymentMethod: String,

    @Column(name = "order_id", nullable = false, unique = true)
    var orderId: String,

    @Column(nullable = false)
    var status: String = "COMPLETED",

    @Column(name = "purchased_at")
    val purchasedAt: LocalDateTime = LocalDateTime.now()
)
