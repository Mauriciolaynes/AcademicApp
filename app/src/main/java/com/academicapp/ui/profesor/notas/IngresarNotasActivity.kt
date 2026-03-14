package com.academicapp.ui.profesor.notas

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.databinding.ActivityIngresarNotasBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.network.model.Nota
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class IngresarNotasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIngresarNotasBinding
    private lateinit var adapter: AlumnosNotasAdapter
    private var cursoId: Int = -1
    private var cursoNombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIngresarNotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoId = intent.getIntExtra("curso_id", -1)
        cursoNombre = intent.getStringExtra("curso_nombre") ?: "Curso Seleccionado"

        setupUI()
        setupRecyclerView()
        cargarAlumnos()
        setupListeners()
    }

    private fun setupUI() {
        binding.tvCursoNombre.text = cursoNombre
        binding.tvDetalleCurso.text = "Cargando alumnos..."
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = AlumnosNotasAdapter()
        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = adapter
    }

    private fun cargarAlumnos() {
        if (cursoId == -1) {
            Toast.makeText(this, "ID de curso no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val context = this
        binding.progressNotas.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAlumnosPorCurso(cursoId)
                if (response.isSuccessful && response.body() != null) {
                    val alumnos = response.body()!!
                    val uiList = alumnos.map {
                        AlumnoNotaUI(idUsuario = it.id_usuario, nombre = "${it.nombre}")
                    }
                    adapter.submitList(uiList)
                    binding.tvDetalleCurso.text = "${uiList.size} alumnos en lista"
                } else {
                    Toast.makeText(context, "Error al cargar alumnos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("IngresarNotas", "Error de red", e)
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressNotas.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.btnGuardarNotas.setOnClickListener {
            guardarNotas()
        }
    }

    private fun guardarNotas() {
        val unidad = binding.etUnidadGlobal.text.toString()
        if (unidad.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa la unidad", Toast.LENGTH_SHORT).show()
            return
        }

        val listaNotas = adapter.currentList
        if (listaNotas.isEmpty()) return

        val context = this
        binding.progressNotas.visibility = View.VISIBLE
        binding.btnGuardarNotas.isEnabled = false

        lifecycleScope.launch {
            try {
                val resultados = listaNotas.map { item ->
                    async {
                        val notaValor = item.nota.toFloatOrNull() ?: 0f
                        val notaDTO = Nota(
                            id_nota = 0,
                            id_alumno = item.idUsuario,
                            id_curso = cursoId,
                            calificacion = notaValor,
                            unidad = unidad
                        )
                        RetrofitClient.instance.registrarNota(notaDTO)
                    }
                }.awaitAll()

                val exitosos = resultados.count { it.isSuccessful }

                binding.progressNotas.visibility = View.GONE
                binding.btnGuardarNotas.isEnabled = true

                if (exitosos > 0) {
                    Toast.makeText(context, "¡Se registraron $exitosos notas!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(context, "No se pudo guardar ninguna nota.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                binding.progressNotas.visibility = View.GONE
                binding.btnGuardarNotas.isEnabled = true
                Log.e("IngresarNotas", "Error al guardar notas", e)
                Toast.makeText(context, "Error de red al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
