package com.academicapp.ui.profesor.notas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.data.model.Curso
import com.academicapp.databinding.ItemCursoProfesorBinding

class CursoAdapter(private val onClick: (Curso) -> Unit) : ListAdapter<Curso, CursoAdapter.CursoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val binding = ItemCursoProfesorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CursoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val curso = getItem(position)
        holder.bind(curso)
        holder.itemView.setOnClickListener { onClick(curso) }
    }

    inner class CursoViewHolder(private val binding: ItemCursoProfesorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(curso: Curso) {
            binding.tvCursoIcono.text = curso.icono
            binding.tvCursoNombre.text = curso.nombre
            binding.tvGradoCurso.text = curso.gradoNombre
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Curso>() {
        override fun areItemsTheSame(oldItem: Curso, newItem: Curso): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Curso, newItem: Curso): Boolean {
            return oldItem == newItem
        }
    }
}