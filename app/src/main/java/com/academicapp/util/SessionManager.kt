package com.academicapp.util

import android.content.Context
import android.content.SharedPreferences
import com.academicapp.data.model.Rol

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("AcademicAppPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_EMAIL = "user_email"
    }

    fun guardarSesion(id: Int, nombre: String, email: String) {
        prefs.edit().apply {
            putInt(USER_ID, id)
            putString(USER_NAME, nombre)
            putString(USER_EMAIL, email)
            apply()
        }
    }

    fun getUserId() = prefs.getInt(USER_ID, -1)
    fun getNombre() = prefs.getString(USER_NAME, null)
    fun getEmail() = prefs.getString(USER_EMAIL, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
