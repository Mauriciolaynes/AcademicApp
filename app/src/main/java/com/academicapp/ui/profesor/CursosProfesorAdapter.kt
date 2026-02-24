package com.academicapp.ui.profesor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.data.model.Curso
import com.academicapp.databinding.ItemCursoBinding

class CursosProfesorAdapter(
    private val onClick: (Curso) -> Unit
) : ListAdapter<Curso, CursosProfesorAdapter.CursoVH>(DiffCallback()) {

    inner class CursoVH(private val binding: ItemCursoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(curso: Curso) {
            binding.tvIconoCurso.text    = curso.icono
            binding.tvNombreCurso.text  = curso.nombre
            binding.tvMetaCurso.text    = "${curso.gradoNombre} · ${curso.cantAlumnos} alumnos"
            binding.tvBadgeCurso.text   = if (curso.horaInicio.isNotEmpty()) curso.horaInicio else "Hoy"
            binding.root.setOnClickListener { onClick(curso) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoVH {
        val binding = ItemCursoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CursoVH(binding)
    }

    override fun onBindViewHolder(holder: CursoVH, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Curso>() {
        override fun areItemsTheSame(a: Curso, b: Curso) = a.id == b.id
        override fun areContentsTheSame(a: Curso, b: Curso) = a == b
    }
}
