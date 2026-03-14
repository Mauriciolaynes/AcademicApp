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
        
        val respuesta = if (nuevoEstado == EstadoSolicitud.APROBADA) 
            "Se acepta la justificación. Recuerda presentar tus trabajos mañana." 
        else 
            "Justificación rechazada. Comuníquese con coordinación."

        val request = JustificacionUpdateRequest(
            idJustificacion = justificacion.id,
            estado = nuevoEstado,
            respuestaProfesor = respuesta
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.actualizarEstadoJustificacion(request)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@GestionJustificacionActivity, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show()
                    cargarJustificaciones()
                } else {
                    Toast.makeText(this@GestionJustificacionActivity, "Error al actualizar estado: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("GestionJustificacion", "Error al actualizar", e)
                Toast.makeText(this@GestionJustificacionActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressJustificaciones.visibility = View.GONE
            }
        }
    }
}
