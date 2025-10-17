package org.example.ui

import org.example.model.Cuenta
import org.example.model.Transaccion
import org.example.model.Usuario
import org.example.model.EstadoTransaccion

/**
 * Utilidades para la interfaz de consola
 * Aplica principio Open/Closed: fácil de extender sin modificar
 */
object ConsoleUI {
    
    /**
     * Lee un texto del usuario
     */
    fun leerTexto(mensaje: String): String {
        print("$mensaje: ")
        return readLine()?.trim() ?: ""
    }
    
    /**
     * Lee un número entero del usuario con validación
     */
    fun leerEntero(mensaje: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int? {
        print("$mensaje: ")
        return try {
            val valor = readLine()?.trim()?.toInt()
            if (valor != null && valor in min..max) {
                valor
            } else {
                println("❌ El valor debe estar entre $min y $max")
                null
            }
        } catch (e: NumberFormatException) {
            println("❌ Debe ingresar un número válido")
            null
        }
    }
    
    /**
     * Lee un número decimal del usuario con validación
     */
    fun leerDouble(mensaje: String, min: Double = 0.0): Double? {
        print("$mensaje: ")
        return try {
            val valor = readLine()?.trim()?.toDouble()
            if (valor != null && valor >= min) {
                valor
            } else {
                println("❌ El valor debe ser mayor o igual a $min")
                null
            }
        } catch (e: NumberFormatException) {
            println("❌ Debe ingresar un número válido")
            null
        }
    }
    
    /**
     * Lee un ID (Long) del usuario
     */
    fun leerLong(mensaje: String): Long? {
        print("$mensaje: ")
        return try {
            readLine()?.trim()?.toLong()
        } catch (e: NumberFormatException) {
            println("❌ Debe ingresar un ID válido")
            null
        }
    }
    
    /**
     * Muestra un mensaje de éxito
     */
    fun mostrarExito(mensaje: String) {
        println("\n✅ $mensaje\n")
    }
    
    /**
     * Muestra un mensaje de error
     */
    fun mostrarError(mensaje: String) {
        println("\n❌ $mensaje\n")
    }
    
    /**
     * Muestra un mensaje de advertencia
     */
    fun mostrarAdvertencia(mensaje: String) {
        println("\n⚠️  $mensaje\n")
    }
    
    /**
     * Muestra un mensaje informativo
     */
    fun mostrarInfo(mensaje: String) {
        println("\nℹ️  $mensaje\n")
    }
    
    /**
     * Muestra un título con formato
     */
    fun mostrarTitulo(titulo: String) {
        val separador = "=".repeat(titulo.length + 4)
        println("\n$separador")
        println("  $titulo")
        println("$separador\n")
    }
    
    /**
     * Muestra un separador visual
     */
    fun mostrarSeparador() {
        println("-".repeat(50))
    }
    
    /**
     * Limpia la pantalla (simulado)
     */
    fun limpiarPantalla() {
        repeat(50) { println() }
    }
    
    /**
     * Pausa la ejecución hasta que el usuario presione Enter
     */
    fun pausar() {
        print("\nPresione Enter para continuar...")
        readLine()
    }
    
    /**
     * Muestra información de un usuario con formato
     */
    fun mostrarUsuario(usuario: Usuario) {
        println("👤 Usuario:")
        println("   ID: ${usuario.id}")
        println("   Nombre: ${usuario.fullName}")
        println("   Email: ${usuario.email}")
        println("   Tipo: ${usuario.userType}")
    }
    
    /**
     * Muestra información de una cuenta con formato
     */
    fun mostrarCuenta(cuenta: Cuenta, numero: Int? = null) {
        val prefijo = if (numero != null) "$numero. " else ""
        println("${prefijo}💰 Cuenta ID: ${cuenta.id}")
        println("   Balance: ${cuenta.balanceFormateado()}")
    }
    
    /**
     * Muestra una lista de cuentas
     */
    fun mostrarListaCuentas(cuentas: List<Cuenta>) {
        if (cuentas.isEmpty()) {
            mostrarAdvertencia("No hay cuentas disponibles")
            return
        }
        
        println("\n📋 Tus cuentas:")
        mostrarSeparador()
        for (index in cuentas.indices) {
            val cuenta = cuentas[index]
            mostrarCuenta(cuenta, index + 1)
            if (index < cuentas.size - 1) mostrarSeparador()
        }
    }
    
    /**
     * Muestra información de una transacción con formato
     */
    fun mostrarTransaccion(transaccion: Transaccion, cuentaActualId: Long) {
        val esEnvio = transaccion.senderAccountId == cuentaActualId
        val esRecepcion = transaccion.receiverAccountId == cuentaActualId
        
        val simbolo = when {
            esEnvio -> "📤"
            esRecepcion -> "📥"
            else -> "💸"
        }
        
        val direccion = when {
            esEnvio -> "Enviado a cuenta ${transaccion.receiverAccountId}"
            esRecepcion -> "Recibido de cuenta ${transaccion.senderAccountId}"
            else -> transaccion.description ?: "Transacción"
        }
        
        val estadoEmoji = when (transaccion.status) {
            EstadoTransaccion.COMPLETED -> "✅"
            EstadoTransaccion.PENDING -> "⏳"
            EstadoTransaccion.FAILED -> "❌"
            EstadoTransaccion.CANCELLED -> "🚫"
        }
        
        println("$simbolo ${transaccion.type.name} - ${transaccion.montoFormateado()} $estadoEmoji")
        println("   $direccion")
        println("   Estado: ${transaccion.status.name}")
        if (transaccion.description != null) {
            println("   Descripción: ${transaccion.description}")
        }
    }
    
    /**
     * Muestra el historial de transacciones
     */
    fun mostrarHistorial(transacciones: List<Transaccion>, cuentaId: Long) {
        if (transacciones.isEmpty()) {
            mostrarAdvertencia("No hay transacciones registradas")
            return
        }
        
        println("\n📜 Historial de Transacciones:")
        mostrarSeparador()
        transacciones.forEachIndexed { index, transaccion ->
            println("\n${index + 1}.")
            mostrarTransaccion(transaccion, cuentaId)
            if (index < transacciones.size - 1) mostrarSeparador()
        }
    }
    
    /**
     * Confirma una acción con el usuario
     */
    fun confirmar(mensaje: String): Boolean {
        print("$mensaje (S/N): ")
        val respuesta = readLine()?.trim()?.uppercase()
        return respuesta == "S" || respuesta == "SI" || respuesta == "Y" || respuesta == "YES"
    }
}