package com.academicapp.ui.profesor.notas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.network.model.Nota
import com.academicapp.databinding.ItemNotaBinding

class NotasAdapter(
    private val onEdit: (Nota) -> Unit,
    private val onDelete: (Nota) -> Unit
) : ListAdapter<Nota, NotasAdapter.NotaViewHolder>(DiffCallback()) {

    private var alumnosMap: Map<Int, String> = emptyMap()

    fun setAlumnosMap(map: Map<Int, String>) {
        alumnosMap = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val binding = ItemNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotaViewHolder(private val binding: ItemNotaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nota: Nota) {
            val nombreAlumno = alumnosMap[nota.id_alumno] ?: "Alumno #${nota.id_alumno}"
            binding.tvAlumnoNombre.text = nombreAlumno
            binding.tvCursoNombre.text = "Unidad: ${nota.unidad}"
            binding.tvDescripcion.text = "Calificación registrada"
            binding.tvNota.text = nota.calificacion.toString()
            
            // Acción de editar al tocar la tarjeta
            binding.root.setOnClickListener { onEdit(nota) }
            
            // Acción de eliminar al tocar el icono de basurero
            binding.btnDeleteNota.setOnClickListener { onDelete(nota) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Nota>() {
        override fun areItemsTheSame(oldItem: Nota, newItem: Nota) = oldItem.id_nota == newItem.id_nota
        override fun areContentsTheSame(oldItem: Nota, newItem: Nota) = oldItem == newItem
    }
}
