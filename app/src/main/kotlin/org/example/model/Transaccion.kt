package org.example.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para Transacción
 * Representa una transacción financiera en el sistema
 */
data class Transaccion(
    val id: Long = 0,
    val senderAccountId: Long?,
    val receiverAccountId: Long?,
    val amount: Double,
    val currency: String = "ARS",
    val type: TipoTransaccion,
    val description: String?,
    val status: EstadoTransaccion = EstadoTransaccion.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Valida que el monto sea positivo
     */
    fun esMontoValido(): Boolean {
        return amount > 0
    }
    
    /**
     * Verifica si es una transferencia entre cuentas
     */
    fun esTransferencia(): Boolean {
        return type == TipoTransaccion.TRANSFER && 
               senderAccountId != null && 
               receiverAccountId != null
    }
    
    /**
     * Verifica si es un depósito
     */
    fun esDeposito(): Boolean {
        return type == TipoTransaccion.DEPOSIT && receiverAccountId != null
    }
    
    /**
     * Verifica si es un retiro
     */
    fun esRetiro(): Boolean {
        return type == TipoTransaccion.WITHDRAWAL && senderAccountId != null
    }
    
    /**
     * Formatea el monto con símbolo de moneda
     */
    fun montoFormateado(): String {
        return when (currency) {
            "ARS" -> "$ %.2f".format(amount)
            "USD" -> "$ %.2f".format(amount)
            "EUR" -> "€ %.2f".format(amount)
            else -> "%.2f %s".format(amount, currency)
        }
    }
    
    override fun toString(): String {
        return "Transaccion(id=$id, tipo=$type, monto=${montoFormateado()}, estado=$status)"
    }
}

/**
 * Enum para tipos de transacción
 */
enum class TipoTransaccion {
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL,
    PAYMENT;
    
    companion object {
        fun fromString(value: String): TipoTransaccion {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: TRANSFER
        }
    }
}

/**
 * Enum para estados de transacción
 */
enum class EstadoTransaccion {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED;
    
    companion object {
        fun fromString(value: String): EstadoTransaccion {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}