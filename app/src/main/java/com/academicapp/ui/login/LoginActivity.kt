package com.academicapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.academicapp.data.model.Rol
import com.academicapp.databinding.ActivityLoginBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.ui.alumno.AlumnoHomeActivity
import com.academicapp.ui.profesor.ProfesorHomeActivity
import com.academicapp.util.SessionManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private var rolSeleccionado: Rol = Rol.PROFESOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupTabs()
        setupListeners()
    }

    private fun setupTabs() {
        binding.tabsRol.addTab(binding.tabsRol.newTab().setText("👨‍🏫 Profesor"))
        binding.tabsRol.addTab(binding.tabsRol.newTab().setText("👨‍🎓 Alumno"))

        binding.tabsRol.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                rolSeleccionado = if (tab?.position == 0) Rol.PROFESOR else Rol.ALUMNO
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val codigo = binding.etUsuario.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (codigo.isEmpty() || password.isEmpty()) {
                mostrarError("Por favor completa todos los campos")
                return@setOnClickListener
            }

            iniciarSesion(codigo, password)
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
                    val userRole = if (receivedRolId == "1") Rol.PROFESOR else Rol.ALUMNO

                    if (userRole == rolSeleccionado) {
                        sessionManager.guardarSesion(usuario.id_usuario, usuario.nombre, userRole)
                        navegarAlHome(userRole)
                    } else {
                        // Error mejorado para mostrar el problema
                        mostrarError("Rol no coincide. App: ${rolSeleccionado.name}, API devolvió: '$receivedRolId'")
                        Log.d("LoginActivity", "Rol de API: '$receivedRolId', Rol seleccionado: '${rolSeleccionado.name}'")
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

    private fun navegarAlHome(rol: Rol) {
        val intent = if (rol == Rol.PROFESOR) {
            Intent(this, ProfesorHomeActivity::class.java)
        } else {
            Intent(this, AlumnoHomeActivity::class.java)
        }
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
