package com.academicapp.network.model

data class Usuario(
    val id_usuario: Int,
    val codigo: String,
    val nombre: String,
    val email: String,
    val password: String,
    val id_rol: String
)
