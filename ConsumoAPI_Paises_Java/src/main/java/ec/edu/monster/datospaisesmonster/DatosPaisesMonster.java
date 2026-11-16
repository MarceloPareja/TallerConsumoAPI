package ec.edu.monster.datospaisesmonster;

import ec.edu.monster.datospaisesmonster.controlador.LoginController;
import ec.edu.monster.datospaisesmonster.vista.DlgLogin;
import ec.edu.monster.datospaisesmonster.vista.FrmPais;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del taller.
 *
 * Primero se muestra la subpantalla de acceso; la ventana principal solo se
 * abre cuando las credenciales son correctas.
 */
public class DatosPaisesMonster {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si el look and feel del sistema no está disponible, se usa el de Java.
        }

        SwingUtilities.invokeLater(() -> {
            if (!DlgLogin.mostrar(null)) {
                System.exit(0);
            }
            FrmPais frm = new FrmPais(new LoginController().getUsuario());
            frm.setVisible(true);
        });
    }
}
