package ec.edu.monster.datospaisesmonster.modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión JDBC a MySQL. Los valores por defecto coinciden con el
 * docker-compose del taller y se pueden cambiar con variables de entorno.
 */
public class Conexion {

    private static final String URL = obtener("DB_URL",
            "jdbc:mysql://localhost:3306/api_paises"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC"
            + "&characterEncoding=UTF-8");

    private static final String USER = obtener("DB_USER", "root");
    private static final String PASS = obtener("DB_PASSWORD", "12345");

    private Conexion() {
    }

    private static String obtener(String variable, String defecto) {
        String valor = System.getenv(variable);
        return (valor == null || valor.isBlank()) ? defecto : valor.trim();
    }

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a MySQL: " + e.getMessage());
            System.err.println("Verifique que Docker esté corriendo y que el puerto 3306 esté libre.");
            return null;
        }
    }
}
