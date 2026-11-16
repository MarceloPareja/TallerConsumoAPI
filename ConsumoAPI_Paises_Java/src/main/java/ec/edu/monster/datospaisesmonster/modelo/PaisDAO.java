package ec.edu.monster.datospaisesmonster.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de la tabla pais. Centraliza todo el acceso a MySQL.
 */
public class PaisDAO {

    public boolean guardar(Pais pais) {
        String sql = "INSERT INTO pais(nombre,capital,region,poblacion,moneda) VALUES(?,?,?,?,?)";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pais.getNombre());
            ps.setString(2, pais.getCapital());
            ps.setString(3, pais.getRegion());
            ps.setLong(4, pais.getPoblacion());
            ps.setString(5, pais.getMoneda());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar país: " + e.getMessage());
            return false;
        }
    }

    public List<Pais> listar() {
        String sql = "SELECT id,nombre,capital,region,poblacion,moneda FROM pais ORDER BY id";
        List<Pais> paises = new ArrayList<>();

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                paises.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar países: " + e.getMessage());
        }

        return paises;
    }

    public List<Pais> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return listar();
        }

        String sql = "SELECT id,nombre,capital,region,poblacion,moneda FROM pais "
                + "WHERE nombre LIKE ? ORDER BY nombre";
        List<Pais> paises = new ArrayList<>();

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paises.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por nombre: " + e.getMessage());
        }

        return paises;
    }

    public List<Pais> buscarPorRegion(String region) {
        if (region == null || region.isBlank() || "Todas".equalsIgnoreCase(region)) {
            return listar();
        }

        String sql = "SELECT id,nombre,capital,region,poblacion,moneda FROM pais "
                + "WHERE region = ? ORDER BY nombre";
        List<Pais> paises = new ArrayList<>();

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paises.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por región: " + e.getMessage());
        }

        return paises;
    }

    public Pais buscarPorId(int id) {
        String sql = "SELECT id,nombre,capital,region,poblacion,moneda FROM pais WHERE id = ?";

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por id: " + e.getMessage());
        }

        return null;
    }

    public boolean actualizar(Pais pais) {
        String sql = "UPDATE pais SET nombre=?, capital=?, region=?, poblacion=?, moneda=? WHERE id=?";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pais.getNombre());
            ps.setString(2, pais.getCapital());
            ps.setString(3, pais.getRegion());
            ps.setLong(4, pais.getPoblacion());
            ps.setString(5, pais.getMoneda());
            ps.setInt(6, pais.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar país: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM pais WHERE id = ?";
        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar país: " + e.getMessage());
            return false;
        }
    }

    /**
     * Devuelve las regiones ordenadas de mayor a menor cantidad de países.
     */
    public Map<String, Integer> contarPorRegion() {
        Map<String, Integer> resultado = new LinkedHashMap<>();
        String sql = "SELECT region, COUNT(*) AS total FROM pais "
                + "GROUP BY region ORDER BY total DESC, region ASC";

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String region = rs.getString("region");
                if (region == null || region.isBlank()) {
                    region = "Sin región";
                }
                resultado.put(region, rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error al contar por región: " + e.getMessage());
        }

        return resultado;
    }

    public Pais paisMasPoblado() {
        String sql = "SELECT id,nombre,capital,region,poblacion,moneda FROM pais "
                + "ORDER BY poblacion DESC, nombre ASC LIMIT 1";

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el país más poblado: " + e.getMessage());
        }

        return null;
    }

    /**
     * Cantidad total de países guardados en la tabla.
     *
     * @return número de filas, o -1 si no se pudo consultar.
     */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) AS total FROM pais";

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al contar los países: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Vacía la tabla pais con TRUNCATE (reinicia también el AUTO_INCREMENT).
     *
     * @return true si la operación se ejecutó correctamente.
     */
    public boolean truncar() {
        String sql = "TRUNCATE TABLE pais";

        try (Connection con = abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al borrar los datos: " + e.getMessage());
            return false;
        }
    }

    private Pais mapear(ResultSet rs) throws SQLException {
        Pais pais = new Pais();
        pais.setId(rs.getInt("id"));
        pais.setNombre(rs.getString("nombre"));
        pais.setCapital(rs.getString("capital"));
        pais.setRegion(rs.getString("region"));
        pais.setPoblacion(rs.getLong("poblacion"));
        pais.setMoneda(rs.getString("moneda"));
        return pais;
    }

    private Connection abrir() throws SQLException {
        Connection con = Conexion.conectar();
        if (con == null) {
            throw new SQLException("No hay conexión con MySQL");
        }
        return con;
    }
}
