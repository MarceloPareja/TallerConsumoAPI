# Países y universidades con ASP.NET Core MVC y SQL Server

Aplicación .NET 10 y Entity Framework Core 10. Incluye las actividades principales del taller: consumo de REST Countries v5, persistencia, consulta por país, CRUD, búsquedas por nombre/región/capital, filtros dinámicos y reportes. Incluye la actividad avanzada de universidades con nombre, dominios, sitios web y país almacenados en SQL Server.

## Arranque completo con Docker

Desde esta carpeta (la que contiene `docker-compose.yml`):

```powershell
if (!(Test-Path .env)) { Copy-Item .env.example .env }
# Edita .env: cambia MSSQL_SA_PASSWORD y RESTCOUNTRIES_API_KEY.
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

Abre http://localhost:8080 e inicia sesión con **MONSTER / MONSTER9**.

Compose levanta SQL Server 2022, espera a su healthcheck y arranca la aplicación. La aplicación crea `ApiPaisesDB` y aplica las migraciones de EF Core antes de atender solicitudes. Se crean `Paises`, `Universidades` y `__EFMigrationsHistory`. Un servicio en segundo plano importa países y universidades de Ecuador cuando las tablas están vacías. Revisa los logs para saber cuándo termina esa carga; la web puede estar disponible antes de que terminen las consultas remotas.

`rc_live_demo` permite probar con una muestra real. Para obtener todos los países configura una clave propia de https://restcountries.com. Si ya usaste la demo, actualiza `.env`, ejecuta `docker compose up -d --force-recreate app` y pulsa **Importar todos**. No se bloquea una importación por tener registros manuales: se agregan los faltantes por nombre sin sobrescribir los existentes ni duplicarlos.

Las consultas externas tienen timeout. Si una API falla, la web y los datos locales continúan disponibles; consulta el aviso en los logs y reintenta desde la interfaz. Una descarga o escritura fallida no guarda una carga parcial.

## Universidades

En la pestaña **Universidades** escribe el país y pulsa **Importar**. Utiliza nombres en inglés como Ecuador, Spain o United States, tal como espera la API de HipoLabs. El listado permite buscar por país y nombre y abrir los sitios web. Se guardan todos los dominios y páginas recibidos. Las importaciones repetidas conservan las universidades existentes y agregan las faltantes usando país y nombre como clave.

Fuente: http://universities.hipolabs.com/search?country=Ecuador y https://github.com/Hipo/university-domains-list.

## Configuración

| Variable | Uso |
| --- | --- |
| `WEB_PORT` | Puerto HTTP local; 8080 por defecto |
| `SQLSERVER_PORT` | Puerto de SQL Server en Windows; 1433 por defecto |
| `MSSQL_SA_PASSWORD` | Contraseña de SQL Server, con mayúsculas, minúsculas, números y símbolos |
| `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexión al ejecutar sin Docker; DB_PASSWORD vacío utiliza MSSQL_SA_PASSWORD |
| `RESTCOUNTRIES_API_KEY` | Clave propia o demo de muestra |
| `RESTCOUNTRIES_BASE_URL` | URL base de países, con `/` final |
| `UNIVERSIDADES_BASE_URL` | URL base de HipoLabs, con `/` final |
| `UNIVERSIDADES_PAIS` | País de la carga inicial; Ecuador por defecto |
| `SEED_ON_START` | `true` para cargar automáticamente tablas vacías |
| `APPLY_MIGRATIONS` | `true` para actualizar el esquema al iniciar |

Dentro de Compose, la conexión se configura automáticamente contra `sqlserver:1433` y usa `MSSQL_SA_PASSWORD`; no tienes que escribir una cadena de conexión. Para ejecución local se carga el `.env` más cercano. Las variables del proceso prevalecen; `.development.env` se mantiene solo como compatibilidad con versiones anteriores. Una cadena `ConnectionStrings__SqlServerConnection` explícita también tiene prioridad sobre DB_*.

Las credenciales de acceso MONSTER son académicas y se mantienen como estaban en el proyecto. El contenedor publica HTTP solo en localhost; para exponerlo fuera del equipo configura HTTPS en un proxy inverso y sustituye la autenticación de demostración. Las claves de cookies persisten en un volumen.

## Ejecución local con Visual Studio o CLI

Necesitas SDK .NET 10. El proyecto original ya utilizaba .NET 10; la guía académica menciona .NET 8. `System.Text.Json` reemplaza a Newtonsoft.Json.

```powershell
# Desde esta carpeta, con .env configurado:
docker compose up -d sqlserver
dotnet restore Consumo_API_Paises_Dotnet/Consumo_API_Paises_Dotnet.csproj
dotnet run --project Consumo_API_Paises_Dotnet --launch-profile http
```

Abre http://localhost:5173. Las migraciones se aplican también al ejecutar localmente. Si cambias `SQLSERVER_PORT`, ajusta `DB_HOST=127.0.0.1,PUERTO` para ese modo.

Para administrar migraciones manualmente:

```powershell
dotnet tool restore
dotnet ef database update --project Consumo_API_Paises_Dotnet
dotnet ef migrations script --idempotent --project Consumo_API_Paises_Dotnet
```

Los scripts `Bdd/01_CreateDatabase.sql` y `Bdd/03_ApiPaisesDB_Migrations.sql` pertenecen al arranque anterior. Compose utiliza ahora EF Core como fuente del esquema; no ejecutes el script 01 sobre una base administrada con migraciones. El script `scripts/init-db.sh` se conserva para instalaciones manuales y aplica los scripts 03 y 04.

## Uso y reportes

- **Países:** listado, filtros, CRUD y ordenamiento. Importar agrega faltantes; Actualizar sincroniza únicamente los países existentes con la API.
- **Consultar API:** busca un país sin guardarlo automáticamente.
- **Reportes:** cantidad de países por región, país más poblado y promedio poblacional por región.
- **Universidades:** importación por país y consultas sobre los datos persistidos.
- `/health`: devuelve 200 cuando se puede conectar con SQL Server; no requiere iniciar sesión.

## Operación y persistencia

```powershell
docker compose logs --tail=100 app sqlserver
docker compose stop
docker compose start
docker compose down
```

`down` conserva los volúmenes de SQL Server y de cookies. Al volver a levantar se aplican solo las migraciones pendientes y se conservan los registros. Cambiar la contraseña en `.env` no modifica automáticamente un SQL Server ya inicializado: actualiza su usuario o utiliza un volumen nuevo con una copia de tus datos.

Para reiniciar intencionalmente desde cero:

```powershell
# DESTRUCTIVO: elimina la base y las claves de cookies de esta instalación.
docker compose down -v
docker compose up -d --build
```

Si el puerto 8080 o 1433 está ocupado, cámbialo en `.env`. Ante errores de SQL revisa `docker compose logs sqlserver`. Ante 401/403 de países revisa la clave; ante 429 espera y reintenta. Los volúmenes nuevos de este Compose tienen nombres distintos de los del Compose antiguo; los datos antiguos no se borran ni se trasladan automáticamente.

## Verificación

Las pruebas de integración y sus instrucciones están en [tests/README.md](../tests/README.md). Incluyen CRUD, importaciones repetidas, fallos de APIs y migraciones de una base nueva.
