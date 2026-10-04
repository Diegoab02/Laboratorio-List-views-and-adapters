package com.dieletech.backend.repository

import com.dieletech.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByVerificationToken(token: String): Optional<User>
    fun findByResetToken(token: String): Optional<User>
}