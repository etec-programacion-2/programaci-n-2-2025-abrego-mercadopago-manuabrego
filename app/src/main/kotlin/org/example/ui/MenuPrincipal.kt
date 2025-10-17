package org.example.ui

import org.example.model.Usuario
import org.example.service.CuentaService
import org.example.service.TransaccionService
import org.example.service.UsuarioService

/**
 * Menú principal de la aplicación
 */
class MenuPrincipal(
    private val usuarioService: UsuarioService = UsuarioService(),
    private val cuentaService: CuentaService = CuentaService(),
    private val transaccionService: TransaccionService = TransaccionService()
) {
    private var usuarioActual: Usuario? = null
    private var ejecutando = true
    
    /**
     * Inicia el menú principal
     */
    fun iniciar() {
        ConsoleUI.limpiarPantalla()
        mostrarBienvenida()
        
        while (ejecutando) {
            try {
                if (usuarioActual == null) {
                    menuNoAutenticado()
                } else {
                    menuAutenticado()
                }
            } catch (e: Exception) {
                ConsoleUI.mostrarError("Error: ${e.message}")
            }
        }
        
        mostrarDespedida()
    }
    
    private fun mostrarBienvenida() {
        ConsoleUI.mostrarTitulo("BILLETERA VIRTUAL")
        println("Bienvenido al sistema de billetera virtual\n")
    }
    
    private fun mostrarDespedida() {
        ConsoleUI.limpiarPantalla()
        ConsoleUI.mostrarTitulo("HASTA PRONTO")
        println("Gracias por usar la Billetera Virtual")
    }
    
    private fun menuNoAutenticado() {
        println("\n=== MENU PRINCIPAL ===")
        println("1. Iniciar sesion")
        println("2. Registrarse")
        println("3. Salir")
        print("\nSeleccione una opcion: ")
        
        val opcion = readLine()?.trim()
        
        when (opcion) {
            "1" -> iniciarSesion()
            "2" -> registrarUsuario()
            "3" -> ejecutando = false
            else -> ConsoleUI.mostrarError("Opcion invalida")
        }
    }
    
    private fun menuAutenticado() {
        val usuario = usuarioActual ?: return
        
        println("\n=== MENU PRINCIPAL ===")
        println("Usuario: ${usuario.fullName}")
        println("-".repeat(40))
        println("1. Crear cuenta")
        println("2. Consultar balance")
        println("3. Enviar dinero")
        println("4. Ver historial")
        println("5. Depositar dinero")
        println("6. Retirar dinero")
        println("7. Ver todas mis cuentas")
        println("8. Cerrar sesion")
        println("9. Salir")
        print("\nSeleccione una opcion: ")
        
        val opcion = readLine()?.trim()
        
        when (opcion) {
            "1" -> crearCuenta()
            "2" -> consultarBalance()
            "3" -> enviarDinero()
            "4" -> verHistorial()
            "5" -> depositarDinero()
            "6" -> retirarDinero()
            "7" -> verTodasLasCuentas()
            "8" -> cerrarSesion()
            "9" -> ejecutando = false
            else -> ConsoleUI.mostrarError("Opcion invalida")
        }
    }
    
    private fun iniciarSesion() {
        ConsoleUI.mostrarTitulo("INICIAR SESION")
        
        print("Email: ")
        val email = readLine()?.trim() ?: ""
        
        print("Contraseña: ")
        val password = readLine()?.trim() ?: ""
        
        if (email.isBlank() || password.isBlank()) {
            ConsoleUI.mostrarError("Email y contraseña no pueden estar vacios")
            return
        }
        
        val usuario = usuarioService.autenticar(email, password)
        
        if (usuario != null) {
            usuarioActual = usuario
            ConsoleUI.mostrarExito("Bienvenido ${usuario.fullName}!")
        } else {
            ConsoleUI.mostrarError("Credenciales incorrectas")
        }
    }
    
    private fun registrarUsuario() {
        ConsoleUI.mostrarTitulo("REGISTRARSE")
        
        print("Nombre completo: ")
        val nombre = readLine()?.trim() ?: ""
        
        print("Email: ")
        val email = readLine()?.trim() ?: ""
        
        print("Contraseña: ")
        val password = readLine()?.trim() ?: ""
        
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            ConsoleUI.mostrarError("Todos los campos son obligatorios")
            return
        }
        
        try {
            val userId = usuarioService.crearUsuario(nombre, email, password)
            ConsoleUI.mostrarExito("Usuario registrado con ID: $userId")
            usuarioActual = usuarioService.buscarPorId(userId)
        } catch (e: Exception) {
            ConsoleUI.mostrarError("Error: ${e.message}")
        }
    }
    
    private fun crearCuenta() {
        val usuario = usuarioActual ?: return
        
        ConsoleUI.mostrarTitulo("CREAR CUENTA")
        
        print("Balance inicial (presione Enter para 0): ")
        val balanceStr = readLine()?.trim()
        val balance = balanceStr?.toDoubleOrNull() ?: 0.0
        
        try {
            val cuentaId = cuentaService.crearCuenta(usuario.id, balance)
            ConsoleUI.mostrarExito("Cuenta creada con ID: $cuentaId")
        } catch (e: Exception) {
            ConsoleUI.mostrarError("Error: ${e.message}")
        }
        
        ConsoleUI.pausar()
    }
    
    private fun consultarBalance() {
        val usuario = usuarioActual ?: return
        
        ConsoleUI.mostrarTitulo("CONSULTAR BALANCE")
        
        val cuentas = cuentaService.obtenerCuentasPorUsuario(usuario.id)
        
        if (cuentas.isEmpty()) {
            ConsoleUI.mostrarAdvertencia("No tienes cuentas")
            ConsoleUI.pausar()
            return
        }
        
        ConsoleUI.mostrarListaCuentas(cuentas)
        
        print("\nID de la cuenta: ")
        val cuentaId = readLine()?.trim()?.toLongOrNull()
        
        if (cuentaId == null) {
            ConsoleUI.mostrarError("ID invalido")
            ConsoleUI.pausar()
            return
        }
        
        val cuenta = cuentaService.buscarPorId(cuentaId)
        
        if (cuenta == null || cuenta.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta no valida")
        } else {
            println("\nBalance: ${cuenta.balanceFormateado()}")
        }
        
        ConsoleUI.pausar()
    }
    
    private fun enviarDinero() {
        val usuario = usuarioActual ?: return
        
        ConsoleUI.mostrarTitulo("ENVIAR DINERO")
        
        val cuentas = cuentaService.obtenerCuentasPorUsuario(usuario.id)
        
        if (cuentas.isEmpty()) {
            ConsoleUI.mostrarAdvertencia("No tienes cuentas")
            ConsoleUI.pausar()
            return
        }
        
        println("\nTus cuentas:")
        ConsoleUI.mostrarListaCuentas(cuentas)
        
        print("\nID de tu cuenta origen: ")
        val cuentaOrigenId = readLine()?.trim()?.toLongOrNull()
        if (cuentaOrigenId == null) return
        
        val cuentaOrigen = cuentaService.buscarPorId(cuentaOrigenId)
        if (cuentaOrigen == null || cuentaOrigen.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta origen invalida")
            ConsoleUI.pausar()
            return
        }
        
        print("ID de la cuenta destino: ")
        val cuentaDestinoId = readLine()?.trim()?.toLongOrNull()
        if (cuentaDestinoId == null) return
        
        val cuentaDestino = cuentaService.buscarPorId(cuentaDestinoId)
        if (cuentaDestino == null) {
            ConsoleUI.mostrarError("Cuenta destino no encontrada")
            ConsoleUI.pausar()
            return
        }
        
        print("Monto a enviar: ")
        val monto = readLine()?.trim()?.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            ConsoleUI.mostrarError("Monto invalido")
            ConsoleUI.pausar()
            return
        }
        
        println("\nResumen:")
        println("De: Cuenta $cuentaOrigenId (${cuentaOrigen.balanceFormateado()})")
        println("A: Cuenta $cuentaDestinoId")
        println("Monto: $monto ARS")
        
        print("\nConfirmar (S/N): ")
        val confirmacion = readLine()?.trim()?.uppercase()
        
        if (confirmacion != "S" && confirmacion != "SI") {
            ConsoleUI.mostrarInfo("Transferencia cancelada")
            ConsoleUI.pausar()
            return
        }
        
        try {
            val transaccionId = transaccionService.registrarTransferencia(
                cuentaOrigenId,
                cuentaDestinoId,
                monto
            )
            
            ConsoleUI.mostrarExito("Transferencia realizada. ID: $transaccionId")
            
            val cuentaActualizada = cuentaService.buscarPorId(cuentaOrigenId)
            if (cuentaActualizada != null) {
                println("Nuevo balance: ${cuentaActualizada.balanceFormateado()}")
            }
        } catch (e: Exception) {
            ConsoleUI.mostrarError("Error: ${e.message}")
        }
        
        ConsoleUI.pausar()
    }
    
    private fun verHistorial() {
        val usuario = usuarioActual ?: return
        
        ConsoleUI.mostrarTitulo("HISTORIAL DE TRANSACCIONES")
        
        val cuentas = cuentaService.obtenerCuentasPorUsuario(usuario.id)
        
        if (cuentas.isEmpty()) {
            ConsoleUI.mostrarAdvertencia("No tienes cuentas")
            ConsoleUI.pausar()
            return
        }
        
        ConsoleUI.mostrarListaCuentas(cuentas)
        
        print("\nID de la cuenta: ")
        val cuentaId = readLine()?.trim()?.toLongOrNull()
        if (cuentaId == null) return
        
        val cuenta = cuentaService.buscarPorId(cuentaId)
        if (cuenta == null || cuenta.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta invalida")
            ConsoleUI.pausar()
            return
        }
        
        val transacciones = transaccionService.obtenerHistorial(cuentaId, 10)
        ConsoleUI.mostrarHistorial(transacciones, cuentaId)
        ConsoleUI.pausar()
    }
    
    private fun depositarDinero() {
        val usuario = usuarioActual ?: return
        
        ConsoleUI.mostrarTitulo("DEPOSITAR DINERO")
        
        val cuentas = cuentaService.obtenerCuentasPorUsuario(usuario.id)
        
        if (cuentas.isEmpty()) {
            ConsoleUI.mostrarAdvertencia("No tienes cuentas")
            ConsoleUI.pausar()
            return
        }
        
        ConsoleUI.mostrarListaCuentas(cuentas)
        
        print("\nID de la cuenta: ")
        val cuentaId = readLine()?.trim()?.toLongOrNull()
        if (cuentaId == null) return
        
        val cuenta = cuentaService.buscarPorId(cuentaId)
        if (cuenta == null || cuenta.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta invalida")
            ConsoleUI.pausar()
            return
        }
        
        print("Monto a depositar: ")
        val monto = readLine()?.trim()?.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            ConsoleUI.mostrarError("Monto invalido")
            ConsoleUI.pausar()
            return
        }
        
        try {
            val transaccionId = transaccionService.registrarDeposito(cuentaId, monto)
            ConsoleUI.mostrarExito("Deposito realizado. ID: $transaccionId")
            
            val cuentaActualizada = cuentaService.buscarPorId(cuentaId)
            if (cuentaActualizada != null) {
                println("Nuevo balance: ${cuentaActualizada.balanceFormateado()}")
            }
        } catch (e: Exception) {
            ConsoleUI.mostrarError("Error: ${e.message}")
        }
        
        ConsoleUI.pausar()
    }
    
    private fun retirarDinero() {
        val usuario = usuarioActual ?: return
        
        ConsoleUI.mostrarTitulo("RETIRAR DINERO")
        
        val cuentas = cuentaService.obtenerCuentasPorUsuario(usuario.id)
        
        if (cuentas.isEmpty()) {
            ConsoleUI.mostrarAdvertencia("No tienes cuentas")
            ConsoleUI.pausar()
            return
        }
        
        ConsoleUI.mostrarListaCuentas(cuentas)
        
        print("\nID de la cuenta: ")
        val cuentaId = readLine()?.trim()?.toLongOrNull()
        if (cuentaId == null) return
        
        val cuenta = cuentaService.buscarPorId(cuentaId)
        if (cuenta == null || cuenta.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta invalida")
            ConsoleUI.pausar()
            return
        }
        
        println("\nBalance actual: ${cuenta.balanceFormateado()}")
        
        print("Monto a retirar: ")
        val monto = readLine()?.trim()?.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            ConsoleUI.mostrarError("Monto invalido")
            ConsoleUI.pausar()
            return
        }
        
        try {
            val transaccionId = transaccionService.registrarRetiro(cuentaId, monto)
            ConsoleUI.mostrarExito("Retiro realizado. ID: $transaccionId")
            
            val cuentaActualizada = cuentaService.buscarPorId(cuentaId)
            if (cuentaActualizada != null) {
                println("Nuevo balance: ${cuentaActualizada.balanceFormateado()}")
            }
        } catch (e: Exception) {
            ConsoleUI.mostrarError("Error: ${e.message}")
        }
        
        ConsoleUI.pausar()
    }
    
    private fun verTodasLasCuentas() {
        val usuario = usuarioActual ?: return
        
        ConsoleUI.mostrarTitulo("MIS CUENTAS")
        
        val cuentas = cuentaService.obtenerCuentasPorUsuario(usuario.id)
        
        if (cuentas.isEmpty()) {
            ConsoleUI.mostrarAdvertencia("No tienes cuentas")
            ConsoleUI.pausar()
            return
        }
        
        ConsoleUI.mostrarListaCuentas(cuentas)
        
        val balanceTotal = cuentas.sumOf { it.balance }
        println("\nBalance total: $balanceTotal ARS")
        
        ConsoleUI.pausar()
    }
    
    private fun cerrarSesion() {
        ConsoleUI.mostrarInfo("Sesion cerrada")
        usuarioActual = null
    }
}