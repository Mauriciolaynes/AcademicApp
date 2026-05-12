package com.academicapp.ui.profesor.notas

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.databinding.ItemAlumnoNotaBinding

data class AlumnoNotaUI(
    val idUsuario: Int,
    val nombre: String,
    var nota: String = ""
)

class AlumnosNotasAdapter : ListAdapter<AlumnoNotaUI, AlumnosNotasAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlumnoNotaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAlumnoNotaBinding) : RecyclerView.ViewHolder(binding.root) {
        private var textWatcher: TextWatcher? = null

        fun bind(item: AlumnoNotaUI) {
            binding.tvAlumnoNombre.text = item.nombre
            
            textWatcher?.let { binding.etNota.removeTextChangedListener(it) }

            val filter = InputFilter { source, _, _, dest, dstart, dend ->
                val nuevoTexto = dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length)
                
                if (nuevoTexto.isEmpty()) return@InputFilter null
                
                if (!nuevoTexto.matches(Regex("^\\d{0,2}(\\.\\d{0,1})?$"))) {
                    return@InputFilter ""
                }
                
                val valor = nuevoTexto.toDoubleOrNull()
                if (valor != null && valor > 20.0) {
                    return@InputFilter ""
                }
                
                null
            }
            
            binding.etNota.filters = arrayOf(filter)
            binding.etNota.setText(item.nota)

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    item.nota = s?.toString() ?: ""
                }
            }
            binding.etNota.addTextChangedListener(textWatcher)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AlumnoNotaUI>() {
        override fun areItemsTheSame(oldItem: AlumnoNotaUI, newItem: AlumnoNotaUI) = oldItem.idUsuario == newItem.idUsuario
        override fun areContentsTheSame(oldItem: AlumnoNotaUI, newItem: AlumnoNotaUI) = oldItem == newItem
    }
}
