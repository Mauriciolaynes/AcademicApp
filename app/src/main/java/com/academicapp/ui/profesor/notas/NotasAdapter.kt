package com.academicapp.ui.profesor.notas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.data.model.Nota
import com.academicapp.databinding.ItemNotaBinding

class NotasAdapter : ListAdapter<Nota, NotasAdapter.NotaViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val binding = ItemNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = getItem(position)
        holder.bind(nota)
    }

    inner class NotaViewHolder(private val binding: ItemNotaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nota: Nota) {
            binding.tvAlumnoNombre.text = nota.alumnoNombre
            binding.tvCursoNombre.text = nota.cursoNombre
            binding.tvDescripcion.text = nota.tipoEvaluacion
            binding.tvFecha.text = nota.fecha
            binding.tvNota.text = nota.valor.toString()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Nota>() {
        override fun areItemsTheSame(oldItem: Nota, newItem: Nota): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Nota, newItem: Nota): Boolean {
            return oldItem == newItem
        }
    }
}