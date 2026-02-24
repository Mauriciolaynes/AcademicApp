package com.academicapp.ui.profesor.notas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Nota
import com.academicapp.databinding.ActivityVerNotasProfesorBinding

class VerNotasProfesorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerNotasProfesorBinding
    private lateinit var notasAdapter: NotasAdapter
    private var cursoId: Int = -1
    private lateinit var cursoNombre: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerNotasProfesorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoId = intent.getIntExtra("curso_id", -1)
        cursoNombre = intent.getStringExtra("curso_nombre") ?: "Notas del Curso"

        setupUI()
        setupRecyclerView()
        setupSwipeToRefresh()
        cargarNotas()
        setupListeners()
    }

    private fun setupUI() {
        binding.tvCursoNombre.text = cursoNombre
    }

    private fun setupRecyclerView() {
        notasAdapter = NotasAdapter()
        binding.rvNotas.apply {
            layoutManager = LinearLayoutManager(this@VerNotasProfesorActivity)
            adapter = notasAdapter
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarNotas()
        }
    }

    private fun cargarNotas() {
        binding.swipeRefreshLayout.isRefreshing = true
        // Simulando carga de datos
        Handler(Looper.getMainLooper()).postDelayed({
            // TODO: Cargar notas reales desde la BD/API para el cursoId
            val allNotasMock = listOf(
                // Notas para Matematicas (cursoId = 1)
                Nota(1, 1, "Suarez, Michael", 1, "Matemáticas", "Examen Parcial", 15.5f, "20/04/2024", 1),
                Nota(2, 2, "Casemiro, Jose", 1, "Matemáticas", "Examen Parcial", 18.0f, "20/04/2024", 1),
                Nota(3, 3, "Alba, Jordi", 1, "Matemáticas", "Examen Parcial", 12.0f, "20/04/2024", 1),
                Nota(4, 1, "Suarez, Michael", 1, "Matemáticas", "Práctica Calificada 1", 14.0f, "10/04/2024", 1),

                // Notas para Ciencias (cursoId = 2)
                Nota(5, 4, "Ramos, Sergio", 2, "Ciencias", "Laboratorio 1", 17.0f, "22/04/2024", 1),
                Nota(6, 5, "Pique, Gerard", 2, "Ciencias", "Laboratorio 1", 16.0f, "22/04/2024", 1),

                // Notas para Historia (cursoId = 3)
                Nota(7, 6, "Busquets, Sergio", 3, "Historia", "Exposición", 19.0f, "25/04/2024", 1),
                Nota(8, 7, "Iniesta, Andres", 3, "Historia", "Exposición", 20.0f, "25/04/2024", 1)
            )
            val notasDelCurso = allNotasMock.filter { it.cursoId == cursoId }
            notasAdapter.submitList(notasDelCurso)
            binding.swipeRefreshLayout.isRefreshing = false
        }, 1000)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}