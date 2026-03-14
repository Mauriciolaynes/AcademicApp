package com.academicapp.util

import android.content.Context
import android.content.SharedPreferences
import com.academicapp.data.model.Rol

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("AcademicAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
    }

    fun guardarSesion(id: Int, nombre: String, email: String, rol: Rol) {
        val editor = prefs.edit()
        editor.putInt(USER_ID, id)
        editor.putString(USER_NAME, nombre)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_ROLE, rol.name)
        editor.apply()
    }

    fun getUserId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    fun getNombre(): String? {
        return prefs.getString(USER_NAME, null)
    }

    fun getEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun getRol(): Rol {
        val rolName = prefs.getString(USER_ROLE, Rol.ALUMNO.name)
        return Rol.valueOf(rolName ?: Rol.ALUMNO.name)
    }

    fun estaLogueado(): Boolean {
        return getUserId() != -1
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
