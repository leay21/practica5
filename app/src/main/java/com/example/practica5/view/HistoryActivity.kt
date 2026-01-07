package com.example.practica5.view

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practica5.R
import com.example.practica5.data.AppDatabase
import com.example.practica5.data.RetrofitClient
import com.example.practica5.data.SessionManager
import com.example.practica5.data.ShowRepository
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val tvLog = findViewById<TextView>(R.id.tvHistoryLog)

        // Instancia rápida del repositorio (en app real usarías ViewModel e inyección)
        val db = AppDatabase.getDatabase(this)
        val repo = ShowRepository(db.showDao(), RetrofitClient.myApi, RetrofitClient.tvMazeApi)
        val session = SessionManager(this) // Necesitamos el SessionManager
        val currentUserId = session.getUserId()

        lifecycleScope.launch {
            val historyList = repo.fetchHistory(currentUserId)
            if (historyList.isNotEmpty()) {
                val sb = StringBuilder()
                // Mostrar los últimos primero
                historyList.reversed().forEach { item ->
                    sb.append("🔍 \"${item.query}\"\n")
                    sb.append("   Usuario ID: ${item.userId}\n")
                    sb.append("   Fecha: ${item.timestamp}\n\n")
                }
                tvLog.text = sb.toString()
            } else {
                tvLog.text = "No hay historial disponible o error de conexión."
            }
        }
    }
}