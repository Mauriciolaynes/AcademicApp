package com.academicapp.ui.profesor.notas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Curso
import com.academicapp.databinding.ActivitySeleccionarCursoNotaBinding

class SeleccionarCursoNotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionarCursoNotaBinding
    private lateinit var cursoAdapter: CursoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarCursoNotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        cargarCursos()
        setupListeners()
    }

    private fun setupRecyclerView() {
        cursoAdapter = CursoAdapter { curso ->
            val intent = Intent(this, VerNotasProfesorActivity::class.java)
            intent.putExtra("curso_id", curso.id)
            intent.putExtra("curso_nombre", curso.nombre)
            startActivity(intent)
        }
        binding.rvCursos.apply {
            layoutManager = LinearLayoutManager(this@SeleccionarCursoNotaActivity)
            adapter = cursoAdapter
        }
    }

    private fun cargarCursos() {
        // TODO: Reemplazar con la carga de cursos real desde la BD/API
        val cursosMock = listOf(
            Curso(1, "Matemáticas", "📐", 1, "3°A", 1, 28, "08:00", "10:00"),
            Curso(2, "Comunicación", "📚", 1, "3°A", 1, 28, "10:00", "12:00"),
            Curso(3, "Ciencia y Tecnología", "🔬", 1, "3°A", 1, 28, "13:00", "15:00")
        )
        cursoAdapter.submitList(cursosMock)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}