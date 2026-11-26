# Taller de países en Java y .NET

Los dos proyectos se levantan de forma independiente. Requieren Docker Desktop con contenedores Linux y Docker Compose v2. Docker descarga Maven, JDK, .NET y los motores de base de datos; no necesitas instalarlos en Windows para usar los contenedores.

| Proyecto | Interfaz | Base de datos publicada en tu equipo |
| --- | --- | --- |
| Java Swing | http://localhost:6080/vnc.html?autoconnect=true&resize=remote | MySQL en localhost:3307 |
| ASP.NET Core MVC | http://localhost:8080 | SQL Server en localhost:1433 |

En cada carpeta de proyecto, si aún no existe `.env`, copia `.env.example` a `.env`, ajusta las contraseñas y ejecuta `docker compose up -d --build`. La creación del esquema y la carga inicial se realizan automáticamente. El primer arranque tarda mientras se descargan las imágenes y se consultan las APIs.

REST Countries requiere una clave propia para el listado completo. Los ejemplos usan `rc_live_demo`, que permite probar el sistema con una muestra real; no equivale a importar todos los países. Las aplicaciones siguen disponibles si la API falla y permiten reintentar desde su interfaz. Las bases quedan guardadas en volúmenes Docker.

El acceso académico en ambas aplicaciones es usuario **MONSTER** y clave **MONSTER9**. Java ofrece un escritorio compartido mediante noVNC; la aplicación sigue siendo Swing, no una conversión a una web multiusuario.

Consulta las instrucciones específicas:

- [Java y MySQL](ConsumoAPI_Paises_Java/README.md)
- [.NET y SQL Server](Consumo_API_Paises_Dotnet/README.md)

La guía .NET utiliza .NET 10 y EF Core 10, conservando la versión del proyecto existente. Incluye las actividades 1 a 5 y la actividad avanzada de universidades. Java conserva JDK 17, Swing, JDBC, Gson y DAO.
