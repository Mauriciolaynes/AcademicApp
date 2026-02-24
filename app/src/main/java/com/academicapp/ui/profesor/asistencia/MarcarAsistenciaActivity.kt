package com.academicapp.ui.profesor.asistencia

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Asistencia
import com.academicapp.databinding.ActivityMarcarAsistenciaBinding

class MarcarAsistenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarcarAsistenciaBinding
    private lateinit var alumnosAdapter: AlumnosAsistenciaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarcarAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            // Lógica para cuando cambia la asistencia de un alumno (opcional)
            // Por ejemplo, podrías guardar el cambio inmediatamente
        }
        binding.rvAlumnos.apply {
            layoutManager = LinearLayoutManager(this@MarcarAsistenciaActivity)
            adapter = alumnosAdapter
        }
    }

    private fun cargarDatosDelCurso() {
        // Recuperar datos del Intent
        val cursoNombre = intent.getStringExtra("curso_nombre") ?: "Curso"
        // TODO: Cargar el número de alumnos real
        val numeroAlumnos = 28

        binding.tvCursoNombre.text = cursoNombre
        binding.tvDetalleClase.text = "3°A • $numeroAlumnos alumnos"
    }

    private fun cargarAlumnos() {
        // TODO: Reemplazar con la carga de alumnos real desde la BD/API
        val alumnosMock = listOf(
            Asistencia(1, 1, "Suarez, Michael"),
            Asistencia(2, 2, "Casemiro, Jose"),
            Asistencia(3, 3, "Alba, Jordi"),
            Asistencia(4, 4, "Valverde, Federico"),
            Asistencia(5, 5, "Militao, Eder"),
            Asistencia(6, 6, "Courtois, Thibaut"),
            Asistencia(7, 7, "García, Radamel")
        )
        alumnosAdapter.submitList(alumnosMock)
    }

    private fun setupListeners() {
        binding.btnGuardarAsistencia.setOnClickListener {
            guardarAsistencia()
        }
    }

    private fun guardarAsistencia() {
        val listaActual = alumnosAdapter.currentList
        // Aquí tendrías la lógica para guardar la lista de asistencia en tu BD o API
        Toast.makeText(this, "Asistencia guardada para ${listaActual.size} alumnos", Toast.LENGTH_SHORT).show()
        finish() // Vuelve a la pantalla anterior
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
