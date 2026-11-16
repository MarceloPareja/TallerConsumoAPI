package ec.edu.monster.datospaisesmonster.controlador;

/**
 * Autenticación del taller.
 *
 * Las credenciales están quemadas en el código porque así se acordó para la
 * entrega. En un entorno real se usarían usuarios en base de datos con la
 * clave almacenada como hash.
 */
public class LoginController {

    public static final String USUARIO = "MONSTER";
    private static final String CLAVE = "MONSTER9";

    /**
     * Valida las credenciales del formulario de acceso.
     *
     * @param usuario usuario ingresado.
     * @param clave clave ingresada.
     * @return true si las credenciales son correctas.
     */
    public boolean autenticar(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            return false;
        }
        return USUARIO.equals(usuario.trim()) && CLAVE.equals(clave);
    }

    public String getUsuario() {
        return USUARIO;
    }
}
