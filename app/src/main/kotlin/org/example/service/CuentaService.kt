package org.example.service

import org.example.database.DatabaseManager
import org.example.model.Cuenta
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Servicio para gestionar operaciones de cuentas
 * Aplica principio Single Responsibility: solo maneja lógica de cuentas
 */
class CuentaService(private val dbManager: DatabaseManager = DatabaseManager()) {
    
    /**
     * Crea una nueva cuenta para un usuario
     * @param userId ID del usuario propietario
     * @param balanceInicial Balance inicial (por defecto 0)
     * @param currency Moneda de la cuenta (por defecto ARS)
     * @return ID de la cuenta creada
     */
    fun crearCuenta(
        userId: Long, 
        balanceInicial: Double = 0.0, 
        currency: String = "ARS"
    ): Long {
        // Validaciones
        if (balanceInicial < 0) {
            throw IllegalArgumentException("El balance inicial no puede ser negativo")
        }
        
        if (!existeUsuario(userId)) {
            throw IllegalArgumentException("El usuario con ID $userId no existe")
        }
        
        return try {
            dbManager.executeInsert(
                "INSERT INTO accounts (user_id, balance, currency) VALUES (?, ?, ?)",
                userId,
                balanceInicial,
                currency
            )
        } catch (e: SQLException) {
            throw SQLException("Error al crear cuenta: ${e.message}")
        }
    }
    
    /**
     * Busca una cuenta por su ID
     * @return Cuenta encontrada o null
     */
    fun buscarPorId(id: Long): Cuenta? {
        return try {
            dbManager.executeQuery("SELECT * FROM accounts WHERE id = ?", id) { rs ->
                if (rs.next()) {
                    mapearCuenta(rs)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            null
        }
    }
    
    /**
     * Obtiene todas las cuentas de un usuario
     * @param userId ID del usuario
     * @return Lista de cuentas del usuario
     */
    fun obtenerCuentasPorUsuario(userId: Long): List<Cuenta> {
        return try {
            dbManager.executeQuery(
                "SELECT * FROM accounts WHERE user_id = ? ORDER BY created_at DESC",
                userId
            ) { rs ->
                buildList {
                    while (rs.next()) {
                        add(mapearCuenta(rs))
                    }
                }
            }
        } catch (e: SQLException) {
            println("Error obteniendo cuentas: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Consulta el balance de una cuenta
     * @return Balance actual o null si no existe
     */
    fun consultarBalance(cuentaId: Long): Double? {
        return buscarPorId(cuentaId)?.balance
    }
    
    /**
     * Deposita dinero en una cuenta
     * @param cuentaId ID de la cuenta
     * @param monto Monto a depositar
     * @return true si se realizó correctamente
     */
    fun depositar(cuentaId: Long, monto: Double): Boolean {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val cuenta = buscarPorId(cuentaId) 
            ?: throw IllegalArgumentException("La cuenta no existe")
        
        return try {
            val filasAfectadas = dbManager.executeUpdate(
                "UPDATE accounts SET balance = balance + ? WHERE id = ?",
                monto,
                cuentaId
            )
            filasAfectadas > 0
        } catch (e: SQLException) {
            throw SQLException("Error al depositar: ${e.message}")
        }
    }
    
    /**
     * Retira dinero de una cuenta
     * @param cuentaId ID de la cuenta
     * @param monto Monto a retirar
     * @return true si se realizó correctamente
     */
    fun retirar(cuentaId: Long, monto: Double): Boolean {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val cuenta = buscarPorId(cuentaId) 
            ?: throw IllegalArgumentException("La cuenta no existe")
        
        if (!cuenta.tieneFondosSuficientes(monto)) {
            throw IllegalArgumentException("Fondos insuficientes. Balance actual: ${cuenta.balanceFormateado()}")
        }
        
        return try {
            val filasAfectadas = dbManager.executeUpdate(
                "UPDATE accounts SET balance = balance - ? WHERE id = ?",
                monto,
                cuentaId
            )
            filasAfectadas > 0
        } catch (e: SQLException) {
            throw SQLException("Error al retirar: ${e.message}")
        }
    }
    
    /**
     * Transfiere dinero entre dos cuentas
     * @param cuentaOrigenId ID de la cuenta origen
     * @param cuentaDestinoId ID de la cuenta destino
     * @param monto Monto a transferir
     * @return true si se realizó correctamente
     */
    fun transferir(cuentaOrigenId: Long, cuentaDestinoId: Long, monto: Double): Boolean {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        if (cuentaOrigenId == cuentaDestinoId) {
            throw IllegalArgumentException("No se puede transferir a la misma cuenta")
        }
        
        val cuentaOrigen = buscarPorId(cuentaOrigenId) 
            ?: throw IllegalArgumentException("La cuenta origen no existe")
            
        val cuentaDestino = buscarPorId(cuentaDestinoId) 
            ?: throw IllegalArgumentException("La cuenta destino no existe")
        
        if (!cuentaOrigen.tieneFondosSuficientes(monto)) {
            throw IllegalArgumentException("Fondos insuficientes en cuenta origen")
        }
        
        // Usar transacción para garantizar atomicidad
        return try {
            val queries = listOf(
                "UPDATE accounts SET balance = balance - ? WHERE id = ?" to arrayOf<Any>(monto, cuentaOrigenId),
                "UPDATE accounts SET balance = balance + ? WHERE id = ?" to arrayOf<Any>(monto, cuentaDestinoId)
            )
            
            dbManager.executeTransaction(queries)
            true
        } catch (e: SQLException) {
            throw SQLException("Error al transferir: ${e.message}")
        }
    }
    
    /**
     * Verifica si existe un usuario
     */
    private fun existeUsuario(userId: Long): Boolean {
        return dbManager.exists("users", "id = ?", userId)
    }
    
    /**
     * Mapea un ResultSet a un objeto Cuenta
     */
    private fun mapearCuenta(rs: ResultSet): Cuenta {
        return Cuenta(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            balance = rs.getDouble("balance"),
            currency = rs.getString("currency")
        )
    }
}