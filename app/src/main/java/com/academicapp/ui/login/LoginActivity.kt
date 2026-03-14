package com.academicapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.academicapp.databinding.ActivityLoginBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.ui.profesor.ProfesorHomeActivity
import com.academicapp.util.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val codigo = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (codigo.isEmpty() || password.isEmpty()) {
                mostrarError("Por favor completa todos los campos")
            } else {
                iniciarSesion(codigo, password)
            }
        }
    }

    private fun iniciarSesion(codigo: String, password: String) {
        mostrarCargando(true)
        ocultarError()

        lifecycleScope.launch {
            try {
                val credentials = mapOf("codigo" to codigo, "password" to password)
                val response = RetrofitClient.instance.login(credentials)

                mostrarCargando(false)

                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!
                    val receivedRolId = usuario.id_rol.trim()
                    
                    if (receivedRolId == "1") {
                        sessionManager.guardarSesion(usuario.id_usuario, usuario.nombre, usuario.email)
                        navegarAlHome()
                    } else {
                        mostrarError("Esta aplicación es exclusiva para profesores.")
                        Log.d("LoginActivity", "Intento de login con rol: '$receivedRolId'")
                    }

                } else {
                    mostrarError("Usuario o contraseña incorrectos")
                }
            } catch (e: IOException) {
                mostrarCargando(false)
                mostrarError("Error de conexión. Revisa tu red.")
                Log.e("LoginActivity", "Error de red", e)
            } catch (e: Exception) {
                mostrarCargando(false)
                mostrarError("Ocurrió un error inesperado.")
                Log.e("LoginActivity", "Error inesperado", e)
            }
        }
    }

    private fun navegarAlHome() {
        val intent = Intent(this, ProfesorHomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarCargando(show: Boolean) {
        binding.progressLogin.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }

    private fun mostrarError(mensaje: String) {
        binding.tvError.text = mensaje
        binding.tvError.visibility = View.VISIBLE
    }

    private fun ocultarError() {
        binding.tvError.visibility = View.GONE
    }
}
