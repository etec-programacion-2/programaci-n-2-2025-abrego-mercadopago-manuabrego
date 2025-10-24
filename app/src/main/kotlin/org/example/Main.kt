package org.example

import org.example.database.DatabaseConnection
import org.example.database.DatabaseUtils
import org.example.ui.MenuPrincipal
import org.example.util.Logger

fun main() {
    Logger.info("=== INICIANDO APLICACION ===")
    println("🏦 Iniciando Billetera Virtual...")
    
    try {
        Logger.debug("Verificando salud de la base de datos...")
        
        // Verificar que la base de datos esté funcionando
        if (!DatabaseUtils.checkDatabaseHealth()) {
            Logger.error("La base de datos no está disponible")
            println("❌ Error: La base de datos no está disponible")
            println("💡 Ejecuta primero: ./setup_db.sh")
            return
        }
        
        Logger.info("Base de datos conectada correctamente")
        println("✅ Base de datos conectada correctamente\n")
        
        Logger.debug("Iniciando MenuPrincipal...")
        
        // Iniciar el menú principal
        val menu = MenuPrincipal()
        Logger.debug("MenuPrincipal creado, llamando a iniciar()...")
        menu.iniciar()
        
        Logger.info("MenuPrincipal finalizado correctamente")
        
    } catch (e: OutOfMemoryError) {
        Logger.error("OutOfMemoryError detectado!", e)
        println("\n❌ ERROR: Se quedó sin memoria")
        println("Posible loop infinito detectado")
        e.printStackTrace()
    } catch (e: Exception) {
        Logger.error("Error crítico en main", e)
        println("❌ Error crítico: ${e.message}")
        e.printStackTrace()
    } finally {
        Logger.debug("Cerrando conexión a base de datos...")
        DatabaseConnection.closeConnection()
        Logger.info("=== APLICACION FINALIZADA ===")
    }
}