package com.example.practica5.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practica5.R
import com.example.practica5.data.RetrofitClient
import com.example.practica5.data.SessionManager
import com.example.practica5.model.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Verificar si ya estamos logueados
        val session = SessionManager(this)
        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val progressBar = findViewById<ProgressBar>(R.id.loginProgressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnLogin.isEnabled = false

                // Llamada a la API en segundo plano
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.myApi.login(LoginRequest(email, pass))
                        if (response.isSuccessful && response.body()?.success == true) {
                            val data = response.body()!!
                            // GUARDAMOS LA SESIÓN
                            session.saveUserSession(data.user!!.id, data.token!!, data.user.nombre)
                            Toast.makeText(this@LoginActivity, "Bienvenido ${data.user.nombre}", Toast.LENGTH_SHORT).show()
                            goToMain()
                        } else {
                            Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                    }
                }
            } else {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Cierra el Login para que no se pueda volver atrás
    }
}