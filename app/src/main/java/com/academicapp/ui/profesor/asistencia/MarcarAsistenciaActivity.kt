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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MarcarAsistenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarcarAsistenciaBinding
    private lateinit var alumnosAdapter: AlumnosAsistenciaAdapter
    private var cursoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarcarAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoId = intent.getIntExtra("curso_id", -1)

        setupUI()
        setupRecyclerView()
        cargarDatosDelCurso()
        cargarAlumnosYEstados()
        setupListeners()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        alumnosAdapter = AlumnosAsistenciaAdapter { }
        binding.rvAlumnos.apply {
            layoutManager = LinearLayoutManager(this@MarcarAsistenciaActivity)
            adapter = alumnosAdapter
        }
    }

    private fun cargarDatosDelCurso() {
        val cursoNombre = intent.getStringExtra("curso_nombre") ?: "Curso"
        binding.tvCursoNombre.text = cursoNombre
        binding.tvDetalleClase.text = "Sincronizando con servidor..."
    }

    private fun cargarAlumnosYEstados() {
        if (cursoId == -1) return
        binding.progressAsistencia.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resAlumnos = RetrofitClient.instance.getAlumnosPorCurso(cursoId)
                
                if (resAlumnos.isSuccessful && resAlumnos.body() != null) {
                    val alumnosApi = resAlumnos.body()!!

                    val listaFinal = alumnosApi.map { alumno ->
                        async {
                            val resAsis = RetrofitClient.instance.visualizarAsistencia(cursoId, alumno.id_usuario)
                            
                            val estadoCargado = if (resAsis.isSuccessful && resAsis.body() != null) {
                                val body = resAsis.body()!!
                                when {
                                    body.observacion == "TARDANZA" -> EstadoAsistencia.TARDANZA
                                    body.asistio -> EstadoAsistencia.PRESENTE
                                    else -> EstadoAsistencia.AUSENTE
                                }
                            } else {
                                EstadoAsistencia.PRESENTE
                            }

                            Asistencia(
                                alumnoId = alumno.id_usuario,
                                alumnoNombre = alumno.nombre,
                                cursoId = cursoId,
                                estado = estadoCargado
                            )
                        }
                    }.awaitAll()

                    alumnosAdapter.submitList(listaFinal)
                    binding.tvDetalleClase.text = "${listaFinal.size} alumnos en lista"
                }
            } catch (e: Exception) {
                Log.e("MarcarAsistencia", "Error al cargar", e)
                Toast.makeText(this@MarcarAsistenciaActivity, "Error de conexión al cargar", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressAsistencia.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.btnGuardarAsistencia.setOnClickListener {
            guardarCambiosUnoPorUno()
        }
    }

    private fun guardarCambiosUnoPorUno() {
        val listaAsistencia = alumnosAdapter.currentList
        if (listaAsistencia.isEmpty()) return

        binding.progressAsistencia.visibility = View.VISIBLE
        binding.btnGuardarAsistencia.isEnabled = false

        lifecycleScope.launch {
            try {
                val resultados = listaAsistencia.map { item ->
                    async {
                        val asisDTO = com.academicapp.network.model.Asistencia(
                            id_clase = cursoId,
                            id_alumno = item.alumnoId,
                            asistio = item.estado == EstadoAsistencia.PRESENTE || item.estado == EstadoAsistencia.TARDANZA,
                            observacion = item.estado.name
                        )

                        // 1. Intentamos registrar (POST asistencias)
                        val resPost = RetrofitClient.instance.registrarAsistencia(asisDTO)
                        
                        // Si el servidor responde "Error" en el body (ya existe) o falla el POST, intentamos EDITAR (PUT asistencias)
                        if (resPost.isSuccessful && resPost.body()?.get("res") == "Error") {
                            RetrofitClient.instance.editarAsistencia(asisDTO)
                        } else if (!resPost.isSuccessful) {
                            RetrofitClient.instance.editarAsistencia(asisDTO)
                        } else {
                            resPost
                        }
                    }
                }.awaitAll()

                val errores = resultados.count { !it.isSuccessful }

                binding.progressAsistencia.visibility = View.GONE
                binding.btnGuardarAsistencia.isEnabled = true

                if (errores == 0) {
                    Toast.makeText(this@MarcarAsistenciaActivity, "¡Asistencia sincronizada correctamente!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@MarcarAsistenciaActivity, "Se guardó con $errores errores", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                binding.progressAsistencia.visibility = View.GONE
                binding.btnGuardarAsistencia.isEnabled = true
                Log.e("Asistencia", "Error al guardar", e)
                Toast.makeText(this@MarcarAsistenciaActivity, "Error de red al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
