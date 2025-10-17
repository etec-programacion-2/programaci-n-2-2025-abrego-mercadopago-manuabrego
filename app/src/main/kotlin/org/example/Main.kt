package org.example

import org.example.database.DatabaseConnection
import org.example.database.DatabaseUtils
import org.example.ui.MenuPrincipal

fun main() {
    println("🏦 Iniciando Billetera Virtual...")
    
    try {
        // Verificar que la base de datos esté funcionando
        if (!DatabaseUtils.checkDatabaseHealth()) {
            println("❌ Error: La base de datos no está disponible")
            println("💡 Ejecuta primero: ./setup_db.sh")
            return
        }
        
        println("✅ Base de datos conectada correctamente\n")
        
        // NO insertar datos de prueba (ya existen del setup_db.sh)
        // DatabaseUtils.insertTestData()
        
        // Iniciar el menú principal
        val menu = MenuPrincipal()
        menu.iniciar()
        
    } catch (e: Exception) {
        println("❌ Error crítico: ${e.message}")
        e.printStackTrace()
    } finally {
        // Cerrar conexión al finalizar
        DatabaseConnection.closeConnection()
    }
}