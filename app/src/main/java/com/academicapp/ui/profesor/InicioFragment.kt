package com.academicapp.ui.profesor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.academicapp.data.model.Curso
import com.academicapp.data.model.EstadoSolicitud
import com.academicapp.databinding.FragmentInicioBinding
import com.academicapp.network.RetrofitClient
import com.academicapp.ui.profesor.asistencia.MarcarAsistenciaActivity
import com.academicapp.ui.profesor.justificacion.GestionJustificacionActivity
import com.academicapp.ui.profesor.notas.CursoAdapter
import com.academicapp.ui.profesor.notas.IngresarNotasActivity
import com.academicapp.ui.profesor.notas.ModificarNotasActivity
import com.academicapp.ui.profesor.notas.VerNotasProfesorActivity
import com.academicapp.util.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var cursosAdapter: CursosProfesorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupUI()
        setupRecyclerView()
        setupListeners()
        cargarCursosDesdeApi()
        actualizarContadorJustificaciones()
    }

    private fun setupUI() {
        val nombre = sessionManager.getNombre()
        binding.tvNombreProfesor.text = getString(com.academicapp.R.string.buenos_dias_format, nombre)
        binding.tvFecha.text = "📅 ${obtenerFechaActual()}"
    }

    private fun setupRecyclerView() {
        cursosAdapter = CursosProfesorAdapter { curso ->
            abrirActividadSegunContexto(MarcarAsistenciaActivity::class.java, curso)
        }
        binding.rvCursos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCursos.adapter = cursosAdapter
    }

    private fun setupListeners() {
        binding.btnMarcarAsistencia.setOnClickListener {
            showCourseSelectionBottomSheet("Asistencia", MarcarAsistenciaActivity::class.java)
        }
        binding.btnIngresarNotas.setOnClickListener {
            showCourseSelectionBottomSheet("Ingresar Notas", IngresarNotasActivity::class.java)
        }
        binding.btnModificarNotas.setOnClickListener {
            showCourseSelectionBottomSheet("Modificar Notas", ModificarNotasActivity::class.java)
        }
        binding.btnVerNotas.setOnClickListener {
            showCourseSelectionBottomSheet("Ver Notas", VerNotasProfesorActivity::class.java)
        }
        binding.cardPendientes.setOnClickListener {
            startActivity(Intent(requireContext(), GestionJustificacionActivity::class.java))
        }
    }

    private fun showCourseSelectionBottomSheet(title: String, targetActivity: Class<*>) {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), com.academicapp.R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(com.academicapp.R.layout.layout_bottom_sheet_cursos, null)
        
        val tvTitle = view.findViewById<TextView>(com.academicapp.R.id.tvSheetTitle)
        val rvCursos = view.findViewById<RecyclerView>(com.academicapp.R.id.rvCursosSheet)
        val progress = view.findViewById<ProgressBar>(com.academicapp.R.id.progressSheet)
        
        tvTitle.text = title
        
        val sheetAdapter = CursoAdapter { curso ->
            abrirActividadSegunContexto(targetActivity, curso)
            bottomSheetDialog.dismiss()
        }
        
        rvCursos.layoutManager = LinearLayoutManager(requireContext())
        rvCursos.adapter = sheetAdapter
        
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        val profesorId = sessionManager.getUserId()
        if (profesorId != -1) {
            progress.visibility = View.VISIBLE
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
                        sheetAdapter.submitList(cursosConDetalles)
                    }
                } catch (e: Exception) {
                    Log.e("BottomSheet", "Error", e)
                } finally {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    private fun abrirActividadSegunContexto(targetActivity: Class<*>, curso: Curso) {
        val intent = Intent(requireContext(), targetActivity).apply {
            putExtra("curso_id", curso.id)
            putExtra("curso_nombre", curso.nombre)
        }
        startActivity(intent)
    }

    private fun cargarCursosDesdeApi() {
        val profesorId = sessionManager.getUserId()
        if (profesorId == -1) return

        binding.progressHome.visibility = View.VISIBLE

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
                    cursosAdapter.submitList(cursosConDetalles)
                }
            } catch (e: Exception) {
                Log.e("InicioFragment", "Error", e)
            } finally {
                binding.progressHome.visibility = View.GONE
            }
        }
    }

    private fun actualizarContadorJustificaciones() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getJustificaciones()
                if (response.isSuccessful && response.body() != null) {
                    val totalPendientes = response.body()!!.count { it.estado == EstadoSolicitud.PENDIENTE }
                    binding.tvCantidadPendientes.text = totalPendientes.toString()
                }
            } catch (e: Exception) {
                Log.e("InicioFragment", "Error al cargar contador", e)
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

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("es", "PE"))
        return sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorJustificaciones()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
