package org.example.util

/**
 * Utilidad simple para logging con información de ubicación
 */
object Logger {
    private var debugMode = true
    
    /**
     * Log de información
     */
    fun info(mensaje: String) {
        val stackTrace = Thread.currentThread().stackTrace[2]
        println("[INFO] ${getLocation(stackTrace)} - $mensaje")
    }
    
    /**
     * Log de debug (solo si debugMode está activado)
     */
    fun debug(mensaje: String) {
        if (!debugMode) return
        val stackTrace = Thread.currentThread().stackTrace[2]
        println("[DEBUG] ${getLocation(stackTrace)} - $mensaje")
    }
    
    /**
     * Log de error
     */
    fun error(mensaje: String, excepcion: Throwable? = null) {
        val stackTrace = Thread.currentThread().stackTrace[2]
        println("[ERROR] ${getLocation(stackTrace)} - $mensaje")
        excepcion?.printStackTrace()
    }
    
    /**
     * Log de warning
     */
    fun warn(mensaje: String) {
        val stackTrace = Thread.currentThread().stackTrace[2]
        println("[WARN] ${getLocation(stackTrace)} - $mensaje")
    }
    
    /**
     * Obtiene la ubicación (archivo:línea) del stackTrace
     */
    private fun getLocation(stackTrace: StackTraceElement): String {
        return "${stackTrace.fileName}:${stackTrace.lineNumber} (${stackTrace.methodName})"
    }
    
    /**
     * Activa/desactiva el modo debug
     */
    fun setDebugMode(enabled: Boolean) {
        debugMode = enabled
    }
}