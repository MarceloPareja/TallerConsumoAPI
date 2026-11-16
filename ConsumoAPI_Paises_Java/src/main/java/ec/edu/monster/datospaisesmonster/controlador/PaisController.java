package ec.edu.monster.datospaisesmonster.controlador;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.datospaisesmonster.modelo.ConsumoAPI;
import ec.edu.monster.datospaisesmonster.modelo.Pais;
import ec.edu.monster.datospaisesmonster.modelo.PaisDAO;
import ec.edu.monster.datospaisesmonster.modelo.Region;
import ec.edu.monster.datospaisesmonster.modelo.ReporteEstadistico;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador MVC: coordina la API, el DAO y los datos que consume la vista.
 */
public class PaisController {

    private final PaisDAO dao = new PaisDAO();

    /**
     * Importa todos los países de la API v5 y los guarda en MySQL.
     *
     * @return cantidad de países guardados correctamente.
     */
    public int importarPaises() {
        String json = ConsumoAPI.obtenerDatos();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();

        int importados = 0;
        for (JsonElement elemento : array) {
            Pais pais = convertir(elemento.getAsJsonObject());
            if (pais != null && dao.guardar(pais)) {
                importados++;
            }
        }
        return importados;
    }

    /**
     * Actividad 1: consulta de países por nombre en la API.
     */
    public List<Pais> buscarPorNombreEnAPI(String nombre) {
        String json = ConsumoAPI.buscarPorNombre(nombre);
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();

        List<Pais> paises = new ArrayList<>();
        for (JsonElement elemento : array) {
            Pais pais = convertir(elemento.getAsJsonObject());
            if (pais != null) {
                paises.add(pais);
            }
        }
        return paises;
    }

    public List<Pais> listarPaises() {
        return dao.listar();
    }

    public List<Pais> buscarPorNombreEnBD(String nombre) {
        return dao.buscarPorNombre(nombre);
    }

    /**
     * Actividad 4: búsqueda local por región.
     */
    public List<Pais> buscarPorRegion(String region) {
        return dao.buscarPorRegion(region);
    }

    /**
     * Actividad 2: crear.
     */
    public boolean guardarPais(Pais pais) {
        return dao.guardar(pais);
    }

    /**
     * Actividad 2: actualizar.
     */
    public boolean actualizarPais(Pais pais) {
        return dao.actualizar(pais);
    }

    /**
     * Actividad 2: eliminar.
     */
    public boolean eliminarPais(int id) {
        return dao.eliminar(id);
    }

    /**
     * Actividad 5: genera los tres reportes solicitados.
     */
    public ReporteEstadistico generarReporte() {
        Map<String, Integer> porRegion = dao.contarPorRegion();
        Pais masPoblado = dao.paisMasPoblado();

        String regionTop = null;
        if (!porRegion.isEmpty()) {
            regionTop = porRegion.keySet().iterator().next();
        }

        return new ReporteEstadistico(porRegion, masPoblado, regionTop);
    }

    public boolean apiEnDemo() {
        return ConsumoAPI.esDemo();
    }

    /**
     * Cantidad de países guardados actualmente en MySQL.
     */
    public int contarPaises() {
        return dao.contarTotal();
    }

    /**
     * Total de países que reporta la API (para verificar si ya están todos
     * exportados en la base). Devuelve -1 si no se pudo verificar.
     */
    public int totalPaisesEnApi() {
        return ConsumoAPI.totalPaises();
    }

    /**
     * Borra todos los registros de la tabla pais con TRUNCATE.
     */
    public boolean borrarTodosLosPaises() {
        return dao.truncar();
    }

    private Pais convertir(JsonObject objeto) {
        if (objeto == null) {
            return null;
        }

        String nombre = "";
        if (objeto.has("names") && objeto.get("names").isJsonObject()) {
            JsonObject names = objeto.getAsJsonObject("names");
            if (names.has("common") && !names.get("common").isJsonNull()) {
                nombre = names.get("common").getAsString();
            }
        }
        if (nombre.isBlank()) {
            return null;
        }

        String capital = "";
        if (objeto.has("capitals") && objeto.get("capitals").isJsonArray()) {
            JsonArray capitales = objeto.getAsJsonArray("capitals");
            if (!capitales.isEmpty() && capitales.get(0).isJsonObject()) {
                JsonObject capitalObj = capitales.get(0).getAsJsonObject();
                if (capitalObj.has("name") && !capitalObj.get("name").isJsonNull()) {
                    capital = capitalObj.get("name").getAsString();
                }
            }
        }

        String region;
        if (objeto.has("region") && !objeto.get("region").isJsonNull()) {
            region = Region.desdeApi(objeto.get("region").getAsString());
        } else {
            region = Region.OTROS.getEtiqueta();
        }

        long poblacion = 0;
        if (objeto.has("population") && !objeto.get("population").isJsonNull()) {
            try {
                poblacion = objeto.get("population").getAsLong();
            } catch (RuntimeException ignorada) {
                poblacion = 0;
            }
        }

        String moneda = "";
        if (objeto.has("currencies") && objeto.get("currencies").isJsonArray()) {
            JsonArray monedas = objeto.getAsJsonArray("currencies");
            if (!monedas.isEmpty() && monedas.get(0).isJsonObject()) {
                JsonObject monedaObj = monedas.get(0).getAsJsonObject();
                if (monedaObj.has("code") && !monedaObj.get("code").isJsonNull()) {
                    moneda = monedaObj.get("code").getAsString();
                } else if (monedaObj.has("name") && !monedaObj.get("name").isJsonNull()) {
                    moneda = monedaObj.get("name").getAsString();
                }
            }
        }

        return new Pais(nombre, capital, region, poblacion, moneda);
    }
}
