package org.example.ui

import org.example.model.Usuario
import org.example.service.CuentaService
import org.example.service.TransaccionService
import org.example.service.UsuarioService
import org.example.util.Logger
import java.util.Scanner

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
    private var iteraciones = 0
    private val scanner = Scanner(System.`in`)
    
    /**
     * Lee una línea de forma segura, nunca retorna null
     */
    private fun leerLinea(): String {
        return try {
            scanner.nextLine().trim()
        } catch (e: Exception) {
            Logger.error("Error leyendo input", e)
            ""
        }
    }
    
    /**
     * Inicia el menú principal
     */
    fun iniciar() {
        Logger.debug("MenuPrincipal.iniciar() - INICIO")
        ConsoleUI.limpiarPantalla()
        mostrarBienvenida()
        
        Logger.debug("Entrando al loop principal del menú")
        while (ejecutando) {
            iteraciones++
            Logger.debug("Iteración #$iteraciones del menú")
            
            if (iteraciones > 1000) {
                Logger.error("LOOP INFINITO DETECTADO! Más de 1000 iteraciones")
                println("\n❌ ERROR: Loop infinito detectado")
                println("💡 Posible causa: Sistema sin soporte de input interactivo")
                break
            }
            
            try {
                if (usuarioActual == null) {
                    Logger.debug("Mostrando menuNoAutenticado")
                    menuNoAutenticado()
                } else {
                    Logger.debug("Mostrando menuAutenticado (usuario: ${usuarioActual?.email})")
                    menuAutenticado()
                }
                
                // Pequeña pausa para prevenir loops infinitos si readLine falla
                Thread.sleep(10)
            } catch (e: Exception) {
                Logger.error("Error en el loop del menú", e)
                ConsoleUI.mostrarError("Error: ${e.message}")
            }
        }
        
        Logger.debug("Saliendo del loop principal (ejecutando=$ejecutando)")
        scanner.close()
        mostrarDespedida()
        Logger.debug("MenuPrincipal.iniciar() - FIN")
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
        
        val opcion = leerLinea()
        
        when (opcion) {
            "1" -> iniciarSesion()
            "2" -> registrarUsuario()
            "3" -> ejecutando = false
            "" -> {
                Logger.warn("Opción vacía detectada")
                ConsoleUI.mostrarError("Por favor ingrese una opción válida")
            }
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
        
        val opcion = leerLinea()
        
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
            "" -> {
                Logger.warn("Opción vacía detectada")
                ConsoleUI.mostrarError("Por favor ingrese una opción válida")
            }
            else -> ConsoleUI.mostrarError("Opcion invalida")
        }
    }
    
    private fun iniciarSesion() {
        ConsoleUI.mostrarTitulo("INICIAR SESION")
        
        print("Email: ")
        val email = leerLinea()
        
        print("Contraseña: ")
        val password = leerLinea()
        
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
        val nombre = leerLinea()
        
        print("Email: ")
        val email = leerLinea()
        
        print("Contraseña: ")
        val password = leerLinea()
        
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
        val balanceStr = leerLinea()
        val balance = balanceStr.toDoubleOrNull() ?: 0.0
        
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
        val cuentaId = leerLinea().toLongOrNull()
        
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
        val cuentaOrigenId = leerLinea().toLongOrNull()
        if (cuentaOrigenId == null) return
        
        val cuentaOrigen = cuentaService.buscarPorId(cuentaOrigenId)
        if (cuentaOrigen == null || cuentaOrigen.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta origen invalida")
            ConsoleUI.pausar()
            return
        }
        
        print("ID de la cuenta destino: ")
        val cuentaDestinoId = leerLinea().toLongOrNull()
        if (cuentaDestinoId == null) return
        
        val cuentaDestino = cuentaService.buscarPorId(cuentaDestinoId)
        if (cuentaDestino == null) {
            ConsoleUI.mostrarError("Cuenta destino no encontrada")
            ConsoleUI.pausar()
            return
        }
        
        print("Monto a enviar: ")
        val monto = leerLinea().toDoubleOrNull()
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
        val confirmacion = leerLinea().uppercase()
        
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
        val cuentaId = leerLinea().toLongOrNull()
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
        val cuentaId = leerLinea().toLongOrNull()
        if (cuentaId == null) return
        
        val cuenta = cuentaService.buscarPorId(cuentaId)
        if (cuenta == null || cuenta.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta invalida")
            ConsoleUI.pausar()
            return
        }
        
        print("Monto a depositar: ")
        val monto = leerLinea().toDoubleOrNull()
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
        val cuentaId = leerLinea().toLongOrNull()
        if (cuentaId == null) return
        
        val cuenta = cuentaService.buscarPorId(cuentaId)
        if (cuenta == null || cuenta.userId != usuario.id) {
            ConsoleUI.mostrarError("Cuenta invalida")
            ConsoleUI.pausar()
            return
        }
        
        println("\nBalance actual: ${cuenta.balanceFormateado()}")
        
        print("Monto a retirar: ")
        val monto = leerLinea().toDoubleOrNull()
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