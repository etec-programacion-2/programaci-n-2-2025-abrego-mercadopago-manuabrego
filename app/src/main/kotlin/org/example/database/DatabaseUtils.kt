package org.example.database

import java.sql.SQLException

/**
 * Utilidades y extensiones para facilitar el trabajo con la base de datos
 */
object DatabaseUtils {
    
    /**
     * Verifica la salud de la base de datos
     * @return true si todas las tablas principales existen y son accesibles
     */
    fun checkDatabaseHealth(): Boolean {
        val dbManager = DatabaseManager()
        val requiredTables = listOf("users", "accounts", "transactions")
        
        return try {
            requiredTables.all { tableName ->
                try {
                    dbManager.countRecords(tableName)
                    println("✅ Tabla '$tableName': Accesible")
                    true
                } catch (e: SQLException) {
                    println("❌ Tabla '$tableName': Error - ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            println("❌ Error verificando salud de la base de datos: ${e.message}")
            false
        }
    }
    
    /**
     * Obtiene estadísticas básicas de la base de datos
     * @return Map con el conteo de registros por tabla
     */
    fun getDatabaseStats(): Map<String, Int> {
        val dbManager = DatabaseManager()
        val tables = listOf("users", "accounts", "transactions")
        val stats = mutableMapOf<String, Int>()
        
        tables.forEach { table ->
            try {
                stats[table] = dbManager.countRecords(table)
            } catch (e: SQLException) {
                stats[table] = -1 // Indica error
            }
        }
        
        return stats
    }
    
    /**
     * Limpia datos de prueba de la base de datos
     * ⚠️ CUIDADO: Esta operación elimina TODOS los datos
     */
    fun clearTestData(): Boolean {
        val dbManager = DatabaseManager()
        
        return try {
            val queries = listOf(
                "DELETE FROM transactions" to emptyArray<Any>(),
                "DELETE FROM accounts" to emptyArray<Any>(),
                "DELETE FROM users" to emptyArray<Any>()
            )
            
            dbManager.executeTransaction(queries)
            println("🧹 Datos de prueba eliminados")
            true
        } catch (e: SQLException) {
            println("❌ Error limpiando datos: ${e.message}")
            false
        }
    }
    
    /**
     * Inserta datos de prueba básicos
     */
    fun insertTestData(): Boolean {
        val dbManager = DatabaseManager()
        
        return try {
            // Verificar si ya hay datos
            val userCount = dbManager.countRecords("users")
            if (userCount > 0) {
                println("ℹ️ Ya existen datos en la base de datos")
                return true
            }
            
            // Insertar usuarios de prueba
            val userId1 = dbManager.executeInsert(
                "INSERT INTO users (full_name, email, password_hash, user_type) VALUES (?, ?, ?, ?)",
                "Juan Pérez", "juan@test.com", "hash123", "CUSTOMER"
            )
            
            val userId2 = dbManager.executeInsert(
                "INSERT INTO users (full_name, email, password_hash, user_type) VALUES (?, ?, ?, ?)",
                "María García", "maria@test.com", "hash456", "CUSTOMER"
            )
            
            // Insertar cuentas de prueba
            val accountId1 = dbManager.executeInsert(
                "INSERT INTO accounts (user_id, balance, currency) VALUES (?, ?, ?)",
                userId1, 1000.0, "ARS"
            )
            
            val accountId2 = dbManager.executeInsert(
                "INSERT INTO accounts (user_id, balance, currency) VALUES (?, ?, ?)",
                userId2, 500.0, "ARS"
            )
            
            // Insertar transacción de prueba
            dbManager.executeInsert(
                "INSERT INTO transactions (sender_account_id, receiver_account_id, amount, currency, type, description, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                accountId1, accountId2, 100.0, "ARS", "TRANSFER", "Transferencia de prueba", "COMPLETED"
            )
            
            println("📊 Datos de prueba insertados correctamente")
            true
        } catch (e: SQLException) {
            println("❌ Error insertando datos de prueba: ${e.message}")
            false
        }
    }
    
    /**
     * Muestra un resumen detallado de la base de datos
     */
    fun showDatabaseSummary() {
        println("📋 RESUMEN DE BASE DE DATOS")
        println("=" * 40)
        
        val stats = getDatabaseStats()
        var totalRecords = 0
        
        stats.forEach { (table, count) ->
            val emoji = when(table) {
                "users" -> "👥"
                "accounts" -> "💰"
                "transactions" -> "💸"
                else -> "📊"
            }
            
            if (count >= 0) {
                println("$emoji $table: $count registros")
                totalRecords += count
            } else {
                println("$emoji $table: Error al acceder")
            }
        }
        
        println("-" * 40)
        println("📈 Total de registros: $totalRecords")
        println("🔗 Estado de conexión: ${if (DatabaseConnection.isConnectionActive()) "Activa" else "Inactiva"}")
    }
}

// Extensión para repetir strings
private operator fun String.times(n: Int): String = this.repeat(n)