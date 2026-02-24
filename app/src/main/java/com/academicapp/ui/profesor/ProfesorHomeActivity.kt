package com.academicapp.ui.profesor

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            startActivity(Intent(this, MarcarAsistenciaActivity::class.java))
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
                val intent = Intent(this, VerNotasProfesorActivity::class.java).apply {
                    putExtra("curso_id", selectedCourse.id)
                    putExtra("curso_nombre", selectedCourse.nombre)
                }
                startActivity(intent)
            }
            .create().show()
    }

    private fun cargarCursosDesdeApi() {
        val profesorId = sessionManager.getUserId()
        if (profesorId == -1) {
            Toast.makeText(this, "Error: ID de profesor no encontrado", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCursosPorProfesor(profesorId)
                if (response.isSuccessful && response.body() != null) {
                    val cursosDesdeApi = response.body()!!
                    // Mapeamos el modelo de red al modelo de la UI
                    val cursosParaUi = cursosDesdeApi.map {
                        Curso(
                            id = it.id,
                            nombre = it.nombre,
                            icono = "📚", // Icono por defecto
                            gradoNombre = "", // Dato no disponible en este endpoint
                            profesorId = it.profesorId
                        )
                    }
                    cursosAdapter.submitList(cursosParaUi)
                } else {
                    Toast.makeText(this@ProfesorHomeActivity, "Error al cargar los cursos", Toast.LENGTH_SHORT).show()
                    Log.e("ProfesorHome", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                Toast.makeText(this@ProfesorHomeActivity, "Error de conexión. Verifique su red.", Toast.LENGTH_SHORT).show()
                Log.e("ProfesorHome", "Error de red", e)
            } catch (e: Exception) {
                Toast.makeText(this@ProfesorHomeActivity, "Ocurrió un error inesperado", Toast.LENGTH_SHORT).show()
                Log.e("ProfesorHome", "Error inesperado", e)
            }
        }
        // TODO: Reemplazar con datos reales
        binding.tvCantidadPendientes.text = "3"
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("es", "PE"))
        return sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }
}
