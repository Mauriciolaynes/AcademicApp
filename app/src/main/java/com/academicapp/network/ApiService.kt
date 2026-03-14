package com.academicapp.network

import com.academicapp.network.model.Asistencia
import com.academicapp.network.model.Curso
import com.academicapp.network.model.Nota
import com.academicapp.network.model.Usuario
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("usuarios/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<Usuario>

    @GET("usuarios/curso/{id}")
    suspend fun getAlumnosPorCurso(@Path("id") cursoId: Int): Response<List<Usuario>>

    @GET("cursos/profesor/{id}")
    suspend fun getCursosPorProfesor(@Path("id") profesorId: Int): Response<List<Curso>>

    @GET("asistencias/curso/{id}")
    suspend fun getAsistenciaPorCurso(@Path("id") cursoId: Int, @Query("fecha") fecha: String): Response<List<Asistencia>>

    @GET("asistencias/{cursoId}/{alumnoId}")
    suspend fun visualizarAsistencia(@Path("cursoId") cursoId: Int, @Path("alumnoId") alumnoId: Int): Response<Asistencia>

    @POST("asistencias/bulk")
    suspend fun registrarAsistenciaMasiva(@Body asistencias: List<Asistencia>): Response<Unit>

    @POST("asistencias")
    suspend fun registrarAsistencia(@Body asistencia: Asistencia): Response<Map<String, String>>

    @PUT("asistencias")
    suspend fun editarAsistencia(@Body asistencia: Asistencia): Response<Unit>

    @GET("notas")
    suspend fun getNotas(): Response<List<Nota>>

    @GET("notas/curso/{id}")
    suspend fun getNotasPorCurso(@Path("id") cursoId: Int): Response<List<Nota>>

    @POST("notas")
    suspend fun registrarNota(@Body nota: Nota): Response<Map<String, String>>

    @PUT("notas")
    suspend fun editarNota(@Body nota: Nota): Response<Map<String, String>>

    @DELETE("notas/{id}")
    suspend fun eliminarNota(@Path("id") id: Int): Response<Map<String, String>>

    @GET("notas/{id}")
    suspend fun visualizarNota(@Path("id") id: Int): Response<Nota>
}
