package org.example.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.io.File

/**
 * Clase Singleton para manejar la conexión a la base de datos SQLite
 * Implementa el patrón Singleton thread-safe usando object en Kotlin
 */
object DatabaseConnection {
    // La ruta cambia dependiendo desde dónde se ejecuta
    // Si se ejecuta desde la raíz: app/database/billetera.db
    // Si se ejecuta desde app/: database/billetera.db
    private const val DATABASE_PATH_FROM_ROOT = "app/database/billetera.db"
    private const val DATABASE_PATH_FROM_APP = "database/billetera.db"
    
    @Volatile
    private var connection: Connection? = null
    
    /**
     * Determina la ruta correcta de la base de datos
     */
    private fun getDatabasePath(): String {
        val fromApp = File(DATABASE_PATH_FROM_APP)
        val fromRoot = File(DATABASE_PATH_FROM_ROOT)
        
        return when {
            fromApp.exists() -> {
                println("🔍 Base de datos encontrada en: $DATABASE_PATH_FROM_APP")
                DATABASE_PATH_FROM_APP
            }
            fromRoot.exists() -> {
                println("🔍 Base de datos encontrada en: $DATABASE_PATH_FROM_ROOT")
                DATABASE_PATH_FROM_ROOT
            }
            else -> {
                println("⚠️ Base de datos no encontrada. Intentando con: $DATABASE_PATH_FROM_APP")
                DATABASE_PATH_FROM_APP
            }
        }
    }
    
    /**
     * Obtiene la conexión singleton a la base de datos
     * @return Connection activa y válida
     * @throws SQLException si no se puede establecer la conexión
     */
    @Synchronized
    fun getConnection(): Connection {
        // Verificar si la conexión existe y está activa
        if (connection == null || connection!!.isClosed) {
            connection = createConnection()
        }
        return connection!!
    }
    
    /**
     * Crea una nueva conexión a la base de datos
     * @return Connection nueva
     * @throws SQLException si la base de datos no existe o hay problemas de conexión
     */
    private fun createConnection(): Connection {
        val dbPath = getDatabasePath()
        val dbFile = File(dbPath)
        
        if (!dbFile.exists()) {
            throw SQLException(
                "Base de datos no encontrada en: ${dbFile.absolutePath}.\n" +
                "Buscado en:\n" +
                "  - $DATABASE_PATH_FROM_APP\n" +
                "  - $DATABASE_PATH_FROM_ROOT\n" +
                "Ejecuta primero el script: ./setup_db.sh"
            )
        }
        
        return try {
            val connectionUrl = "jdbc:sqlite:$dbPath"
            val conn = DriverManager.getConnection(connectionUrl)
            
            // Configurar la conexión para mejor rendimiento y seguridad
            conn.autoCommit = true
            
            // Habilitar foreign keys en SQLite
            val stmt = conn.createStatement()
            stmt.execute("PRAGMA foreign_keys = ON")
            stmt.close()
            
            println("✅ Conexión a base de datos establecida correctamente")
            println("📂 Ruta: ${dbFile.absolutePath}")
            conn
        } catch (e: SQLException) {
            throw SQLException("Error al conectar con la base de datos: ${e.message}", e)
        }
    }
    
    /**
     * Cierra la conexión actual si existe
     */
    @Synchronized
    fun closeConnection() {
        try {
            connection?.let {
                if (!it.isClosed) {
                    it.close()
                    println("🔒 Conexión a base de datos cerrada")
                }
            }
        } catch (e: SQLException) {
            println("⚠️ Error al cerrar la conexión: ${e.message}")
        } finally {
            connection = null
        }
    }
    
    /**
     * Verifica si la conexión está activa
     * @return true si la conexión está activa, false en caso contrario
     */
    fun isConnectionActive(): Boolean {
        return try {
            connection?.let { !it.isClosed } ?: false
        } catch (e: SQLException) {
            false
        }
    }
    
    /**
     * Obtiene información sobre la conexión actual
     * @return String con información de la conexión
     */
    fun getConnectionInfo(): String {
        return try {
            val conn = getConnection()
            val meta = conn.metaData
            "Database: ${meta.databaseProductName} ${meta.databaseProductVersion}, " +
            "Driver: ${meta.driverName} ${meta.driverVersion}"
        } catch (e: SQLException) {
            "Error obteniendo información: ${e.message}"
        }
    }
}