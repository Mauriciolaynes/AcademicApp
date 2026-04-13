package com.academicapp.data.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    val id: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val password: String = "",
    val rol: Rol = Rol.ALUMNO
)

enum class Rol { PROFESOR, ALUMNO }

data class Profesor(
    val id: Int = 0,
    val usuarioId: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val especialidad: String = ""
)


data class Curso(
    val id: Int = 0,
    val nombre: String = "",
    val icono: String = "📚",
    val gradoId: Int = 0,
    val gradoNombre: String = "",
    val profesorId: Int = 0,
    val cantAlumnos: Int = 0,
    val horaInicio: String = "",
    val horaFin: String = ""
)

data class Asistencia(
    val id: Int = 0,
    val alumnoId: Int = 0,
    val alumnoNombre: String = "",
    val cursoId: Int = 0,
    val fecha: String = "",
    var estado: EstadoAsistencia = EstadoAsistencia.SIN_MARCAR
)

enum class EstadoAsistencia { PRESENTE, AUSENTE, TARDANZA, SIN_MARCAR }

data class Nota(
    val id: Int = 0,
    val alumnoId: Int = 0,
    val alumnoNombre: String = "",
    val cursoId: Int = 0,
    val cursoNombre: String = "",
    val tipoEvaluacion: String = "",
    var valor: Float = 0f,
    val fecha: String = "",
    val profesorId: Int = 0
)

data class Justificacion(
    @SerializedName("id_justificacion")
    val id: Int = 0,
    @SerializedName("id_alumno")
    val alumnoId: Int = 0,
    @SerializedName("alumno_nombre")
    val alumnoNombre: String = "",
    @SerializedName("id_asistencia")
    val asistenciaId: Int = 0,
    @SerializedName("nombre_curso")
    val cursoNombre: String = "",
    val fecha: String = "",
    val motivo: String = "",
    var estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    @SerializedName("respuesta_profesor")
    val respuestaProfesor: String? = null
)

data class JustificacionUpdateRequest(
    @SerializedName("id_justificacion")
    val idJustificacion: Int,
    val estado: EstadoSolicitud,
    @SerializedName("respuesta_profesor")
    val respuestaProfesor: String
)

data class RevisionNota(
    val id: Int = 0,
    val alumnoId: Int = 0,
    val alumnoNombre: String = "",
    val notaId: Int = 0,
    val cursoNombre: String = "",
    val valorActual: Float = 0f,
    val motivo: String = "",
    var estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    val comentarioProfesor: String = ""
)

enum class EstadoSolicitud { PENDIENTE, APROBADA, RECHAZADA }
