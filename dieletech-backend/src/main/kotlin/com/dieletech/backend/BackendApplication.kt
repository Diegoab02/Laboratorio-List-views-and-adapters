package com.dieletech.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.dieletech.backend", "com.controller", "com.dto", "com.model", "com.repository", "com.security", "com.config", "service"])
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}