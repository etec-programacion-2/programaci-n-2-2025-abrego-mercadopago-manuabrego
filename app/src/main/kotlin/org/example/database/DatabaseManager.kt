package org.example.database

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

/**
 * Clase para manejar la ejecución de queries en la base de datos
 * Utiliza el DatabaseConnection singleton y proporciona métodos seguros para ejecutar SQL
 */
class DatabaseManager {
    
    /**
     * Ejecuta una query SELECT y retorna el ResultSet
     * @param sql Query SQL a ejecutar
     * @param parameters Parámétroso pcionales para la query preparada
     * @return ResultSet con los resultados
     * @throws SQLException si hay error en la ejecución
     */
    fun executeQuery(sql: String, vararg parameters: Any): ResultSet {
        val connection = DatabaseConnection.getConnection()
        val preparedStatement = connection.prepareStatement(sql)
        
        try {
            // Setear parámetros si existen
            parameters.forEachIndexed { index, param ->
                setPreparedStatementParameter(preparedStatement, index + 1, param)
            }
            
            return preparedStatement.executeQuery()
        } catch (e: SQLException) {
            preparedStatement.close()
            throw SQLException("Error ejecutando query: $sql. ${e.message}", e)
        }
    }
    
    /**
     * Ejecuta una query de modificación (INSERT, UPDATE, DELETE)
     * @param sql Query SQL a ejecutar
     * @param parameters Parámetros opcionales para la query preparada
     * @return Número de filas afectadas
     * @throws SQLException si hay error en la ejecución
     */
    fun executeUpdate(sql: String, vararg parameters: Any): Int {
        val connection = DatabaseConnection.getConnection()
        
        return connection.prepareStatement(sql).use { preparedStatement ->
            try {
                // Setear parámetros si existen
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
     * @param sql Query INSERT a ejecutar
     * @param parameters Parámetros opcionales para la query preparada
     * @return ID generado por la base de datos
     * @throws SQLException si hay error en la ejecución
     */
    fun executeInsert(sql: String, vararg parameters: Any): Long {
        val connection = DatabaseConnection.getConnection()
        
        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { preparedStatement ->
            try {
                // Setear parámetros si existen
                parameters.forEachIndexed { index, param ->
                    setPreparedStatementParameter(preparedStatement, index + 1, param)
                }
                
                val affectedRows = preparedStatement.executeUpdate()
                
                if (affectedRows == 0) {
                    throw SQLException("No se pudo insertar el registro")
                }
                
                val generatedKeys = preparedStatement.generatedKeys
                return if (generatedKeys.next()) {
                    val id = generatedKeys.getLong(1)
                    println("➕ Registro insertado con ID: $id")
                    id
                } else {
                    throw SQLException("No se pudo obtener el ID generado")
                }
            } catch (e: SQLException) {
                throw SQLException("Error ejecutando insert: $sql. ${e.message}", e)
            }
        }
    }
    
    /**
     * Ejecuta múltiples queries en una transacción
     * @param queries Lista de pares (SQL, parámetros)
     * @return true si todas las queries se ejecutaron correctamente
     * @throws SQLException si alguna query falla (hace rollback automático)
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
     * @param tableName Nombre de la tabla
     * @param whereClause Cláusula WHERE opcional (sin incluir 'WHERE')
     * @param parameters Parámetros para la cláusula WHERE
     * @return Número de registros
     */
    fun countRecords(tableName: String, whereClause: String? = null, vararg parameters: Any): Int {
        val sql = if (whereClause != null) {
            "SELECT COUNT(*) FROM $tableName WHERE $whereClause"
        } else {
            "SELECT COUNT(*) FROM $tableName"
        }
        
        executeQuery(sql, *parameters).use { resultSet ->
            return if (resultSet.next()) {
                resultSet.getInt(1)
            } else {
                0
            }
        }
    }
    
    /**
     * Verifica si existe al menos un registro que cumpla la condición
     * @param tableName Nombre de la tabla
     * @param whereClause Cláusula WHERE (sin incluir 'WHERE')
     * @param parameters Parámetros para la cláusula WHERE
     * @return true si existe al menos un registro
     */
    fun exists(tableName: String, whereClause: String, vararg parameters: Any): Boolean {
        return countRecords(tableName, whereClause, *parameters) > 0
    }
    
    /**
     * Método privado para setear parámetros en PreparedStatement
     */
    private fun setPreparedStatementParameter(ps: PreparedStatement, index: Int, parameter: Any) {
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