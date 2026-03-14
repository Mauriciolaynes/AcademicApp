package com.academicapp.ui.profesor.justificacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.data.model.Justificacion
import com.academicapp.data.model.EstadoSolicitud
import com.academicapp.databinding.ItemJustificacionBinding

class JustificacionAdapter(
    private val onAprobar: (Justificacion) -> Unit,
    private val onRechazar: (Justificacion) -> Unit
) : ListAdapter<Justificacion, JustificacionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJustificacionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemJustificacionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Justificacion) {
            binding.tvAlumnoNombre.text = item.alumnoNombre
            binding.tvFecha.text = item.fecha
            binding.tvCursoNombre.text = item.cursoNombre
            binding.tvMotivo.text = item.motivo

            if (item.estado == EstadoSolicitud.PENDIENTE) {
                binding.layoutAcciones.visibility = View.VISIBLE
                binding.tvEstado.visibility = View.GONE
            } else {
                binding.layoutAcciones.visibility = View.GONE
                binding.tvEstado.visibility = View.VISIBLE
                binding.tvEstado.text = item.estado.name
                val color = if (item.estado == EstadoSolicitud.APROBADA) "#4CAF50" else "#F44336"
                binding.tvEstado.setTextColor(android.graphics.Color.parseColor(color))
            }

            binding.btnAprobar.setOnClickListener { onAprobar(item) }
            binding.btnRechazar.setOnClickListener { onRechazar(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Justificacion>() {
        override fun areItemsTheSame(oldItem: Justificacion, newItem: Justificacion) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Justificacion, newItem: Justificacion) = oldItem == newItem
    }
}
