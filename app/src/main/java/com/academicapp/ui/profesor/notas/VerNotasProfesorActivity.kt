package com.academicapp.ui.profesor.notas

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.databinding.ActivityVerNotasProfesorBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.network.model.Nota
import kotlinx.coroutines.launch

class VerNotasProfesorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerNotasProfesorBinding
    private lateinit var notasAdapter: NotasAdapter
    private var cursoId: Int = -1
    private var alumnosMap = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerNotasProfesorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoId = intent.getIntExtra("curso_id", -1)
        val cursoNombre = intent.getStringExtra("curso_nombre") ?: "Notas"
        binding.tvCursoNombre.text = cursoNombre

        setupRecyclerView()
        setupSwipeToRefresh()
        cargarDatos()
        
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        notasAdapter = NotasAdapter(
            onEdit = { nota -> showEditDialog(nota) },
            onDelete = { nota -> confirmarEliminacion(nota) }
        )
        binding.rvNotas.apply {
            layoutManager = LinearLayoutManager(this@VerNotasProfesorActivity)
            adapter = notasAdapter
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener { cargarDatos() }
    }

    private fun cargarDatos() {
        binding.swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            try {
                // 1. Cargamos alumnos para los nombres
                val resAlu = RetrofitClient.instance.getAlumnosPorCurso(cursoId)
                if (resAlu.isSuccessful) resAlu.body()?.forEach { alumnosMap[it.id_usuario] = it.nombre }

                // 2. Cargamos TODAS las notas del servidor usando el nuevo método GET /notas
                val response = RetrofitClient.instance.getNotas()
                if (response.isSuccessful && response.body() != null) {
                    // 3. Filtramos por cursoId para mostrar solo las de este curso
                    val notasDelCurso = response.body()!!.filter { it.id_curso == cursoId }
                    
                    notasAdapter.setAlumnosMap(alumnosMap)
                    notasAdapter.submitList(notasDelCurso)
                    
                    if (notasDelCurso.isEmpty()) {
                        Toast.makeText(this@VerNotasProfesorActivity, "No hay notas para este curso", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("VerNotas", "Error: ${response.code()}")
                    Toast.makeText(this@VerNotasProfesorActivity, "Error al cargar datos del servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("VerNotas", "Error de red", e)
                Toast.makeText(this@VerNotasProfesorActivity, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showEditDialog(nota: Nota) {
        val dialogView = layoutInflater.inflate(com.academicapp.R.layout.dialog_edit_nota, null)
        val etCal = dialogView.findViewById<EditText>(com.academicapp.R.id.etCalificacion)
        val etUni = dialogView.findViewById<EditText>(com.academicapp.R.id.etUnidad)

        etCal.setText(nota.calificacion.toString())
        etUni.setText(nota.unidad)

        AlertDialog.Builder(this)
            .setTitle("Modificar Nota")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val nCal = etCal.text.toString().toFloatOrNull() ?: nota.calificacion
                val nUni = etUni.text.toString()
                actualizarNota(nota.copy(calificacion = nCal, unidad = nUni))
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarNota(nota: Nota) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.editarNota(nota)
                if (res.isSuccessful) {
                    Toast.makeText(this@VerNotasProfesorActivity, "Actualizado correctamente", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                }
            } catch (e: Exception) {
                Toast.makeText(this@VerNotasProfesorActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminacion(nota: Nota) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Nota")
            .setMessage("¿Deseas eliminar esta nota permanentemente?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarNota(nota.id_nota) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarNota(id: Int) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.instance.eliminarNota(id)
                if (res.isSuccessful) {
                    Toast.makeText(this@VerNotasProfesorActivity, "Nota eliminada", Toast.LENGTH_SHORT).show()
                    cargarDatos()
                }
            } catch (e: Exception) {
                Toast.makeText(this@VerNotasProfesorActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
