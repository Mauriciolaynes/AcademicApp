package com.academicapp.network.model

data class Nota(
    val id_nota: Int,
    val id_alumno: Int,
    val id_curso: Int,
    val calificacion: Float,
    val unidad: String
)
