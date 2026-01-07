package com.example.practica5.model

// Lo que enviamos al servidor
data class LoginRequest(
    val email: String,
    val password: String
)

// Lo que el servidor nos responde (según tu server.js)
data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: UserData?,
    val message: String? // Por si falla
)

data class UserData(
    val id: Int,
    val email: String,
    val nombre: String
)