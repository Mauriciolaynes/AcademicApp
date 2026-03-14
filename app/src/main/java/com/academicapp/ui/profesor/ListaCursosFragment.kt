package com.academicapp.ui.profesor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.academicapp.data.model.Curso
import com.academicapp.databinding.FragmentListaCursosBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.ui.profesor.asistencia.MarcarAsistenciaActivity
import com.academicapp.ui.profesor.notas.CursoAdapter
import com.academicapp.ui.profesor.notas.IngresarNotasActivity
import com.academicapp.util.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ListaCursosFragment : Fragment() {

    private var _binding: FragmentListaCursosBinding? = null
    private val binding get() = _binding!!
    private lateinit var cursoAdapter: CursoAdapter
    private lateinit var sessionManager: SessionManager
    private var tipo: String = "asistencia"

    companion object {
        fun newInstance(tipo: String): ListaCursosFragment {
            val fragment = ListaCursosFragment()
            val args = Bundle()
            args.putString("tipo", tipo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaCursosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        
        // Soporte para Navigation Component y newInstance tradicional
        tipo = arguments?.getString("tipo") ?: "asistencia"

        setupUI()
        setupRecyclerView()
        cargarCursos()
    }

    private fun setupUI() {
        if (tipo == "asistencia") {
            binding.tvTituloPestana.text = "Asistencia"
            binding.tvSubtituloPestana.text = "Seleccione un curso para marcar asistencia"
        } else {
            binding.tvTituloPestana.text = "Notas"
            binding.tvSubtituloPestana.text = "Seleccione un curso para ingresar notas"
        }
    }

    private fun setupRecyclerView() {
        cursoAdapter = CursoAdapter { curso ->
            val targetActivity = if (tipo == "asistencia") {
                MarcarAsistenciaActivity::class.java
            } else {
                IngresarNotasActivity::class.java
            }
            
            val intent = Intent(requireContext(), targetActivity).apply {
                putExtra("curso_id", curso.id)
                putExtra("curso_nombre", curso.nombre)
            }
            startActivity(intent)
        }
        
        binding.rvCursos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cursoAdapter
        }
    }

    private fun cargarCursos() {
        val profesorId = sessionManager.getUserId()
        if (profesorId == -1) return

        binding.progressCursos.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCursosPorProfesor(profesorId)
                if (response.isSuccessful && response.body() != null) {
                    val cursosDesdeApi = response.body()!!
                    
                    val cursosConDetalles = cursosDesdeApi.map { cursoNet ->
                        async {
                            val alumnosResponse = RetrofitClient.instance.getAlumnosPorCurso(cursoNet.id)
                            val cantAlumnos = if (alumnosResponse.isSuccessful) alumnosResponse.body()?.size ?: 0 else 0
                            
                            Curso(
                                id = cursoNet.id,
                                nombre = cursoNet.nombre,
                                icono = obtenerIconoPorNombre(cursoNet.nombre),
                                gradoNombre = "Grado",
                                profesorId = cursoNet.profesorId,
                                cantAlumnos = cantAlumnos
                            )
                        }
                    }.awaitAll()

                    cursoAdapter.submitList(cursosConDetalles)
                }
            } catch (e: Exception) {
                Log.e("ListaCursosFragment", "Error", e)
            } finally {
                binding.progressCursos.visibility = View.GONE
            }
        }
    }

    private fun obtenerIconoPorNombre(nombre: String): String {
        return when {
            nombre.contains("Matem", true) -> "📐"
            nombre.contains("Ciencia", true) -> "🔬"
            nombre.contains("Comunic", true) -> "📚"
            nombre.contains("Hist", true) -> "📜"
            nombre.contains("Art", true) -> "🎨"
            nombre.contains("Ingl", true) -> "🇬🇧"
            else -> "📘"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
