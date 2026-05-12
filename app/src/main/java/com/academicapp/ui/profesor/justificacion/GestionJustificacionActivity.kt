package com.academicapp.ui.profesor.justificacion

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Justificacion
import com.academicapp.data.model.JustificacionUpdateRequest
import com.academicapp.data.model.EstadoSolicitud
import com.academicapp.databinding.ActivityGestionJustificacionBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.util.SessionManager
import kotlinx.coroutines.launch

class GestionJustificacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionJustificacionBinding
    private lateinit var adapter: JustificacionAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionJustificacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        cargarJustificaciones()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = JustificacionAdapter(
            onAprobar = { justificacion -> actualizarEstado(justificacion, EstadoSolicitud.APROBADA) },
            onRechazar = { justificacion -> actualizarEstado(justificacion, EstadoSolicitud.RECHAZADA) }
        )
        binding.rvJustificaciones.layoutManager = LinearLayoutManager(this)
        binding.rvJustificaciones.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { cargarJustificaciones() }
    }

    private fun cargarJustificaciones() {
        binding.progressJustificaciones.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getJustificaciones()
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!!
                    adapter.submitList(lista)
                    if (lista.isEmpty()) binding.tvNoData.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@GestionJustificacionActivity, "Error al cargar justificaciones", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("GestionJustificacion", "Error de red", e)
                Toast.makeText(this@GestionJustificacionActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressJustificaciones.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun actualizarEstado(justificacion: Justificacion, nuevoEstado: EstadoSolicitud) {
        binding.progressJustificaciones.visibility = View.VISIBLE
        
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val hoy = sdf.format(java.util.Date())

        val esAprobada = nuevoEstado == EstadoSolicitud.APROBADA || nuevoEstado == EstadoSolicitud.ACEPTADA
        
        val respuesta = if (esAprobada) 
            "Justificación aprobada el $hoy. Asistencia actualizada." 
        else 
            "Justificación rechazada el $hoy. Comuníquese con coordinación."

        val request = JustificacionUpdateRequest(
            idJustificacion = justificacion.id,
            idAsistencia = justificacion.asistenciaId,
            estado = nuevoEstado,
            respuestaProfesor = respuesta
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.actualizarEstadoJustificacion(request)
                
                if (response.isSuccessful) {
                    if (esAprobada) {
                        // Si se aprueba, actualizamos la asistencia a ASISTIO
                        actualizarAsistenciaAprobadaSync(justificacion.asistenciaId)
                    } else {
                        Toast.makeText(this@GestionJustificacionActivity, "Justificación rechazada correctamente", Toast.LENGTH_SHORT).show()
                    }
                    // Refrescamos la lista en ambos casos exitosos
                    cargarJustificaciones()
                } else {
                    val errorMsg = "Error al actualizar: ${response.code()}"
                    Toast.makeText(this@GestionJustificacionActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("GestionJustificacion", "Error API: ${response.errorBody()?.string()}")
                    cargarJustificaciones() // Recargar para restaurar estado de botones
                }
            } catch (e: Exception) {
                Log.e("GestionJustificacion", "Error al actualizar", e)
                Toast.makeText(this@GestionJustificacionActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                cargarJustificaciones()
            } finally {
                binding.progressJustificaciones.visibility = View.GONE
            }
        }
    }

    private suspend fun actualizarAsistenciaAprobadaSync(asistenciaId: Int) {
        try {
            // 1. Obtener los datos actuales de la asistencia
            val resGet = RetrofitClient.instance.getAsistenciaPorId(asistenciaId)
            if (resGet.isSuccessful && resGet.body() != null) {
                val asistenciaActual = resGet.body()!!
                
                // 2. Modificar el tipo a ASISTIO (Justificado)
                val asistenciaEditada = asistenciaActual.copy(
                    tipo = "ASISTIO",
                    observacion = "Justificado: ${asistenciaActual.observacion ?: ""}".trim()
                )
                
                // 3. Enviar actualización
                val resPut = RetrofitClient.instance.editarAsistencia(asistenciaEditada)
                if (resPut.isSuccessful) {
                    Log.d("GestionJustificacion", "Asistencia actualizada correctamente")
                }
            }
        } catch (e: Exception) {
            Log.e("GestionJustificacion", "Error al sincronizar asistencia", e)
        }
    }
}
