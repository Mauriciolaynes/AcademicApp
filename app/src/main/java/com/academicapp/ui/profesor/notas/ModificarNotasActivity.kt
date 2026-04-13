package com.academicapp.ui.profesor.notas

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.databinding.ActivityModificarNotasBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.network.model.Nota
import kotlinx.coroutines.launch
import java.util.Locale

class ModificarNotasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModificarNotasBinding
    private lateinit var adapter: NotasAdapter
    private var cursoId: Int = -1
    private var cursoNombre: String = ""
    private var alumnosMap = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModificarNotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoId = intent.getIntExtra("curso_id", -1)
        cursoNombre = intent.getStringExtra("curso_nombre") ?: "Curso Seleccionado"

        setupUI()
        setupRecyclerView()
        cargarDatos()
    }

    private fun setupUI() {
        binding.tvCursoNombre.text = cursoNombre
        binding.tvDetalleCurso.text = "Cargando notas..."
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = NotasAdapter(
            onEdit = { nota -> showEditDialog(nota) },
            onDelete = { nota -> confirmarEliminacion(nota) }
        )
        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = adapter
    }

    private fun cargarDatos() {
        if (cursoId == -1) return
        
        val context = this
        binding.progressNotas.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val resAlumnos = RetrofitClient.instance.getAlumnosPorCurso(cursoId)
                if (resAlumnos.isSuccessful) {
                    resAlumnos.body()?.forEach { 
                        alumnosMap[it.id_usuario] = "${it.nombre}"
                    }
                }

                val response = RetrofitClient.instance.getNotas()
                if (response.isSuccessful && response.body() != null) {
                    val notasDelCurso = response.body()!!.filter { it.id_curso == cursoId }
                    
                    adapter.setAlumnosMap(alumnosMap)
                    adapter.submitList(notasDelCurso)
                    
                    binding.tvDetalleCurso.text = "${notasDelCurso.size} notas encontradas"
                    
                    if (notasDelCurso.isEmpty()) {
                        Toast.makeText(context, "No hay notas para este curso", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Error al obtener notas del servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ModificarNotas", "Error de red", e)
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressNotas.visibility = View.GONE
            }
        }
    }

    private fun showEditDialog(nota: Nota) {
        val dialogView = layoutInflater.inflate(com.academicapp.R.layout.dialog_edit_nota, null)
        val etCalificacion = dialogView.findViewById<EditText>(com.academicapp.R.id.etCalificacion)
        val etUnidad = dialogView.findViewById<EditText>(com.academicapp.R.id.etUnidad)

        // Filtro estricto: 0.0 - 20.0 y máximo 1 decimal
        val filter = InputFilter { source, start, end, dest, dstart, dend ->
            val nuevoTexto = dest.subSequence(0, dstart).toString() + 
                             source.subSequence(start, end) + 
                             dest.subSequence(dend, dest.length)
            
            if (nuevoTexto.isEmpty()) return@InputFilter null
            
            // Regex: Máximo 2 dígitos enteros, punto opcional y máximo 1 decimal
            if (!nuevoTexto.matches(Regex("^\\d{0,2}(\\.\\d{0,1})?$"))) return@InputFilter ""
            
            // Rango numérico
            val valor = nuevoTexto.toDoubleOrNull() ?: return@InputFilter ""
            if (valor > 20.0) return@InputFilter ""
            
            null
        }

        etCalificacion.filters = arrayOf(filter)
        
        // Ajustar valor inicial si es inválido (ej. 21.0) y formatear a 1 decimal
        val valorSeguro = if (nota.calificacion > 20f) 20.0f else nota.calificacion
        etCalificacion.setText(String.format(Locale.US, "%.1f", valorSeguro))
        
        etUnidad.setText(nota.unidad)

        AlertDialog.Builder(this)
            .setTitle("Modificar Nota de ${alumnosMap[nota.id_alumno] ?: "Alumno"}")
            .setView(dialogView)
            .setPositiveButton("Guardar Cambios") { _, _ ->
                val nuevaCal = etCalificacion.text.toString().toFloatOrNull() ?: valorSeguro
                val nuevaUni = etUnidad.text.toString().ifBlank { nota.unidad }
                actualizarNota(nota.copy(calificacion = nuevaCal, unidad = nuevaUni))
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarNota(nota: Nota) {
        val context = this
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.editarNota(nota)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Nota actualizada!", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                } else {
                    Toast.makeText(context, "Error al actualizar en el servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red al actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminacion(nota: Nota) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Nota")
            .setMessage("¿Estás seguro de eliminar esta nota?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarNota(nota.id_nota) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarNota(id: Int) {
        val context = this
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.eliminarNota(id)
                if (res.isSuccessful) {
                    Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
