package org.example.service

import org.example.database.DatabaseManager
import org.example.model.Cuenta
import java.sql.ResultSet
import java.sql.SQLException

/**
 * CORREGIDO: Ya no recibe DatabaseManager como parámetro
 */
class CuentaService {
    
    fun crearCuenta(userId: Long, balanceInicial: Double = 0.0): Long {
        if (balanceInicial < 0) {
            throw IllegalArgumentException("El balance inicial no puede ser negativo")
        }
        
        return DatabaseManager.executeInsert(
            "INSERT INTO accounts (user_id, balance, currency) VALUES (?, ?, ?)",
            userId,
            balanceInicial,
            "ARS"
        )
    }
    
    fun buscarPorId(id: Long): Cuenta? {
        return try {
            DatabaseManager.executeQuery(
                "SELECT * FROM accounts WHERE id = ?",
                id
            ) { rs ->
                if (rs.next()) {
                    mapearCuenta(rs)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            println("Error buscando cuenta: ${e.message}")
            null
        }
    }
    
    fun obtenerCuentasPorUsuario(userId: Long): List<Cuenta> {
        return try {
            DatabaseManager.executeQuery(
                "SELECT * FROM accounts WHERE user_id = ?",
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
    
    fun depositar(cuentaId: Long, monto: Double): Boolean {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        return try {
            val rowsAffected = DatabaseManager.executeUpdate(
                "UPDATE accounts SET balance = balance + ? WHERE id = ?",
                monto,
                cuentaId
            )
            rowsAffected > 0
        } catch (e: SQLException) {
            throw SQLException("Error al depositar: ${e.message}", e)
        }
    }
    
    fun retirar(cuentaId: Long, monto: Double): Boolean {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val cuenta = buscarPorId(cuentaId)
            ?: throw IllegalArgumentException("Cuenta no encontrada")
        
        if (cuenta.balance < monto) {
            throw IllegalArgumentException("Saldo insuficiente")
        }
        
        return try {
            val rowsAffected = DatabaseManager.executeUpdate(
                "UPDATE accounts SET balance = balance - ? WHERE id = ?",
                monto,
                cuentaId
            )
            rowsAffected > 0
        } catch (e: SQLException) {
            throw SQLException("Error al retirar: ${e.message}", e)
        }
    }
    
    fun transferir(cuentaOrigenId: Long, cuentaDestinoId: Long, monto: Double): Boolean {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        if (cuentaOrigenId == cuentaDestinoId) {
            throw IllegalArgumentException("No se puede transferir a la misma cuenta")
        }
        
        val cuentaOrigen = buscarPorId(cuentaOrigenId)
            ?: throw IllegalArgumentException("Cuenta origen no encontrada")
        
        val cuentaDestino = buscarPorId(cuentaDestinoId)
            ?: throw IllegalArgumentException("Cuenta destino no encontrada")
        
        if (cuentaOrigen.balance < monto) {
            throw IllegalArgumentException("Saldo insuficiente en cuenta origen")
        }
        
        return try {
            val queries = listOf(
                "UPDATE accounts SET balance = balance - ? WHERE id = ?" to arrayOf<Any>(monto, cuentaOrigenId),
                "UPDATE accounts SET balance = balance + ? WHERE id = ?" to arrayOf<Any>(monto, cuentaDestinoId)
            )
            
            DatabaseManager.executeTransaction(queries)
        } catch (e: SQLException) {
            throw SQLException("Error en la transferencia: ${e.message}", e)
        }
    }
    
    private fun mapearCuenta(rs: ResultSet): Cuenta {
        return Cuenta(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            balance = rs.getDouble("balance"),
            currency = rs.getString("currency")
        )
    }
}