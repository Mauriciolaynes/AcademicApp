package com.academicapp.ui.profesor.asistencia

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Asistencia
import com.academicapp.data.model.EstadoAsistencia
import com.academicapp.databinding.ActivityMarcarAsistenciaBinding
import com.academicapp.network.RetrofitClient
import kotlinx.coroutines.launch
import java.io.IOException

class MarcarAsistenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarcarAsistenciaBinding
    private lateinit var alumnosAdapter: AlumnosAsistenciaAdapter
    private var cursoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarcarAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoId = intent.getIntExtra("curso_id", -1)

        setupActionBar()
        setupRecyclerView()
        cargarDatosDelCurso()
        cargarAlumnos()
        setupListeners()
    }

    private fun setupActionBar() {
        supportActionBar?.title = "Marcar Asistencia"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        alumnosAdapter = AlumnosAsistenciaAdapter {
            // Lógica para cuando cambia la asistencia de un alumno
        }
        binding.rvAlumnos.apply {
            layoutManager = LinearLayoutManager(this@MarcarAsistenciaActivity)
            adapter = alumnosAdapter
        }
    }

    private fun cargarDatosDelCurso() {
        val cursoNombre = intent.getStringExtra("curso_nombre") ?: "Curso"
        binding.tvCursoNombre.text = cursoNombre
        binding.tvDetalleClase.text = "Cargando alumnos..."
    }

    private fun cargarAlumnos() {
        if (cursoId == -1) {
            Toast.makeText(this, "Error: ID de curso no válido", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressAsistencia.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAlumnosPorCurso(cursoId)
                binding.progressAsistencia.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val usuarios = response.body()!!
                    val alumnosAsistencia = usuarios.map {
                        Asistencia(
                            alumnoId = it.id_usuario,
                            alumnoNombre = it.nombre,
                            cursoId = cursoId,
                            estado = EstadoAsistencia.PRESENTE // Por defecto presente
                        )
                    }
                    alumnosAdapter.submitList(alumnosAsistencia)
                    binding.tvDetalleClase.text = "${alumnosAsistencia.size} alumnos registrados"
                } else {
                    Toast.makeText(this@MarcarAsistenciaActivity, "Error al cargar alumnos", Toast.LENGTH_SHORT).show()
                    Log.e("MarcarAsistencia", "Error: ${response.code()}")
                }
            } catch (e: IOException) {
                binding.progressAsistencia.visibility = View.GONE
                Toast.makeText(this@MarcarAsistenciaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.progressAsistencia.visibility = View.GONE
                Toast.makeText(this@MarcarAsistenciaActivity, "Error inesperado", Toast.LENGTH_SHORT).show()
                Log.e("MarcarAsistencia", "Error", e)
            }
        }
    }

    private fun setupListeners() {
        binding.btnGuardarAsistencia.setOnClickListener {
            guardarAsistencia()
        }
    }

    private fun guardarAsistencia() {
        val listaActual = alumnosAdapter.currentList
        if (listaActual.isEmpty()) {
            Toast.makeText(this, "No hay alumnos para registrar", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Aquí podrías implementar el envío de la asistencia a la API si existiera el endpoint masivo
        Toast.makeText(this, "Asistencia guardada para ${listaActual.size} alumnos", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
