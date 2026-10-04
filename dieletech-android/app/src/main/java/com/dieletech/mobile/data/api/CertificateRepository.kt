package com.dieletech.mobile.data.api

import com.dieletech.mobile.data.model.CertificateNet
import com.dieletech.mobile.data.model.CertificateVerificationNet

/** HU-12 · Certificados emitidos y verificación pública. */
object CertificateRepository {
    private val api = RetrofitClient.api

    suspend fun mine(): List<CertificateNet> = try {
        api.myCertificates()
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun verify(code: String): CertificateVerificationNet? = try {
        api.verifyCertificate(code.trim().uppercase())
    } catch (_: Exception) {
        null
    }
}
