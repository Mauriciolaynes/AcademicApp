package com.academicapp.ui.profesor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.academicapp.databinding.FragmentPerfilBinding
import com.academicapp.ui.login.LoginActivity
import com.academicapp.util.SessionManager

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        cargarDatosPerfil()

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun cargarDatosPerfil() {
        val nombre = sessionManager.getNombre()
        val email = sessionManager.getEmail()
        val id = sessionManager.getUserId()

        binding.tvPerfilNombre.text = nombre ?: "Usuario"
        binding.tvPerfilEmail.text = email ?: "No disponible"
        binding.tvPerfilId.text = if (id != -1) id.toString() else "---"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
