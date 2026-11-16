package ec.edu.monster.datospaisesmonster.modelo;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Resultado de los reportes estadísticos de la actividad 5.
 */
public class ReporteEstadistico {

    private final Map<String, Integer> paisesPorRegion;
    private final Pais paisMasPoblado;
    private final String regionConMasPaises;

    public ReporteEstadistico(Map<String, Integer> paisesPorRegion,
            Pais paisMasPoblado,
            String regionConMasPaises) {
        this.paisesPorRegion = paisesPorRegion;
        this.paisMasPoblado = paisMasPoblado;
        this.regionConMasPaises = regionConMasPaises;
    }

    public Map<String, Integer> getPaisesPorRegion() {
        return paisesPorRegion;
    }

    public Pais getPaisMasPoblado() {
        return paisMasPoblado;
    }

    public String getRegionConMasPaises() {
        return regionConMasPaises;
    }

    public String toTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("REPORTE ESTADÍSTICO DE PAÍSES\n");
        sb.append("========================================\n\n");

        if (paisesPorRegion == null || paisesPorRegion.isEmpty()) {
            sb.append("No hay países registrados.\n");
            sb.append("Use el botón IMPORTAR TODOS o cree un país manualmente.\n");
            return sb.toString();
        }

        sb.append("Países por región:\n");
        int total = 0;
        for (Map.Entry<String, Integer> entrada : paisesPorRegion.entrySet()) {
            sb.append(String.format("  %-12s : %d%n", entrada.getKey(), entrada.getValue()));
            total += entrada.getValue();
        }
        sb.append("\nTotal de países: ").append(total).append("\n\n");

        if (paisMasPoblado != null) {
            NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-EC"));
            sb.append("País más poblado: ")
                    .append(paisMasPoblado.getNombre())
                    .append(" (")
                    .append(formato.format(paisMasPoblado.getPoblacion()))
                    .append(" habitantes)\n");
        } else {
            sb.append("País más poblado: sin datos\n");
        }

        sb.append("Región con más países: ");
        if (regionConMasPaises == null || regionConMasPaises.isBlank()) {
            sb.append("sin datos\n");
        } else {
            Integer cantidad = paisesPorRegion.get(regionConMasPaises);
            sb.append(regionConMasPaises);
            if (cantidad != null) {
                sb.append(" (").append(cantidad).append(" países)");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
