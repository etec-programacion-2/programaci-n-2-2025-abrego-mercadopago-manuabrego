#!/bin/bash

# Script para inicializar la base de datos SQLite de la billetera virtual
# Autor: [Tu nombre]
# Fecha: $(date +%Y-%m-%d)

set -e  # Salir si hay algún error

DB_PATH="database/billetera.db"
SCHEMA_PATH="database/schema.sql"

echo "🔧 Inicializando base de datos SQLite para billetera virtual..."

# Crear directorio de base de datos si no existe
mkdir -p database

# Eliminar base de datos existente si existe (para reinicializar)
if [ -f "$DB_PATH" ]; then
    echo "📄 Eliminando base de datos existente..."
    rm "$DB_PATH"
fi

# Crear nueva base de datos y aplicar esquema
echo "🗄️ Creando nueva base de datos..."
sqlite3 "$DB_PATH" < "$SCHEMA_PATH"

echo "✅ Base de datos inicializada correctamente en: $DB_PATH"
echo "📊 Tablas creadas:"
sqlite3 "$DB_PATH" ".tables"

echo ""
echo "🎉 ¡Setup completado! La base de datos está lista para usar."