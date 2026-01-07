package com.example.practica5.model

// Para Login
data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: UserData?,
    val message: String?
)

// Para Registro (NUEVO)
data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)

// Usuario (Ahora incluye ROL)
data class UserData(
    val id: Int,
    val email: String,
    val nombre: String,
    val rol: String // "admin" o "user"
)

// Historial
data class HistoryRequest(val userId: Int, val query: String)
data class HistoryItem(val userId: Int, val query: String, val timestamp: String)