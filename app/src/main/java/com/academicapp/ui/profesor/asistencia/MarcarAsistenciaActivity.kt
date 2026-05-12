package com.academicapp.ui.profesor.asistencia

import android.app.DatePickerDialog
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
    private val calendar = Calendar.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var fechaSeleccionada: String = sdf.format(Date())

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
        binding.tvFechaSeleccionada.text = "Fecha: $fechaSeleccionada"
    }

    private fun setupRecyclerView() {
        alumnosAdapter = AlumnosAsistenciaAdapter { }
        binding.rvAlumnos.layoutManager = LinearLayoutManager(this)
        binding.rvAlumnos.adapter = alumnosAdapter
    }

    private fun cargarDatosDelCurso() {
        val cursoNombre = intent.getStringExtra("curso_nombre") ?: "Curso"
        binding.tvCursoNombre.text = cursoNombre
        binding.tvDetalleClase.text = "Sincronizando con servidor..."
    }

    private fun setupListeners() {
        binding.btnGuardarAsistencia.setOnClickListener {
            guardarCambiosUnoPorUno()
        }

        binding.btnCalendario.setOnClickListener {
            showDatePicker()
        }

        binding.layoutFecha.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                fechaSeleccionada = sdf.format(calendar.time)
                binding.tvFechaSeleccionada.text = "Fecha: $fechaSeleccionada"
                cargarAlumnosYEstados()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Restringir para que no se pueda seleccionar fechas futuras
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun cargarAlumnosYEstados() {
        if (cursoId == -1) return
        
        val context = this
        binding.progressAsistencia.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // 1. Obtener lista de alumnos del curso
                val resAlumnos = RetrofitClient.instance.getAlumnosPorCurso(cursoId)
                
                // 2. Obtener TODA la asistencia de este curso (nuevo endpoint optimizado)
                val resAsistencias = RetrofitClient.instance.getAsistenciasPorClase(cursoId)

                if (resAlumnos.isSuccessful && resAlumnos.body() != null) {
                    val alumnosApi = resAlumnos.body()!!
                    val asistenciasApi = resAsistencias.body() ?: emptyList()

                    val listaFinal = alumnosApi.map { alumno ->
                        // Buscamos si el alumno tiene asistencia registrada para la fecha seleccionada
                        val asistenciaHoy = asistenciasApi.find { 
                            it.id_alumno == alumno.id_usuario && it.fecha.startsWith(fechaSeleccionada) 
                        }
                        
                        val estadoCargado = if (asistenciaHoy != null) {
                            val tipo = asistenciaHoy.tipo.uppercase().trim()
                            when {
                                tipo.contains("ASIST") || tipo == "P" || tipo.contains("PRESENTE") -> EstadoAsistencia.ASISTIO
                                tipo.contains("FALT") || tipo == "F" || tipo.contains("FALTA") -> EstadoAsistencia.FALTO
                                tipo.contains("TARD") || tipo == "T" || tipo.contains("TARDE") -> EstadoAsistencia.TARDE
                                else -> EstadoAsistencia.SIN_MARCAR
                            }
                        } else {
                            EstadoAsistencia.SIN_MARCAR
                        }

                        Asistencia(
                            id = asistenciaHoy?.id_asistencia ?: 0, // Guardamos el ID real de la DB
                            alumnoId = alumno.id_usuario,
                            alumnoNombre = alumno.nombre,
                            cursoId = cursoId,
                            fecha = fechaSeleccionada,
                            estado = estadoCargado
                        )
                    }

                    alumnosAdapter.submitList(listaFinal)
                    binding.tvDetalleClase.text = "${listaFinal.size} alumnos en lista"
                }
            } catch (e: Exception) {
                Log.e("MarcarAsistencia", "Error al cargar", e)
                Toast.makeText(context, "Error de conexión al cargar", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressAsistencia.visibility = View.GONE
            }
        }
    }

    private fun guardarCambiosUnoPorUno() {
        val listaAsistencia = alumnosAdapter.currentList.filter { it.estado != EstadoAsistencia.SIN_MARCAR }
        
        if (listaAsistencia.isEmpty()) {
            Toast.makeText(this, "Por favor, marca la asistencia de al menos un alumno", Toast.LENGTH_SHORT).show()
            return
        }

        val context = this
        binding.progressAsistencia.visibility = View.VISIBLE
        binding.btnGuardarAsistencia.isEnabled = false

        lifecycleScope.launch {
            try {
                val resultados = listaAsistencia.map { item ->
                    async {
                        if (item.id > 0) {
                            // Si ya tiene ID, es una actualización (PUT)
                            val asisDTO = com.academicapp.network.model.Asistencia(
                                id_asistencia = item.id,
                                id_clase = item.cursoId,
                                id_alumno = item.alumnoId,
                                tipo = item.estado.name,
                                observacion = "Actualizado: $fechaSeleccionada",
                                fecha = fechaSeleccionada
                            )
                            RetrofitClient.instance.editarAsistencia(asisDTO)
                        } else {
                            // Si no tiene ID, es un nuevo registro (POST)
                            val asisDTO = com.academicapp.network.model.Asistencia(
                                id_clase = item.cursoId,
                                id_alumno = item.alumnoId,
                                tipo = item.estado.name,
                                observacion = "Nuevo Registro: $fechaSeleccionada",
                                fecha = fechaSeleccionada
                            )
                            val res = RetrofitClient.instance.registrarAsistencia(asisDTO)
                            // Retornamos un Response genérico para el conteo de errores
                            if (res.isSuccessful) retrofit2.Response.success(Unit) 
                            else retrofit2.Response.error(res.code(), res.errorBody()!!)
                        }
                    }
                }.awaitAll()

                val errores = resultados.count { !it.isSuccessful }

                binding.progressAsistencia.visibility = View.GONE
                binding.btnGuardarAsistencia.isEnabled = true

                if (errores == 0) {
                    Toast.makeText(context, "¡Asistencia sincronizada correctamente!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(context, "Se guardó con $errores errores", Toast.LENGTH_SHORT).show()
                    cargarAlumnosYEstados()
                }

            } catch (e: Exception) {
                binding.progressAsistencia.visibility = View.GONE
                binding.btnGuardarAsistencia.isEnabled = true
                Log.e("Asistencia", "Error al guardar", e)
                Toast.makeText(context, "Error de red al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
