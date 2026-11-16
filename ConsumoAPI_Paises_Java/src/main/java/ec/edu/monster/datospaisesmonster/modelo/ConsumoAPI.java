package ec.edu.monster.datospaisesmonster.modelo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Consumo de la API REST Countries v5.
 *
 * La versión v3.1 usada originalmente en el documento fue retirada.
 * La v5 exige autenticación y pagina los resultados, por eso esta clase
 * recorre todas las páginas y devuelve un arreglo JSON uniforme.
 */
public class ConsumoAPI {

    private static final String API_BASE = "https://api.restcountries.com/countries/v5";
    private static final String CAMPOS = "names.common,capitals,region,population,currencies";
    private static final int TAMANO_PAGINA = 100;
    private static final int MAX_PAGINAS = 50;
    private static final int TIMEOUT_CONEXION = 15_000;
    private static final int TIMEOUT_LECTURA = 30_000;
    private static final String DEMO_KEY = "rc_live_demo";

    private ConsumoAPI() {
    }

    /**
     * Descarga todos los países recorriendo las páginas de la API.
     *
     * @return arreglo JSON con los objetos de todos los países.
     */
    public static String obtenerDatos() {
        JsonArray todos = new JsonArray();
        int offset = 0;

        for (int pagina = 0; pagina < MAX_PAGINAS; pagina++) {
            String url = API_BASE
                    + "?limit=" + TAMANO_PAGINA
                    + "&offset=" + offset
                    + "&response_fields=" + CAMPOS;

            JsonObject data = solicitarData(url);
            if (data == null) {
                break;
            }

            JsonArray objects = data.getAsJsonArray("objects");
            if (objects == null || objects.isEmpty()) {
                break;
            }

            for (JsonElement elemento : objects) {
                todos.add(elemento);
            }

            JsonObject meta = data.getAsJsonObject("meta");
            boolean hayMas = meta != null
                    && meta.has("more")
                    && meta.get("more").getAsBoolean();
            if (!hayMas) {
                break;
            }

            offset += TAMANO_PAGINA;
        }

        return todos.toString();
    }

    /**
     * Busca países por nombre en la API v5.
     *
     * @param nombre texto a buscar.
     * @return arreglo JSON con los países encontrados.
     */
    public static String buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "[]";
        }

        String consulta = URLEncoder.encode(nombre.trim(), StandardCharsets.UTF_8);
        String url = API_BASE + "/name?q=" + consulta
                + "&limit=100&response_fields=" + CAMPOS;

        JsonObject data = solicitarData(url);
        if (data == null) {
            return "[]";
        }

        JsonArray objects = data.getAsJsonArray("objects");
        return objects == null ? "[]" : objects.toString();
    }

    public static boolean esDemo() {
        return DEMO_KEY.equals(apiKey());
    }

    /**
     * Consulta cuántos países reporta la API en total (meta.total).
     * Se usa para verificar si la base ya contiene todos los países antes
     * de vaciar la tabla.
     *
     * @return total reportado por la API, o -1 si no se pudo verificar.
     */
    public static int totalPaises() {
        try {
            JsonObject data = solicitarData(API_BASE
                    + "?limit=1&response_fields=names.common");
            if (data == null) {
                return -1;
            }
            JsonObject meta = data.getAsJsonObject("meta");
            if (meta != null && meta.has("total") && !meta.get("total").isJsonNull()) {
                return meta.get("total").getAsInt();
            }
        } catch (RuntimeException e) {
            System.err.println("No se pudo verificar el total de la API: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Orden de búsqueda de la API key:
     * 1) propiedad del sistema -Drestcountries.api.key
     * 2) variable de entorno RESTCOUNTRIES_API_KEY
     * 3) src/main/resources/config.properties
     * 4) demo key
     */
    public static String apiKey() {
        String key = System.getProperty("restcountries.api.key");
        if (esVacio(key)) {
            key = System.getenv("RESTCOUNTRIES_API_KEY");
        }
        if (esVacio(key)) {
            key = leerClaveConfigurada();
        }
        if (esVacio(key)) {
            key = DEMO_KEY;
        }
        return key.trim();
    }

    private static String leerClaveConfigurada() {
        try (InputStream in = ConsumoAPI.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                return null;
            }
            Properties propiedades = new Properties();
            propiedades.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return propiedades.getProperty("restcountries.api.key");
        } catch (IOException e) {
            System.err.println("No se pudo leer config.properties: " + e.getMessage());
            return null;
        }
    }

    private static JsonObject solicitarData(String url) {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conexion.setRequestMethod("GET");
            conexion.setConnectTimeout(TIMEOUT_CONEXION);
            conexion.setReadTimeout(TIMEOUT_LECTURA);
            conexion.setRequestProperty("Accept", "application/json");
            conexion.setRequestProperty("Authorization", "Bearer " + apiKey());
            conexion.setRequestProperty("User-Agent", "TallerConsumoAPI/1.0");

            int codigo = conexion.getResponseCode();
            InputStream stream = (codigo >= 200 && codigo < 300)
                    ? conexion.getInputStream()
                    : conexion.getErrorStream();
            String cuerpo = leer(stream);

            if (codigo == 404) {
                return null;
            }
            if (codigo < 200 || codigo >= 300) {
                throw new IllegalStateException(mensajeError(codigo, cuerpo));
            }

            JsonObject raiz = JsonParser.parseString(cuerpo).getAsJsonObject();
            if (raiz.has("success") && !raiz.get("success").getAsBoolean()) {
                throw new IllegalStateException("La API respondió con error: " + cuerpo);
            }

            JsonObject data = raiz.getAsJsonObject("data");
            if (data == null) {
                throw new IllegalStateException("Respuesta inesperada de la API: " + cuerpo);
            }
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo consumir la API: " + e.getMessage(), e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("La API devolvió un JSON inválido: " + e.getMessage(), e);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static String mensajeError(int codigo, String cuerpo) {
        if (codigo == 401 || codigo == 403) {
            return "API key inválida o sin permisos (HTTP " + codigo + ")."
                    + " Configure RESTCOUNTRIES_API_KEY o restcountries.api.key.";
        }
        if (codigo == 429) {
            return "Se superó el límite de solicitudes de la API (HTTP 429). Intente más tarde.";
        }
        return "La API respondió HTTP " + codigo + ": " + cuerpo;
    }

    private static String leer(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
            return sb.toString();
        }
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
