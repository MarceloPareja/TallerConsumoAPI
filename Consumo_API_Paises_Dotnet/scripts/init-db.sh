#!/usr/bin/env bash
# ============================================================
# Inicializacion automatica de la base de datos ApiPaisesDB.
# Se ejecuta dentro del servicio "db-init" de docker-compose.
# ============================================================
set -euo pipefail

SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
SERVER="${MSSQL_SERVER:-sqlserver}"
USER="${MSSQL_USER:-sa}"
PASSWORD="${MSSQL_SA_PASSWORD:-Password123!}"
DATABASE="${MSSQL_DATABASE:-ApiPaisesDB}"
MAX_ATTEMPTS="${DB_INIT_MAX_ATTEMPTS:-60}"
SLEEP_SECONDS="${DB_INIT_SLEEP_SECONDS:-2}"
MIGRATIONS_SCRIPT="/bdd/03_ApiPaisesDB_Migrations.sql"

log() {
    echo "[db-init] $*"
}

log "Esperando a SQL Server en '${SERVER}'..."

attempt=1
until "$SQLCMD" -S "$SERVER" -U "$USER" -P "$PASSWORD" -C -l 5 -Q "SELECT 1" >/dev/null 2>&1; do
    if [ "$attempt" -ge "$MAX_ATTEMPTS" ]; then
        log "ERROR: SQL Server no respondio tras $((MAX_ATTEMPTS * SLEEP_SECONDS)) segundos."
        exit 1
    fi
    log "Intento ${attempt}/${MAX_ATTEMPTS}: servidor no disponible, reintentando en ${SLEEP_SECONDS}s..."
    attempt=$((attempt + 1))
    sleep "$SLEEP_SECONDS"
done

log "SQL Server disponible."

log "Creando la base de datos '${DATABASE}' si no existe..."
"$SQLCMD" -S "$SERVER" -U "$USER" -P "$PASSWORD" -C -b \
    -Q "IF DB_ID('${DATABASE}') IS NULL CREATE DATABASE [${DATABASE}];"

if [ ! -f "$MIGRATIONS_SCRIPT" ]; then
    log "ERROR: no se encontro el script de migraciones '${MIGRATIONS_SCRIPT}'."
    exit 1
fi

log "Aplicando el esquema y el historial de migraciones de EF Core..."
"$SQLCMD" -S "$SERVER" -U "$USER" -P "$PASSWORD" -C -b \
    -d "$DATABASE" -i "$MIGRATIONS_SCRIPT"

log "Base de datos '${DATABASE}' inicializada correctamente."
