package org.example.service

import org.example.database.DatabaseManager
import org.example.model.Usuario
import org.example.model.TipoUsuario
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Servicio para gestionar operaciones de usuarios
 * Aplica principio Single Responsibility: solo maneja lógica de usuarios
 */
class UsuarioService(private val dbManager: DatabaseManager = DatabaseManager()) {
    
    /**
     * Crea un nuevo usuario en el sistema
     * @param fullName Nombre completo del usuario
     * @param email Email único del usuario
     * @param password Contraseña en texto plano (se hasheará)
     * @param userType Tipo de usuario (CUSTOMER o ADMIN)
     * @return ID del usuario creado
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws SQLException si el email ya existe
     */
    fun crearUsuario(
        fullName: String, 
        email: String, 
        password: String, 
        userType: TipoUsuario = TipoUsuario.CUSTOMER
    ): Long {
        // Validaciones
        val usuario = Usuario(
            fullName = fullName,
            email = email,
            passwordHash = hashPassword(password),
            userType = userType
        )
        
        if (!usuario.validarNombre()) {
            throw IllegalArgumentException("El nombre debe tener al menos 3 caracteres")
        }
        
        if (!usuario.validarEmail()) {
            throw IllegalArgumentException("El email no tiene un formato válido")
        }
        
        // Verificar que el email no exista
        if (existeEmail(email)) {
            throw SQLException("Ya existe un usuario con el email: $email")
        }
        
        // Insertar usuario
        return try {
            dbManager.executeInsert(
                "INSERT INTO users (full_name, email, password_hash, user_type) VALUES (?, ?, ?, ?)",
                usuario.fullName,
                usuario.email,
                usuario.passwordHash,
                usuario.userType.name
            )
        } catch (e: SQLException) {
            throw SQLException("Error al crear usuario: ${e.message}")
        }
    }
    
    /**
     * Busca un usuario por su ID
     * @return Usuario encontrado o null
     */
    fun buscarPorId(id: Long): Usuario? {
        return try {
            val resultSet = dbManager.executeQuery(
                "SELECT * FROM users WHERE id = ?",
                id
            )
            
            if (resultSet.next()) {
                mapearUsuario(resultSet)
            } else {
                null
            }
        } catch (e: SQLException) {
            null
        }
    }
    
    /**
     * Busca un usuario por su email
     * @return Usuario encontrado o null
     */
    fun buscarPorEmail(email: String): Usuario? {
        return try {
            val resultSet = dbManager.executeQuery(
                "SELECT * FROM users WHERE email = ?",
                email
            )
            
            if (resultSet.next()) {
                mapearUsuario(resultSet)
            } else {
                null
            }
        } catch (e: SQLException) {
            null
        }
    }
    
    /**
     * Lista todos los usuarios del sistema
     * @return Lista de usuarios
     */
    fun listarTodos(): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        
        try {
            val resultSet = dbManager.executeQuery("SELECT * FROM users ORDER BY created_at DESC")
            
            while (resultSet.next()) {
                usuarios.add(mapearUsuario(resultSet))
            }
            
            resultSet.close()
        } catch (e: SQLException) {
            println("Error listando usuarios: ${e.message}")
        }
        
        return usuarios
    }
    
    /**
     * Verifica si existe un email en el sistema
     */
    fun existeEmail(email: String): Boolean {
        return dbManager.exists("users", "email = ?", email)
    }
    
    /**
     * Autentica un usuario con email y password
     * @return Usuario si las credenciales son correctas, null en caso contrario
     */
    fun autenticar(email: String, password: String): Usuario? {
        val usuario = buscarPorEmail(email) ?: return null
        
        return if (verificarPassword(password, usuario.passwordHash)) {
            usuario
        } else {
            null
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
    
    /**
     * Hashea una contraseña (versión simplificada)
     * En producción usar BCrypt o similar
     */
    private fun hashPassword(password: String): String {
        return "hash_${password}_${password.length}"
    }
    
    /**
     * Verifica una contraseña contra su hash
     */
    private fun verificarPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}