# Taller Práctico: Consumo de APIs Públicas en .NET con SQL Server (MVC)

Implementación de las secciones 1 a 18 del taller usando **ASP.NET Core MVC (.NET 10)**,
**Entity Framework Core 10** y **SQL Server 2022**.

> La API original `restcountries.com/v3.1` fue deprecada. El proyecto usa la versión
> vigente **v5** (`https://api.restcountries.com/countries/v5`) con autenticación
> `Authorization: Bearer <RESTCOUNTRIES_API_KEY>`.
> También se reemplazó `Newtonsoft.Json` por `System.Text.Json` (incluido en .NET).

## Estructura

```
Consumo_API_Paises_Dotnet/        # Raíz del repositorio
├── docker-compose.yml            # SQL Server + inicialización automática (db-init)
├── scripts/init-db.sh            # Automatización: crea ApiPaisesDB y aplica el esquema
├── .gitattributes                # Fuerza LF en los scripts .sh
└── Consumo_API_Paises_Dotnet/    # Proyecto ASP.NET Core MVC
    ├── Bdd/                      # Scripts SQL (base de datos y reportes)
    ├── Controllers/PaisController.cs # CRUD, búsquedas, filtros y reportes
    ├── Migrations/               # Migraciones de EF Core
    ├── Models/
    │   ├── Pais.cs
    │   ├── AppDbContext.cs
    │   └── ReportesViewModel.cs
    ├── Services/
    │   ├── IPaisService.cs
    │   └── PaisApiService.cs     # Consumo de REST Countries v5
    ├── Views/Pais/               # Index, Lista, Create, Edit, Delete, Details, BuscarPais, Reportes
    ├── .development.env          # RESTCOUNTRIES_API_KEY (no se versiona)
    ├── appsettings.json          # Cadena de conexión SqlServerConnection
    └── Program.cs                # DI: DbContext + HttpClient tipado
```

## Puesta en marcha

1. Levantar SQL Server con inicialización automática de la base de datos:

   ```bash
   docker compose up -d
   ```

   `docker compose` levanta el contenedor `sqlserver` y, en cuanto pasa su
   `healthcheck`, ejecuta el servicio `db-init` (`scripts/init-db.sh`), que:

   - espera a que SQL Server acepte conexiones,
   - crea la base de datos `ApiPaisesDB` si no existe,
   - aplica `Bdd/03_ApiPaisesDB_Migrations.sql`, creando la tabla `Paises` y el
     historial `__EFMigrationsHistory` con el mismo esquema que EF Core.

   Para verificar que la inicialización terminó correctamente:

   ```bash
   docker compose logs db-init
   ```

   La automatización es idempotente: si la base de datos ya existe en el volumen
   `sqlserver_data`, los scripts no vuelven a crear nada. Para reinicializar desde
   cero (borra los datos):

   ```bash
   docker compose down -v
   docker compose up -d
   ```

   > **Nota:** el script `scripts/init-db.sh` debe conservar saltos de línea LF
   > (el `.gitattributes` del repositorio lo garantiza) para poder ejecutarse dentro
   > del contenedor Linux.

2. *(Opcional)* Aplicar migraciones manualmente con EF Core, solo si se trabaja sin
   Docker o se agregaron migraciones nuevas:

   ```bash
   cd Consumo_API_Paises_Dotnet
   dotnet tool restore
   dotnet ef database update
   ```

   La alternativa manual de la guía original (`Bdd/01_CreateDatabase.sql`) sigue
   disponible, pero no debe combinarse con la automatización: ese script crea
   `Paises` con columnas `NULL` y entraría en conflicto con el esquema de EF Core.

3. Configurar la clave de la API en `.development.env`:

   ```
   RESTCOUNTRIES_API_KEY=rc_live_xxxxxxxxxxxxxxxx
   ```

4. Ejecutar la aplicación:

   ```bash
   dotnet run
   ```

5. Abrir `/Pais` e importar la información desde la API (254 países).

## Secciones cubiertas

| Sección | Implementación |
|---|---|
| 7 | `Bdd/01_CreateDatabase.sql` (ApiPaisesDB + Paises) |
| 9 | `SqlServerConnection` en `appsettings.json` |
| 10–11 | `Models/Pais.cs`, `Models/AppDbContext.cs` |
| 12 | `Program.cs` (EF Core, HttpClient tipado, ruta por defecto `Pais`) |
| 13 | `Services/PaisApiService.cs` (+ `IPaisService`) |
| 14–16 | `Controllers/PaisController.cs`, `Views/Pais/*.cshtml` |
| 17 | Migración `InitialCreate` + `Bdd/03_ApiPaisesDB_Migrations.sql` |
| 18 | Actividades 1–5: consulta por país, CRUD, búsquedas, filtros y reportes |

## Actividades de la sección 18

- **Actividad 1:** `/Pais/BuscarPais` consulta un país específico en la API.
- **Actividad 2:** CRUD completo (`Create`, `Lista`, `Edit`, `Delete`).
- **Actividad 3:** búsqueda por nombre, región y capital en `Lista`.
- **Actividad 4:** filtro dinámico por región y ordenamiento.
- **Actividad 5:** `/Pais/Reportes` (países por región, país más poblado, promedio por región)
  y `Bdd/02_Reportes.sql` con las consultas equivalentes.

## Nota sobre duplicados

Cada ejecución de **Importar** agrega nuevamente los 254 países (comportamiento del
documento). Si desea evitar duplicados, puede vaciar la tabla antes de importar:

```sql
USE ApiPaisesDB;
DELETE FROM Paises;
DBCC CHECKIDENT('Paises', RESEED, 0);
```
