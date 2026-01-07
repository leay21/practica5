package com.example.practica5.view

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practica5.R
import com.example.practica5.data.RetrofitClient
import com.example.practica5.data.SessionManager
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val tvLog = findViewById<TextView>(R.id.tvHistoryLog)
        val session = SessionManager(this)
        val role = session.getUserRole()
        val myId = session.getUserId()

        lifecycleScope.launch {
            tvLog.text = "Cargando datos..."

            try {
                val api = RetrofitClient.myApi
                val sb = StringBuilder()

                if (role == "admin") {
                    // MODO ADMIN: Ejecutamos secuencial para evitar crashes de concurrencia simple
                    sb.append("--- MODO ADMINISTRADOR ---\n\n")

                    // 1. Obtener Historial Global
                    val history = try {
                        api.getHistory(myId)
                    } catch (e: Exception) {
                        sb.append("⚠️ Error cargando historial: ${e.message}\n")
                        emptyList()
                    }

                    // 2. Obtener Favoritos Globales
                    val favorites = try {
                        api.getAllFavorites()
                    } catch (e: Exception) {
                        // Si falla este endpoint, no crasheamos, solo avisamos
                        sb.append("⚠️ Error cargando favoritos (revisa server.js): ${e.message}\n")
                        emptyList()
                    }

                    // 3. Agrupar y Mostrar
                    val allUserIds = (history.map { it.userId } + favorites.map { it.userId }).distinct().sorted()

                    if (allUserIds.isEmpty()) {
                        sb.append("No hay datos registrados en el servidor.")
                    }

                    for (uid in allUserIds) {
                        sb.append("👤 USUARIO ID: $uid\n")
                        sb.append("====================\n")

                        val userFavs = favorites.filter { it.userId == uid }
                        if (userFavs.isNotEmpty()) {
                            sb.append("⭐ Favoritos (${userFavs.size}):\n")
                            userFavs.forEach { f -> sb.append("   - ${f.title}\n") }
                        } else {
                            sb.append("⭐ Favoritos: Ninguno\n")
                        }

                        sb.append("\n")

                        val userHistory = history.filter { it.userId == uid }
                        if (userHistory.isNotEmpty()) {
                            sb.append("🕒 Historial (${userHistory.size}):\n")
                            userHistory.reversed().forEach { h -> sb.append("   - ${h.query} (${h.timestamp})\n") }
                        } else {
                            sb.append("🕒 Historial: Vacío\n")
                        }
                        sb.append("\n--------------------\n\n")
                    }

                } else {
                    // MODO USUARIO
                    val history = api.getHistory(myId)
                    sb.append("--- MI HISTORIAL ---\n\n")
                    if (history.isEmpty()) sb.append("No tienes búsquedas recientes.")

                    history.reversed().forEach {
                        sb.append("🔍 ${it.query}\n")
                        sb.append("   ${it.timestamp}\n\n")
                    }
                }

                tvLog.text = sb.toString()

            } catch (e: Exception) {
                e.printStackTrace()
                tvLog.text = "Error crítico de conexión:\n${e.localizedMessage}"
                Toast.makeText(this@HistoryActivity, "Error al conectar con servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }
}