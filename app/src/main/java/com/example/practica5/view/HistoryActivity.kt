package com.example.practica5.view

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practica5.R
import com.example.practica5.data.RetrofitClient
import com.example.practica5.data.SessionManager
import kotlinx.coroutines.async
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
            tvLog.text = "Cargando datos del servidor..."

            try {
                val api = RetrofitClient.myApi
                val sb = StringBuilder()

                if (role == "admin") {
                    // MODO ADMIN: Cargar TODO en paralelo
                    // Usamos async para pedir historial y favoritos a la vez
                    val historyDeferred = async { api.getHistory(myId) } // El server decide enviar todo pq soy admin
                    val favoritesDeferred = async { api.getAllFavorites() } // Nuevo endpoint

                    val history = historyDeferred.await()
                    val favorites = favoritesDeferred.await()

                    // Agrupar por usuarios (IDs únicos)
                    val allUserIds = (history.map { it.userId } + favorites.map { it.userId }).distinct().sorted()

                    sb.append("--- REPORTE GLOBAL (ADMIN) ---\n\n")

                    for (uid in allUserIds) {
                        sb.append("👤 USUARIO ID: $uid\n")
                        sb.append("====================\n")

                        // Imprimir Historial de este usuario
                        val userHistory = history.filter { it.userId == uid }
                        if (userHistory.isNotEmpty()) {
                            sb.append("🕒 Historial Búsquedas:\n")
                            userHistory.reversed().forEach {
                                sb.append("   • \"${it.query}\" (${it.timestamp})\n")
                            }
                        } else {
                            sb.append("🕒 Historial: (Vacío)\n")
                        }

                        // Imprimir Favoritos de este usuario
                        val userFavs = favorites.filter { it.userId == uid }
                        if (userFavs.isNotEmpty()) {
                            sb.append("\n⭐ Favoritos Guardados:\n")
                            userFavs.forEach {
                                sb.append("   • ${it.title} (ID: ${it.showId})\n")
                            }
                        } else {
                            sb.append("\n⭐ Favoritos: (Ninguno)\n")
                        }
                        sb.append("\n\n")
                    }

                } else {
                    // MODO USUARIO NORMAL: Solo mi historial
                    val history = api.getHistory(myId)
                    sb.append("--- MI HISTORIAL ---\n\n")
                    history.reversed().forEach {
                        sb.append("🔍 \"${it.query}\"\n")
                        sb.append("   Fecha: ${it.timestamp}\n\n")
                    }
                }

                tvLog.text = sb.toString()

            } catch (e: Exception) {
                tvLog.text = "Error al obtener datos: ${e.message}\nRevisa que el servidor esté corriendo."
            }
        }
    }
}