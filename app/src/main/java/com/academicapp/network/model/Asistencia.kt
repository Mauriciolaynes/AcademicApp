package com.academicapp.network.model

data class Asistencia(
    val id_clase: Int,
    val id_alumno: Int,
    val asistio: Boolean,
    val observacion: String
)
