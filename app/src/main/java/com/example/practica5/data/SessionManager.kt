package com.example.practica5.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    fun saveUserSession(userId: Int, token: String, name: String, role: String) {
        val editor = prefs.edit()
        editor.putInt("USER_ID", userId)
        editor.putString("AUTH_TOKEN", token)
        editor.putString("USER_NAME", name)
        editor.putString("USER_ROLE", role)
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("USER_ID", -1) // -1 si no hay usuario
    }

    fun getUserName(): String? {
        return prefs.getString("USER_NAME", "Usuario")
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
    fun getUserRole(): String {
        return prefs.getString("USER_ROLE", "user") ?: "user"
    }
}