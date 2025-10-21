package org.example.database

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

/**
 * Singleton para manejar la ejecución de queries en la base de datos
 * CORREGIDO: Ahora es un object singleton para evitar múltiples instancias
 */
object DatabaseManager {
    
    /**
     * Ejecuta una query SELECT y procesa los resultados con una lambda
     * Los recursos se cierran automáticamente después del procesamiento
     */
    fun <T> executeQuery(sql: String, vararg parameters: Any, processor: (ResultSet) -> T): T {
        val connection = DatabaseConnection.getConnection()
        
        return connection.prepareStatement(sql).use { preparedStatement ->
            try {
                // Setear parámetros si existen
                parameters.forEachIndexed { index, param ->
                    setPreparedStatementParameter(preparedStatement, index + 1, param)
                }
                
                preparedStatement.executeQuery().use { resultSet ->
                    processor(resultSet)
                }
            } catch (e: SQLException) {
                throw SQLException("Error ejecutando query: $sql. ${e.message}", e)
            }
        }
    }
    
    /**
     * Ejecuta una query de modificación (INSERT, UPDATE, DELETE)
     */
    fun executeUpdate(sql: String, vararg parameters: Any): Int {
        val connection = DatabaseConnection.getConnection()
        
        return connection.prepareStatement(sql).use { preparedStatement ->
            try {
                parameters.forEachIndexed { index, param ->
                    setPreparedStatementParameter(preparedStatement, index + 1, param)
                }
                
                val result = preparedStatement.executeUpdate()
                println("📝 Query ejecutada: $sql, Filas afectadas: $result")
                result
            } catch (e: SQLException) {
                throw SQLException("Error ejecutando update: $sql. ${e.message}", e)
            }
        }
    }
    
    /**
     * Ejecuta una query INSERT y retorna el ID generado
     */
    fun executeInsert(sql: String, vararg parameters: Any): Long {
        val connection = DatabaseConnection.getConnection()
        
        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { preparedStatement ->
            try {
                parameters.forEachIndexed { index, param ->
                    setPreparedStatementParameter(preparedStatement, index + 1, param)
                }
                
                val affectedRows = preparedStatement.executeUpdate()
                
                if (affectedRows == 0) {
                    throw SQLException("No se pudo insertar el registro")
                }
                
                preparedStatement.generatedKeys.use { generatedKeys ->
                    if (generatedKeys.next()) {
                        val id = generatedKeys.getLong(1)
                        println("➕ Registro insertado con ID: $id")
                        id
                    } else {
                        throw SQLException("No se pudo obtener el ID generado")
                    }
                }
            } catch (e: SQLException) {
                throw SQLException("Error ejecutando insert: $sql. ${e.message}", e)
            }
        }
    }
    
    /**
     * Ejecuta múltiples queries en una transacción
     */
    fun executeTransaction(queries: List<Pair<String, Array<Any>>>): Boolean {
        val connection = DatabaseConnection.getConnection()
        val originalAutoCommit = connection.autoCommit
        
        try {
            connection.autoCommit = false
            
            queries.forEach { (sql, parameters) ->
                connection.prepareStatement(sql).use { preparedStatement ->
                    parameters.forEachIndexed { index, param ->
                        setPreparedStatementParameter(preparedStatement, index + 1, param)
                    }
                    preparedStatement.executeUpdate()
                }
            }
            
            connection.commit()
            println("✅ Transacción completada exitosamente")
            return true
            
        } catch (e: SQLException) {
            connection.rollback()
            println("❌ Error en transacción, rollback ejecutado: ${e.message}")
            throw e
        } finally {
            connection.autoCommit = originalAutoCommit
        }
    }
    
    /**
     * Cuenta el número de registros en una tabla
     */
    fun countRecords(tableName: String, whereClause: String? = null, vararg parameters: Any): Int {
        val sql = if (whereClause != null) {
            "SELECT COUNT(*) FROM $tableName WHERE $whereClause"
        } else {
            "SELECT COUNT(*) FROM $tableName"
        }
        
        return executeQuery(sql, *parameters) { resultSet ->
            if (resultSet.next()) {
                resultSet.getInt(1)
            } else {
                0
            }
        }
    }
    
    /**
     * Verifica si existe al menos un registro que cumpla la condición
     */
    fun exists(tableName: String, whereClause: String, vararg parameters: Any): Boolean {
        return countRecords(tableName, whereClause, *parameters) > 0
    }
    
    /**
     * Método privado para setear parámetros en PreparedStatement
     */
    private fun setPreparedStatementParameter(ps: PreparedStatement, index: Int, parameter: Any?) {
        when (parameter) {
            is String -> ps.setString(index, parameter)
            is Int -> ps.setInt(index, parameter)
            is Long -> ps.setLong(index, parameter)
            is Double -> ps.setDouble(index, parameter)
            is Boolean -> ps.setBoolean(index, parameter)
            is java.sql.Date -> ps.setDate(index, parameter)
            is java.sql.Timestamp -> ps.setTimestamp(index, parameter)
            null -> ps.setObject(index, null)
            else -> ps.setString(index, parameter.toString())
        }
    }
}