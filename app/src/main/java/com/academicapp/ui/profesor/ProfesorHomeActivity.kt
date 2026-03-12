package com.academicapp.ui.profesor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Curso
import com.academicapp.databinding.ActivityProfesorHomeBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.ui.profesor.asistencia.MarcarAsistenciaActivity
import com.academicapp.ui.profesor.justificacion.GestionJustificacionActivity
import com.academicapp.ui.profesor.notas.IngresarNotasActivity
import com.academicapp.ui.profesor.notas.ModificarNotasActivity
import com.academicapp.ui.profesor.notas.VerNotasProfesorActivity
import com.academicapp.util.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfesorHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfesorHomeBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var cursosAdapter: CursosProfesorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfesorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupRecyclerView()
        setupListeners()
        cargarCursosDesdeApi()
    }

    private fun setupUI() {
        binding.tvNombreProfesor.text = "${sessionManager.getNombre()} 👋"
        binding.tvFecha.text = "📅 ${obtenerFechaActual()}"
    }

    private fun setupRecyclerView() {
        cursosAdapter = CursosProfesorAdapter { curso ->
            val intent = Intent(this, MarcarAsistenciaActivity::class.java)
            intent.putExtra("curso_id", curso.id)
            intent.putExtra("curso_nombre", curso.nombre)
            startActivity(intent)
        }
        binding.rvCursos.apply {
            layoutManager = LinearLayoutManager(this@ProfesorHomeActivity)
            adapter = cursosAdapter
        }
    }

    private fun setupListeners() {
        binding.btnMarcarAsistencia.setOnClickListener {
            // Podríamos abrir directamente el primer curso o mostrar el diálogo
            showCourseSelectionDialog()
        }
        binding.btnIngresarNotas.setOnClickListener {
            startActivity(Intent(this, IngresarNotasActivity::class.java))
        }
        binding.btnModificarNotas.setOnClickListener {
            startActivity(Intent(this, ModificarNotasActivity::class.java))
        }
        binding.btnVerNotas.setOnClickListener {
            showCourseSelectionDialog()
        }
        binding.cardPendientes.setOnClickListener {
            startActivity(Intent(this, GestionJustificacionActivity::class.java))
        }
    }

    private fun showCourseSelectionDialog() {
        val cursos = cursosAdapter.currentList
        if (cursos.isEmpty()) {
            Toast.makeText(this, "No hay cursos para mostrar", Toast.LENGTH_SHORT).show()
            return
        }
        val courseNames = cursos.map { it.nombre }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccione un curso")
            .setItems(courseNames) { _, which ->
                val selectedCourse = cursos[which]
                val intent = Intent(this, MarcarAsistenciaActivity::class.java).apply {
                    putExtra("curso_id", selectedCourse.id)
                    putExtra("curso_nombre", selectedCourse.nombre)
                }
                startActivity(intent)
            }
            .create().show()
    }

    private fun cargarCursosDesdeApi() {
        val profesorId = sessionManager.getUserId()
        if (profesorId == -1) return

        binding.progressHome.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCursosPorProfesor(profesorId)
                if (response.isSuccessful && response.body() != null) {
                    val cursosDesdeApi = response.body()!!
                    
                    // Para obtener el número real de alumnos, consultamos cada curso
                    val cursosConDetalles = cursosDesdeApi.map { cursoNet ->
                        async {
                            val alumnosResponse = RetrofitClient.instance.getAlumnosPorCurso(cursoNet.id)
                            val cantAlumnos = if (alumnosResponse.isSuccessful) alumnosResponse.body()?.size ?: 0 else 0
                            
                            Curso(
                                id = cursoNet.id,
                                nombre = cursoNet.nombre,
                                icono = obtenerIconoPorNombre(cursoNet.nombre),
                                gradoNombre = "Grado", // Podría venir de la API en el futuro
                                profesorId = cursoNet.profesorId,
                                cantAlumnos = cantAlumnos
                            )
                        }
                    }.awaitAll()

                    cursosAdapter.submitList(cursosConDetalles)
                } else {
                    Log.e("ProfesorHome", "Error cargando cursos: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ProfesorHome", "Error en cargarCursosDesdeApi", e)
            } finally {
                binding.progressHome.visibility = View.GONE
            }
        }
        binding.tvCantidadPendientes.text = "3"
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

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("es", "PE"))
        return sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }
}
