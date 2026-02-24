package com.academicapp.network

import com.academicapp.network.model.Asistencia
import com.academicapp.network.model.Curso
import com.academicapp.network.model.Nota
import com.academicapp.network.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("usuarios/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<Usuario>

    @GET("usuarios/curso/{id}")
    suspend fun getAlumnosPorCurso(@Path("id") cursoId: Int): Response<List<Usuario>>

    @GET("cursos/profesor/{id}")
    suspend fun getCursosPorProfesor(@Path("id") profesorId: Int): Response<List<Curso>>

    @POST("asistencias")
    suspend fun registrarAsistencia(@Body asistencia: Asistencia): Response<Unit>

    @POST("notas")
    suspend fun registrarNota(@Body nota: Nota): Response<Unit>
}
