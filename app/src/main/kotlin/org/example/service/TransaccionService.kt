package org.example.service

import org.example.database.DatabaseManager
import org.example.model.Transaccion
import org.example.model.TipoTransaccion
import org.example.model.EstadoTransaccion
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Servicio para gestionar transacciones
 * Aplica principio Single Responsibility: solo maneja lógica de transacciones
 */
class TransaccionService(
    private val dbManager: DatabaseManager = DatabaseManager(),
    private val cuentaService: CuentaService = CuentaService(dbManager)
) {
    
    /**
     * Registra una nueva transacción y ejecuta la operación
     * @return ID de la transacción creada
     */
    fun registrarTransferencia(
        cuentaOrigenId: Long,
        cuentaDestinoId: Long,
        monto: Double,
        descripcion: String? = null
    ): Long {
        // Validar datos
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        // Crear transacción en estado PENDING
        val transaccionId = dbManager.executeInsert(
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
            // Ejecutar la transferencia
            cuentaService.transferir(cuentaOrigenId, cuentaDestinoId, monto)
            
            // Actualizar estado a COMPLETED
            actualizarEstado(transaccionId, EstadoTransaccion.COMPLETED)
            
            transaccionId
        } catch (e: Exception) {
            // Actualizar estado a FAILED
            actualizarEstado(transaccionId, EstadoTransaccion.FAILED)
            throw e
        }
    }
    
    /**
     * Registra un depósito
     */
    fun registrarDeposito(
        cuentaId: Long,
        monto: Double,
        descripcion: String? = null
    ): Long {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val transaccionId = dbManager.executeInsert(
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
    
    /**
     * Registra un retiro
     */
    fun registrarRetiro(
        cuentaId: Long,
        monto: Double,
        descripcion: String? = null
    ): Long {
        if (monto <= 0) {
            throw IllegalArgumentException("El monto debe ser mayor a 0")
        }
        
        val transaccionId = dbManager.executeInsert(
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
    
    /**
     * Obtiene el historial de transacciones de una cuenta
     * @param cuentaId ID de la cuenta
     * @param limit Número máximo de transacciones a retornar
     * @return Lista de transacciones ordenadas por fecha (más recientes primero)
     */
    fun obtenerHistorial(cuentaId: Long, limit: Int = 10): List<Transaccion> {
        return try {
            dbManager.executeQuery(
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
    
    /**
     * Obtiene todas las transacciones de un usuario (de todas sus cuentas)
     */
    fun obtenerHistorialPorUsuario(userId: Long, limit: Int = 20): List<Transaccion> {
        return try {
            dbManager.executeQuery(
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
    
    /**
     * Actualiza el estado de una transacción
     */
    private fun actualizarEstado(transaccionId: Long, estado: EstadoTransaccion) {
        try {
            dbManager.executeUpdate(
                "UPDATE transactions SET status = ? WHERE id = ?",
                estado.name,
                transaccionId
            )
        } catch (e: SQLException) {
            println("Error actualizando estado de transacción: ${e.message}")
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Transaccion
     */
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