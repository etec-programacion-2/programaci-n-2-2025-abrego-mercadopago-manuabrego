package org.example.service

import org.example.database.DatabaseManager
import org.example.model.Usuario
import org.example.model.TipoUsuario
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Servicio para gestionar usuarios
 * CORREGIDO: Usa DatabaseManager singleton y maneja passwordHash correctamente
 */
class UsuarioService {
    
    /**
     * Crea un nuevo usuario
     * @param fullName Nombre completo del usuario
     * @param email Email del usuario
     * @param password Contraseña en texto plano (se debería hashear en producción)
     * @param userType Tipo de usuario (por defecto CUSTOMER)
     * @return ID del usuario creado
     */
    fun crearUsuario(
        fullName: String,
        email: String,
        password: String,
        userType: TipoUsuario = TipoUsuario.CUSTOMER
    ): Long {
        // Validar que el email no exista
        if (existeEmail(email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }
        
        // En producción, aquí se debería hashear la contraseña
        // Por ahora usamos un hash simple para desarrollo
        val passwordHash = "hash_${password}_${email.length}"
        
        return DatabaseManager.executeInsert(
            """INSERT INTO users (full_name, email, password_hash, user_type) 
               VALUES (?, ?, ?, ?)""",
            fullName,
            email,
            passwordHash,
            userType.name
        )
    }
    
    /**
     * Autentica un usuario
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @return Usuario si las credenciales son correctas, null en caso contrario
     */
    fun autenticar(email: String, password: String): Usuario? {
        return try {
            // Generar el mismo hash que se usó al crear el usuario
            val passwordHash = "hash_${password}_${email.length}"
            
            DatabaseManager.executeQuery(
                "SELECT * FROM users WHERE email = ? AND password_hash = ?",
                email,
                passwordHash
            ) { rs ->
                if (rs.next()) {
                    mapearUsuario(rs)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            println("Error en autenticación: ${e.message}")
            null
        }
    }
    
    /**
     * Busca un usuario por su ID
     */
    fun buscarPorId(id: Long): Usuario? {
        return try {
            DatabaseManager.executeQuery(
                "SELECT * FROM users WHERE id = ?",
                id
            ) { rs ->
                if (rs.next()) {
                    mapearUsuario(rs)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            println("Error buscando usuario: ${e.message}")
            null
        }
    }
    
    /**
     * Busca un usuario por su email
     */
    fun buscarPorEmail(email: String): Usuario? {
        return try {
            DatabaseManager.executeQuery(
                "SELECT * FROM users WHERE email = ?",
                email
            ) { rs ->
                if (rs.next()) {
                    mapearUsuario(rs)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            println("Error buscando usuario por email: ${e.message}")
            null
        }
    }
    
    /**
     * Verifica si existe un usuario con el email dado
     */
    fun existeEmail(email: String): Boolean {
        return DatabaseManager.exists("users", "email = ?", email)
    }
    
    /**
     * Actualiza los datos de un usuario
     */
    fun actualizarUsuario(id: Long, fullName: String, email: String): Boolean {
        return try {
            val rowsAffected = DatabaseManager.executeUpdate(
                "UPDATE users SET full_name = ?, email = ? WHERE id = ?",
                fullName,
                email,
                id
            )
            rowsAffected > 0
        } catch (e: SQLException) {
            println("Error actualizando usuario: ${e.message}")
            false
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario
     */
    private fun mapearUsuario(rs: ResultSet): Usuario {
        return Usuario(
            id = rs.getLong("id"),
            fullName = rs.getString("full_name"),
            email = rs.getString("email"),
            passwordHash = rs.getString("password_hash"),
            userType = TipoUsuario.fromString(rs.getString("user_type"))
        )
    }
}