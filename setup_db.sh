#!/bin/bash

# Script para inicializar la base de datos SQLite de la billetera virtual
# Autor: Manuel Abrego
# Fecha: $(date +%Y-%m-%d)

set -e  # Salir si hay algún error

DB_PATH="app/database/billetera.db"
SCHEMA_PATH="app/database/schema.sql"

echo "🔧 Inicializando base de datos SQLite para billetera virtual..."

# Crear directorio de base de datos si no existe
mkdir -p app/database

# Verificar que el archivo schema.sql existe
if [ ! -f "$SCHEMA_PATH" ]; then
    echo "❌ Error: No se encuentra el archivo schema.sql en: $SCHEMA_PATH"
    echo "💡 Asegúrate de que el archivo existe en app/database/schema.sql"
    exit 1
fi

# Eliminar base de datos existente si existe (para reinicializar)
if [ -f "$DB_PATH" ]; then
    echo "📄 Eliminando base de datos existente..."
    rm "$DB_PATH"
fi

# Crear nueva base de datos y aplicar esquema
echo "🗄️ Creando nueva base de datos..."
sqlite3 "$DB_PATH" < "$SCHEMA_PATH"

# Verificar que la base de datos se creó correctamente
if [ -f "$DB_PATH" ]; then
    echo "✅ Base de datos inicializada correctamente en: $DB_PATH"
    echo "📊 Tablas creadas:"
    sqlite3 "$DB_PATH" ".tables"
    
    echo ""
    echo "📈 Estadísticas iniciales:"
    echo "   👥 Usuarios: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM users;")"
    echo "   💰 Cuentas: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM accounts;")"
    echo "   💸 Transacciones: $(sqlite3 "$DB_PATH" "SELECT COUNT(*) FROM transactions;")"
    
    echo ""
    echo "🎉 ¡Setup completado! La base de datos está lista para usar."
else
    echo "❌ Error: No se pudo crear la base de datos"
    exit 1
fi