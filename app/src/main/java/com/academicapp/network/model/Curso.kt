package com.academicapp.network.model

import com.google.gson.annotations.SerializedName

data class Curso(
    @SerializedName("id_curso")
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("id_profesor")
    val profesorId: Int
)
