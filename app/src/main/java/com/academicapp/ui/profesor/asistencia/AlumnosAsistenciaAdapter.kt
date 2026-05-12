package com.academicapp.ui.profesor.asistencia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.data.model.Asistencia
import com.academicapp.data.model.EstadoAsistencia
import com.academicapp.databinding.ItemAlumnoAsistenciaBinding

class AlumnosAsistenciaAdapter(
    private val onAsistenciaChanged: (Asistencia) -> Unit
) : ListAdapter<Asistencia, AlumnosAsistenciaAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlumnoAsistenciaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alumno = getItem(position)
        holder.bind(alumno)
    }

    inner class ViewHolder(private val binding: ItemAlumnoAsistenciaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(asistencia: Asistencia) {
            binding.tvAlumnoNombre.text = asistencia.alumnoNombre

            binding.rgAsistencia.setOnCheckedChangeListener(null)
            
            when (asistencia.estado) {
                EstadoAsistencia.ASISTIO -> binding.rgAsistencia.check(binding.rbPresente.id)
                EstadoAsistencia.TARDE -> binding.rgAsistencia.check(binding.rbTardanza.id)
                EstadoAsistencia.FALTO -> binding.rgAsistencia.check(binding.rbFalta.id)
                EstadoAsistencia.SIN_MARCAR -> binding.rgAsistencia.clearCheck()
            }

            binding.rgAsistencia.setOnCheckedChangeListener { _, checkedId ->
                val nuevoEstado = when (checkedId) {
                    binding.rbPresente.id -> EstadoAsistencia.ASISTIO
                    binding.rbTardanza.id -> EstadoAsistencia.TARDE
                    binding.rbFalta.id -> EstadoAsistencia.FALTO
                    else -> asistencia.estado
                }
                
                if (asistencia.estado != nuevoEstado) {
                    asistencia.estado = nuevoEstado
                    onAsistenciaChanged(asistencia)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Asistencia>() {
        override fun areItemsTheSame(oldItem: Asistencia, newItem: Asistencia): Boolean {
            return oldItem.alumnoId == newItem.alumnoId
        }

        override fun areContentsTheSame(oldItem: Asistencia, newItem: Asistencia): Boolean {
            return oldItem == newItem
        }
    }
}
