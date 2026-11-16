package ec.edu.monster.datospaisesmonster.modelo;

/**
 * Traduce las regiones que devuelve REST Countries v5 a las etiquetas
 * usadas por la aplicación y por la base de datos.
 */
public enum Region {

    AMERICA("América", "Americas"),
    EUROPA("Europa", "Europe"),
    ASIA("Asia", "Asia"),
    AFRICA("África", "Africa"),
    OCEANIA("Oceanía", "Oceania"),
    ANTARTIDA("Antártida", "Antarctic"),
    OTROS("Otros", "");

    private final String etiqueta;
    private final String valorApi;

    Region(String etiqueta, String valorApi) {
        this.etiqueta = etiqueta;
        this.valorApi = valorApi;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getValorApi() {
        return valorApi;
    }

    public static String desdeApi(String valorApi) {
        if (valorApi == null) {
            return OTROS.etiqueta;
        }
        for (Region region : values()) {
            if (!region.valorApi.isEmpty() && region.valorApi.equalsIgnoreCase(valorApi.trim())) {
                return region.etiqueta;
            }
        }
        return OTROS.etiqueta;
    }

    /**
     * Etiquetas para el formulario de creación/edición.
     */
    public static String[] etiquetas() {
        return new String[]{
            AMERICA.etiqueta,
            EUROPA.etiqueta,
            ASIA.etiqueta,
            AFRICA.etiqueta,
            OCEANIA.etiqueta,
            ANTARTIDA.etiqueta,
            OTROS.etiqueta
        };
    }

    /**
     * Etiquetas para el filtro, incluyendo la opción "Todas".
     */
    public static String[] etiquetasFiltro() {
        return new String[]{
            "Todas",
            AMERICA.etiqueta,
            EUROPA.etiqueta,
            ASIA.etiqueta,
            AFRICA.etiqueta,
            OCEANIA.etiqueta,
            ANTARTIDA.etiqueta,
            OTROS.etiqueta
        };
    }
}
