package org.example.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para Cuenta
 * Representa una cuenta bancaria en la billetera virtual
 */
data class Cuenta(
    val id: Long = 0,
    val userId: Long,
    val balance: Double,
    val currency: String = "ARS",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Verifica si hay fondos suficientes para un monto dado
     */
    fun tieneFondosSuficientes(monto: Double): Boolean {
        return balance >= monto && monto > 0
    }
    
    /**
     * Valida que el balance no sea negativo
     */
    fun esBalanceValido(): Boolean {
        return balance >= 0
    }
    
    /**
     * Formatea el balance con símbolo de moneda
     */
    fun balanceFormateado(): String {
        return when (currency) {
            "ARS" -> "$ %.2f ARS".format(balance)
            "USD" -> "$ %.2f USD".format(balance)
            "EUR" -> "€ %.2f EUR".format(balance)
            else -> "%.2f %s".format(balance, currency)
        }
    }
    
    override fun toString(): String {
        return "Cuenta(id=$id, userId=$userId, balance=${balanceFormateado()})"
    }
}