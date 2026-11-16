# Taller Consumo de APIs Públicas - Países (Java + MySQL + MVC)

Proyecto Maven/NetBeans que consume **REST Countries v5**, guarda los países en **MySQL** y aplica el patrón **MVC** con un **DAO**.

> El documento original usaba `https://restcountries.com/v3.1/all`. Ese endpoint fue retirado en 2026.
> La aplicación está adaptada a la API actual `https://api.restcountries.com/countries/v5`, que requiere
> autenticación y pagina los resultados.

## Ruta del proyecto

```text
C:\Users\gmlop\Desktop\ESPE\Octavo\Arquitectura\TallerConsumoAPI\TallerConsumoAPI\ConsumoAPI_Paises_Java
```

## Funcionalidades implementadas

- Importación de todos los países desde la API v5 (paginación automática).
- Búsqueda de países por nombre en la API (actividad 1).
- CRUD completo sobre la tabla `pais`: crear, leer, actualizar y eliminar (actividad 2).
- Ingreso y edición en **subpantallas** (diálogos); la vista principal solo lista y selecciona.
- `Actualizar` y `Eliminar` permanecen deshabilitados hasta seleccionar una fila guardada.
- Botón **Borrar datos** que vacía la tabla con `TRUNCATE`, verificando antes cuántos países hay
  en la base y cuántos reporta la API.
- Tabla `JTable` con los países (actividad 3).
- Búsqueda/filtro por región: América, Europa, Asia, África, Oceanía y Antártida (actividad 4).
- Reportes estadísticos (actividad 5):
  - Cantidad de países por región.
  - País más poblado.
  - Región con más países.
- Arquitectura MVC:
  - `modelo`: entidad, conexión, consumo de API, DAO y reporte.
  - `controlador`: coordina API, DAO y vista.
  - `vista`: formulario Swing con tabla, búsqueda y CRUD.

## Estructura

```text
ConsumoAPI_Paises_Java/
├── pom.xml
├── docker-compose.yml
├── README.md
├── .gitignore
├── bd/
│   └── 01_esquema.sql
└── src/main/
    ├── java/ec/edu/monster/datospaisesmonster/
    │   ├── DatosPaisesMonster.java
    │   ├── modelo/
    │   │   ├── Pais.java
    │   │   ├── Region.java
    │   │   ├── Conexion.java
    │   │   ├── ConsumoAPI.java
    │   │   ├── PaisDAO.java
    │   │   └── ReporteEstadistico.java
    │   ├── controlador/
    │   │   ├── LoginController.java
    │   │   └── PaisController.java
    │   └── vista/
    │       ├── DlgLogin.java
    │       ├── DlgPais.java
    │       ├── FrmPais.java
    │       └── Tema.java
    └── resources/
        ├── config.properties          # API key local (ignorada por Git)
        └── config.properties.example
```

## Requisitos

- JDK 17 o superior.
- NetBeans 21 o superior (opcional, el proyecto también compila con Maven puro).
- Maven 3.9+.
- Docker Desktop con Docker Compose.
- MySQL no es necesario instalarlo en Windows: se usa el contenedor.

## API key

La API key se busca en este orden:

1. Propiedad del sistema: `-Drestcountries.api.key=SU_KEY`
2. Variable de entorno: `RESTCOUNTRIES_API_KEY`
3. Archivo `src/main/resources/config.properties`
4. Demo key `rc_live_demo` (solo devuelve un país de muestra)

El proyecto ya incluye una key local en `src/main/resources/config.properties`.
Ese archivo está en `.gitignore` para no publicar la clave. Si compartes el proyecto,
copia `config.properties.example` como `config.properties` y coloca tu propia key.

También puedes ejecutar sin tocar archivos:

```powershell
$env:RESTCOUNTRIES_API_KEY="TU_API_KEY"
mvn exec:java
```

o:

```powershell
mvn exec:java "-Drestcountries.api.key=TU_API_KEY"
```

## Acceso al sistema (login)

Al ejecutar la aplicación se muestra primero una subpantalla de acceso. La ventana
principal no se abre hasta autenticarse.

| Campo | Valor |
|---|---|
| Usuario | `MONSTER` |
| Clave | `MONSTER9` |

Comportamiento:

- El usuario distingue mayúsculas/minúsculas y la clave también.
- Si los datos son incorrectos muestra `Usuario o clave incorrectos.` y permite reintentar.
- `Enter` intenta ingresar; `Esc` o **Cancelar** cierran la aplicación.
- Al autenticarse, la barra de estado de la ventana principal muestra `Sesión: MONSTER`.

> Las credenciales están quemadas en `LoginController` porque así se acordó para el taller.
> En un entorno real se usarían usuarios en base de datos con la clave guardada como hash.

## Base de datos

El `docker-compose.yml` crea:

- MySQL 8.0
- Base: `api_paises`
- Usuario: `root`
- Contraseña: `12345`
- Puerto: `3306`
- Tabla: `pais(id, nombre, capital, region, poblacion, moneda)`
- Tabla vacía inicialmente, tal como se acordó.

### Iniciar MySQL

```powershell
cd C:\Users\gmlop\Desktop\ESPE\Octavo\Arquitectura\TallerConsumoAPI\TallerConsumoAPI\ConsumoAPI_Paises_Java
docker compose up -d
docker compose ps
```

Espera a que el contenedor aparezca como `healthy`.

### Verificar la base

```powershell
docker compose exec mysql mysql -uroot -p12345 -e "USE api_paises; SHOW TABLES; DESCRIBE pais; SELECT COUNT(*) AS total FROM pais;"
```

### Reiniciar la base desde cero

```powershell
docker compose down -v
docker compose up -d
```

El parámetro `-v` elimina el volumen y vuelve a ejecutar `bd/01_esquema.sql`.

## Compilar y ejecutar

### NetBeans

1. Abrir NetBeans.
2. `File > Open Project`.
3. Seleccionar la carpeta `ConsumoAPI_Paises_Java` (la que contiene `pom.xml`).
4. NetBeans la reconoce como proyecto Maven.
5. Ejecutar con `Run Project` (F6).

La clase principal es:

```text
ec.edu.monster.datospaisesmonster.DatosPaisesMonster
```

### Maven

```powershell
mvn clean package
mvn exec:java
```

`mvn clean package` genera dos JAR:

```text
target/ConsumoAPI_Paises_Java-1.0-SNAPSHOT.jar
target/ConsumoAPI_Paises_Java-1.0-SNAPSHOT-todo.jar
```

El segundo incluye Gson y MySQL Connector, por lo que se puede ejecutar directamente:

```powershell
java -jar target\ConsumoAPI_Paises_Java-1.0-SNAPSHOT-todo.jar
```

## Cómo usar la aplicación

### Iniciar sesión

1. Al arrancar aparece la subpantalla **Acceso al sistema**.
2. Ingresar usuario `MONSTER` y clave `MONSTER9`.
3. Presionar **Ingresar** (o `Enter`).
4. Se abre la ventana principal y la barra inferior muestra `Sesión: MONSTER`.

### Importar todos los países

1. Presionar **Importar todos**.
2. La aplicación recorre las páginas de la API v5 y guarda los países en MySQL.
3. Al finalizar muestra cuántos países se guardaron.
4. La tabla se actualiza automáticamente.

Con una key real se importan los **254 países** de la API.
Con la demo key solo se importa **1 país de muestra**.

### Buscar por nombre (actividad 1)

1. Escribir un nombre en **Buscar por nombre (API)**.
2. Presionar **Buscar**.
3. Los resultados se muestran en la tabla y todavía no están guardados.
4. Para guardar uno, seleccionarlo, revisar el formulario y presionar **Guardar**.

Ejemplo: `Ecuador` devuelve Quito, América, población y USD.

### Crear un país (CRUD)

1. Presionar **Nuevo ingreso de país**.
2. Se abre una subpantalla; llenar Nombre, Capital, Región, Población y Moneda.
3. Presionar **Registrar país**.
4. El país se inserta en MySQL y aparece en la tabla.

### Actualizar un país

1. Seleccionar una fila en la tabla (el botón **Actualizar** se habilita al seleccionar).
2. Presionar **Actualizar** (o hacer doble clic sobre la fila).
3. Modificar los datos en la subpantalla y presionar **Guardar cambios**.

### Eliminar un país

1. Seleccionar una fila guardada (el botón **Eliminar** se habilita al seleccionar).
2. Presionar **Eliminar**.
3. Confirmar la eliminación.

### Borrar todos los datos (TRUNCATE)

1. Presionar **Borrar datos**.
2. La aplicación consulta cuántos países hay en la base y cuántos reporta la API.
3. Se muestra un resumen, por ejemplo:

   ```text
   Países guardados en la base de datos: 254
   Países reportados por la API: 254

   Verificación: la base ya contiene todos los países de la API.
   ```

   Si la base tiene menos registros, avisa cuántos países faltan por importar.
4. Confirmar con **Sí, borrar**. Se ejecuta `TRUNCATE TABLE pais`, la tabla queda vacía
   y el `AUTO_INCREMENT` vuelve a 1.

### Limpiar la vista

El botón **Limpiar** de la cabecera borra el texto de búsqueda, restablece el filtro de
región, quita la selección de la tabla y vuelve a listar los datos desde MySQL.

### Filtrar por región (actividad 4)

1. Elegir una región en el combo.
2. Presionar **Filtrar**.
3. La tabla muestra solo los países de esa región.

### Reportes (actividad 5)

Presionar **Reportes** para ver:

```text
Países por región:
  África       : 60
  América      : 56
  Europa       : 54
  Asia         : 52
  Oceanía      : 27
  Antártida    : 5

Total de países: 254

País más poblado: India (1.429.404.000 habitantes)
Región con más países: África (60 países)
```

Los valores dependen de los datos importados.

## Consultas SQL de verificación

```powershell
docker compose exec mysql mysql -uroot -p12345 -e "USE api_paises; SELECT COUNT(*) AS total FROM pais;"
```

```powershell
docker compose exec mysql mysql -uroot -p12345 -e "USE api_paises; SELECT * FROM pais ORDER BY id DESC LIMIT 10;"
```

```powershell
docker compose exec mysql mysql -uroot -p12345 -e "USE api_paises; SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC;"
```

## Cambios realizados respecto al documento original

| Documento original | Implementación actual |
|---|---|
| `https://restcountries.com/v3.1/all` | `https://api.restcountries.com/countries/v5` |
| Sin autenticación | Header `Authorization: Bearer <API_KEY>` |
| Una sola respuesta con todos los países | Paginación de 100 en 100 con `limit` y `offset` |
| `name.common` | `names.common` |
| `capital` (texto) | `capitals[0].name` |
| `currencies` (objeto) | `currencies[0].code` |
| `new URL(...)` | `URI.create(...).toURL()` por compatibilidad con JDK 20+ |
| Lectura sin charset | Lectura UTF-8 explícita |
| `Connection` sin cerrar | `try-with-resources` en el DAO |
| Capital y moneda vacías en el controlador | Se extraen de la API |
| Solo botón de importación | Tabla, CRUD, búsqueda, filtros y reportes |
| Java 25 en el `pom.xml` | `maven.compiler.release=17` para NetBeans 21+ y JDK 17+ |

## Notas y solución de problemas

### El puerto 3306 está ocupado

Si tienes otro MySQL local o el `docker-compose.yml` del repositorio padre usando el puerto 3306,
detén ese servicio o cambia el mapeo del puerto en `docker-compose.yml`, por ejemplo:

```yaml
ports:
  - "3307:3306"
```

Luego ajusta la conexión:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3307/api_paises?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"
```

### La API responde 401 o 403

La key no es válida o no tiene permisos. Configura una key correcta en
`src/main/resources/config.properties`, en `RESTCOUNTRIES_API_KEY` o con
`-Drestcountries.api.key=...`.

### La API responde 429

Se superó el límite de solicitudes. Espera unos minutos y vuelve a intentar.

### Se duplican los países al importar dos veces

El esquema del documento no tiene una restricción `UNIQUE` sobre `nombre`.
Si presionas **Importar todos** varias veces, se insertan filas repetidas.
Para limpiar sin salir de la aplicación usa **Borrar datos** (TRUNCATE) y vuelve a importar.

También puedes hacerlo desde consola:

```powershell
docker compose exec mysql mysql -uroot -p12345 -e "USE api_paises; TRUNCATE TABLE pais;"
```

### No aparece la ventana de la aplicación

Verifica que estés ejecutando `DatosPaisesMonster` y que el JDK tenga soporte gráfico.
En NetBeans, revisa `Run > Set Project Configuration > Customize > Run > Main Class`.

### La vista no está en el diseñador visual de NetBeans

`FrmPais` se construye por código con layouts de Swing para que sea robusta y no dependa
de un archivo `.form` con muchos componentes. NetBeans compila y ejecuta el proyecto
normalmente; si se necesita editar la ventana en el diseñador visual, se puede generar
un `.form` aparte.

## Seguridad

- `src/main/resources/config.properties` está ignorado por Git.
- No publiques la API key en repositorios públicos.
- Si la key se filtra, revócala en el panel de REST Countries y genera otra.
