package com.academicapp.ui.profesor.asistencia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Curso
import com.academicapp.databinding.ActivitySeleccionarCursoNotaBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.ui.profesor.notas.CursoAdapter
import com.academicapp.util.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class SeleccionarCursoAsistenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionarCursoNotaBinding
    private lateinit var cursoAdapter: CursoAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarCursoNotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupRecyclerView()
        cargarCursos()
        setupListeners()
    }

    private fun setupUI() {
        // Reutilizamos el layout de notas pero cambiamos el título
        binding.layoutHeader.findViewById<android.widget.TextView>(com.academicapp.R.id.tvListaTitulo)?.text = "Seleccionar para Asistencia"
    }

    private fun setupRecyclerView() {
        cursoAdapter = CursoAdapter { curso ->
            val intent = Intent(this, MarcarAsistenciaActivity::class.java)
            intent.putExtra("curso_id", curso.id)
            intent.putExtra("curso_nombre", curso.nombre)
            startActivity(intent)
        }
        binding.rvCursos.apply {
            layoutManager = LinearLayoutManager(this@SeleccionarCursoAsistenciaActivity)
            adapter = cursoAdapter
        }
    }

    private fun cargarCursos() {
        val profesorId = sessionManager.getUserId()
        if (profesorId == -1) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCursosPorProfesor(profesorId)
                if (response.isSuccessful && response.body() != null) {
                    val cursosDesdeApi = response.body()!!
                    
                    val cursosConDetalles = cursosDesdeApi.map { cursoNet ->
                        async {
                            val alumnosResponse = RetrofitClient.instance.getAlumnosPorCurso(cursoNet.id)
                            val cantAlumnos = if (alumnosResponse.isSuccessful) alumnosResponse.body()?.size ?: 0 else 0
                            
                            Curso(
                                id = cursoNet.id,
                                nombre = cursoNet.nombre,
                                icono = obtenerIconoPorNombre(cursoNet.nombre),
                                gradoNombre = "Grado",
                                profesorId = cursoNet.profesorId,
                                cantAlumnos = cantAlumnos
                            )
                        }
                    }.awaitAll()

                    cursoAdapter.submitList(cursosConDetalles)
                } else {
                    Toast.makeText(this@SeleccionarCursoAsistenciaActivity, "Error al cargar cursos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SelCursoAsistencia", "Error de red", e)
                Toast.makeText(this@SeleccionarCursoAsistenciaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerIconoPorNombre(nombre: String): String {
        return when {
            nombre.contains("Matem", true) -> "📐"
            nombre.contains("Ciencia", true) -> "🔬"
            nombre.contains("Comunic", true) -> "📚"
            nombre.contains("Hist", true) -> "📜"
            nombre.contains("Art", true) -> "🎨"
            nombre.contains("Ingl", true) -> "🇬🇧"
            else -> "📘"
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
