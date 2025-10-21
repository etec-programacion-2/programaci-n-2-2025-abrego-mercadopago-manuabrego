package org.example.service

import org.example.database.DatabaseManager
import org.example.model.Transaccion
import org.example.model.TipoTransaccion
import org.example.model.EstadoTransaccion
import java.sql.ResultSet
import java.sql.SQLException

/**
 * CORREGIDO: Ya no crea múltiples instancias de CuentaService
 */
class TransaccionService {
    private val cuentaService = CuentaService()
    
    fun registrarTransferencia(
        cuentaOrigenId: Long,
        cuentaDestinoId: Long,
        monto: Double,
        descripcion: String? = null
    ): Long {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val transaccionId = DatabaseManager.executeInsert(
            """INSERT INTO transactions 
               (sender_account_id, receiver_account_id, amount, currency, type, description, status) 
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            cuentaOrigenId,
            cuentaDestinoId,
            monto,
            "ARS",
            TipoTransaccion.TRANSFER.name,
            descripcion ?: "Transferencia",
            EstadoTransaccion.PENDING.name
        )
        
        return try {
            cuentaService.transferir(cuentaOrigenId, cuentaDestinoId, monto)
            actualizarEstado(transaccionId, EstadoTransaccion.COMPLETED)
            transaccionId
        } catch (e: Exception) {
            actualizarEstado(transaccionId, EstadoTransaccion.FAILED)
            throw e
        }
    }
    
    fun registrarDeposito(
        cuentaId: Long,
        monto: Double,
        descripcion: String? = null
    ): Long {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val transaccionId = DatabaseManager.executeInsert(
            """INSERT INTO transactions 
               (receiver_account_id, amount, currency, type, description, status) 
               VALUES (?, ?, ?, ?, ?, ?)""",
            cuentaId,
            monto,
            "ARS",
            TipoTransaccion.DEPOSIT.name,
            descripcion ?: "Depósito",
            EstadoTransaccion.PENDING.name
        )
        
        return try {
            cuentaService.depositar(cuentaId, monto)
            actualizarEstado(transaccionId, EstadoTransaccion.COMPLETED)
            transaccionId
        } catch (e: Exception) {
            actualizarEstado(transaccionId, EstadoTransaccion.FAILED)
            throw e
        }
    }
    
    fun registrarRetiro(
        cuentaId: Long,
        monto: Double,
        descripcion: String? = null
    ): Long {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val transaccionId = DatabaseManager.executeInsert(
            """INSERT INTO transactions 
               (sender_account_id, amount, currency, type, description, status) 
               VALUES (?, ?, ?, ?, ?, ?)""",
            cuentaId,
            monto,
            "ARS",
            TipoTransaccion.WITHDRAWAL.name,
            descripcion ?: "Retiro",
            EstadoTransaccion.PENDING.name
        )
        
        return try {
            cuentaService.retirar(cuentaId, monto)
            actualizarEstado(transaccionId, EstadoTransaccion.COMPLETED)
            transaccionId
        } catch (e: Exception) {
            actualizarEstado(transaccionId, EstadoTransaccion.FAILED)
            throw e
        }
    }
    
    fun obtenerHistorial(cuentaId: Long, limit: Int = 10): List<Transaccion> {
        return try {
            DatabaseManager.executeQuery(
                """SELECT * FROM transactions 
                   WHERE sender_account_id = ? OR receiver_account_id = ? 
                   ORDER BY created_at DESC 
                   LIMIT ?""",
                cuentaId,
                cuentaId,
                limit
            ) { rs ->
                buildList {
                    while (rs.next()) {
                        add(mapearTransaccion(rs))
                    }
                }
            }
        } catch (e: SQLException) {
            println("Error obteniendo historial: ${e.message}")
            emptyList()
        }
    }
    
    fun obtenerHistorialPorUsuario(userId: Long, limit: Int = 20): List<Transaccion> {
        return try {
            DatabaseManager.executeQuery(
                """SELECT t.* FROM transactions t
                   INNER JOIN accounts a ON (t.sender_account_id = a.id OR t.receiver_account_id = a.id)
                   WHERE a.user_id = ?
                   ORDER BY t.created_at DESC
                   LIMIT ?""",
                userId,
                limit
            ) { rs ->
                buildList {
                    while (rs.next()) {
                        add(mapearTransaccion(rs))
                    }
                }
            }
        } catch (e: SQLException) {
            println("Error obteniendo historial por usuario: ${e.message}")
            emptyList()
        }
    }
    
    private fun actualizarEstado(transaccionId: Long, estado: EstadoTransaccion) {
        try {
            DatabaseManager.executeUpdate(
                "UPDATE transactions SET status = ? WHERE id = ?",
                estado.name,
                transaccionId
            )
        } catch (e: SQLException) {
            println("Error actualizando estado de transacción: ${e.message}")
        }
    }
    
    private fun mapearTransaccion(rs: ResultSet): Transaccion {
        return Transaccion(
            id = rs.getLong("id"),
            senderAccountId = rs.getObject("sender_account_id") as? Long,
            receiverAccountId = rs.getObject("receiver_account_id") as? Long,
            amount = rs.getDouble("amount"),
            currency = rs.getString("currency"),
            type = TipoTransaccion.fromString(rs.getString("type")),
            description = rs.getString("description"),
            status = EstadoTransaccion.fromString(rs.getString("status"))
        )
    }
}