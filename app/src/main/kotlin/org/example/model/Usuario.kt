package org.example.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para Usuario
 * Representa a un usuario del sistema de billetera virtual
 */
data class Usuario(
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val userType: TipoUsuario = TipoUsuario.CUSTOMER,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Valida que el email tenga formato correcto
     */
    fun validarEmail(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
    
    /**
     * Valida que el nombre no esté vacío
     */
    fun validarNombre(): Boolean {
        return fullName.isNotBlank() && fullName.length >= 3
    }
    
    /**
     * Obtiene representación legible del usuario
     */
    override fun toString(): String {
        return "Usuario(id=$id, nombre='$fullName', email='$email', tipo=$userType)"
    }
}

/**
 * Enum para tipos de usuario
 */
enum class TipoUsuario {
    CUSTOMER,
    ADMIN;
    
    companion object {
        fun fromString(value: String): TipoUsuario {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: CUSTOMER
        }
    }
}