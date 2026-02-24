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

            // Marcar el RadioButton correspondiente sin activar el listener
            when (asistencia.estado) {
                EstadoAsistencia.PRESENTE -> binding.rbPresente.isChecked = true
                EstadoAsistencia.TARDANZA -> binding.rbTardanza.isChecked = true
                EstadoAsistencia.AUSENTE -> binding.rbFalta.isChecked = true
                else -> {}
            }

            binding.rgAsistencia.setOnCheckedChangeListener { _, checkedId ->
                val nuevoEstado = when (checkedId) {
                    binding.rbPresente.id -> EstadoAsistencia.PRESENTE
                    binding.rbTardanza.id -> EstadoAsistencia.TARDANZA
                    binding.rbFalta.id -> EstadoAsistencia.AUSENTE
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
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Asistencia, newItem: Asistencia): Boolean {
            return oldItem == newItem
        }
    }
}
