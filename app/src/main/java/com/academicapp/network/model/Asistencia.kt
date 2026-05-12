package com.academicapp.network.model

data class Asistencia(
    val id_asistencia: Int? = null,
    val id_clase: Int,
    val id_alumno: Int,
    val fecha: String, // formato "YYYY-MM-DD"
    val tipo: String, // "ASISTIO", "TARDE", "FALTO"
    val observacion: String? = null
)
