package com.example.practica5.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practica5.R
import com.example.practica5.data.RetrofitClient
import com.example.practica5.model.RegisterRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etNameRegister)
        val etEmail = findViewById<EditText>(R.id.etEmailRegister)
        val etPass = findViewById<EditText>(R.id.etPasswordRegister)
        val btnRegister = findViewById<Button>(R.id.btnRegisterConfirm)
        val progressBar = findViewById<ProgressBar>(R.id.registerProgressBar)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        // Botón Registrar
        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnRegister.isEnabled = false

                lifecycleScope.launch {
                    try {
                        val request = RegisterRequest(nombre = name, email = email, password = pass)
                        val response = RetrofitClient.myApi.register(request)

                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@RegisterActivity, "¡Registro exitoso! Por favor inicia sesión.", Toast.LENGTH_LONG).show()
                            finish() // Cierra esta pantalla y vuelve al Login
                        } else {
                            val errorMsg = response.body()?.message ?: "Error al registrar"
                            Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        progressBar.visibility = View.GONE
                        btnRegister.isEnabled = true
                    }
                }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Link para volver al Login
        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}