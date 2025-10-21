package org.example.database

import java.sql.SQLException

/**
 * Utilidades y extensiones para facilitar el trabajo con la base de datos
 * CORREGIDO: Usa DatabaseManager como object singleton
 */
object DatabaseUtils {
    
    /**
     * Verifica la salud de la base de datos
     * @return true si todas las tablas principales existen y son accesibles
     */
    fun checkDatabaseHealth(): Boolean {
        val requiredTables = listOf("users", "accounts", "transactions")
        
        return try {
            requiredTables.all { tableName ->
                try {
                    DatabaseManager.countRecords(tableName)
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
        val tables = listOf("users", "accounts", "transactions")
        val stats = mutableMapOf<String, Int>()
        
        tables.forEach { table ->
            try {
                stats[table] = DatabaseManager.countRecords(table)
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
        return try {
            val queries = listOf(
                "DELETE FROM transactions" to emptyArray<Any>(),
                "DELETE FROM accounts" to emptyArray<Any>(),
                "DELETE FROM users" to emptyArray<Any>()
            )
            
            DatabaseManager.executeTransaction(queries)
            println("🧹 Datos de prueba eliminados")
            true
        } catch (e: SQLException) {
            println("❌ Error limpiando datos: ${e.message}")
            false
        }
    }
    
    /**
     * Inserta datos de prueba básicos (SOLO si no existen)
     */
    fun insertTestData(): Boolean {
        return try {
            // Verificar si ya hay datos
            val userCount = DatabaseManager.countRecords("users")
            if (userCount > 0) {
                println("ℹ️ Ya existen datos en la base de datos")
                return true
            }
            
            // Insertar usuarios de prueba
            val userId1 = DatabaseManager.executeInsert(
                "INSERT INTO users (full_name, email, password_hash, user_type) VALUES (?, ?, ?, ?)",
                "Juan Pérez",
                "juan@test.com",
                "hash_juan_4",
                "CUSTOMER"
            )
            
            val userId2 = DatabaseManager.executeInsert(
                "INSERT INTO users (full_name, email, password_hash, user_type) VALUES (?, ?, ?, ?)",
                "María García",
                "maria@test.com",
                "hash_maria_6",
                "CUSTOMER"
            )
            
            // Insertar cuentas de prueba
            DatabaseManager.executeInsert(
                "INSERT INTO accounts (user_id, balance, currency) VALUES (?, ?, ?)",
                userId1,
                1000.0,
                "ARS"
            )
            
            DatabaseManager.executeInsert(
                "INSERT INTO accounts (user_id, balance, currency) VALUES (?, ?, ?)",
                userId2,
                500.0,
                "ARS"
            )
            
            println("📊 Datos de prueba insertados correctamente")
            true
        } catch (e: Exception) {
            println("ℹ️ Datos ya existen o error al insertar: ${e.message}")
            true
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