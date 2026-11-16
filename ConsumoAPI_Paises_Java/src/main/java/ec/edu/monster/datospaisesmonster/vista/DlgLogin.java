package ec.edu.monster.datospaisesmonster.vista;

import ec.edu.monster.datospaisesmonster.controlador.LoginController;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

/**
 * Subpantalla de acceso. La aplicación principal no se abre hasta que el
 * usuario se autentique correctamente.
 */
public class DlgLogin extends JDialog {

    private final LoginController controller = new LoginController();

    private final JTextField txtUsuario = Tema.campoTexto();
    private final JPasswordField txtClave = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");

    private boolean autenticado;

    private DlgLogin(Window owner) {
        super(owner, "Acceso al sistema", ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        registrarEscape();
        pack();
        setLocationRelativeTo(owner);
        txtUsuario.requestFocusInWindow();
    }

    /**
     * Muestra la subpantalla de login.
     *
     * @param owner ventana padre (puede ser null).
     * @return true si el usuario se autenticó correctamente.
     */
    public static boolean mostrar(Window owner) {
        DlgLogin dialogo = new DlgLogin(owner);
        dialogo.setVisible(true);
        return dialogo.autenticado;
    }

    private JPanel crearCabecera() {
        Tema.PanelDegradado cabecera = new Tema.PanelDegradado();
        cabecera.setLayout(new javax.swing.BoxLayout(cabecera, javax.swing.BoxLayout.Y_AXIS));
        cabecera.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel lblTitulo = new JLabel("Acceso al sistema");
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Taller de consumo de API · Países");
        lblSubtitulo.setFont(Tema.fuente(Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(0xEA, 0xF3, 0xFF));
        lblSubtitulo.setAlignmentX(LEFT_ALIGNMENT);

        cabecera.add(lblTitulo);
        cabecera.add(javax.swing.Box.createVerticalStrut(4));
        cabecera.add(lblSubtitulo);
        return cabecera;
    }

    private JPanel crearFormulario() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Color.WHITE);
        contenedor.setBorder(new EmptyBorder(18, 22, 6, 22));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);

        txtUsuario.setPreferredSize(new Dimension(230, 30));

        txtClave.setFont(Tema.fuente(Font.PLAIN, 13));
        txtClave.setForeground(Tema.TEXTO);
        txtClave.setPreferredSize(new Dimension(230, 30));
        txtClave.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                new EmptyBorder(6, 9, 6, 9)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(formulario, gbc, 0, "Usuario:", txtUsuario);
        agregarCampo(formulario, gbc, 1, "Clave:", txtClave);

        lblError.setFont(Tema.fuente(Font.PLAIN, 12));
        lblError.setForeground(new Color(0xC9, 0x3B, 0x3B));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 0, 0, 0);
        formulario.add(lblError, gbc);

        contenedor.add(formulario, BorderLayout.CENTER);
        return contenedor;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc,
            int fila, String etiqueta, JComponent campo) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(Tema.fuente(Font.PLAIN, 13));
        lbl.setForeground(Tema.TEXTO_SUAVE);

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(6, 0, 6, 10);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        panel.setBackground(Color.WHITE);

        JButton btnCancelar = new Tema.Boton("Cancelar", new Color(0x8A, 0x93, 0xA6));
        JButton btnIngresar = new Tema.Boton("Ingresar", Tema.COLOR_3);

        btnCancelar.addActionListener(e -> cancelar());
        btnIngresar.addActionListener(e -> ingresar());

        panel.add(btnCancelar);
        panel.add(btnIngresar);

        getRootPane().setDefaultButton(btnIngresar);
        return panel;
    }

    private void ingresar() {
        String usuario = txtUsuario.getText();
        String clave = new String(txtClave.getPassword());

        if (usuario.isBlank() || clave.isEmpty()) {
            lblError.setText("Ingrese usuario y clave.");
            return;
        }

        if (controller.autenticar(usuario, clave)) {
            autenticado = true;
            dispose();
        } else {
            lblError.setText("Usuario o clave incorrectos.");
            txtClave.setText("");
            txtClave.requestFocusInWindow();
        }
    }

    private void cancelar() {
        autenticado = false;
        dispose();
    }

    private void registrarEscape() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancelar-login");
        getRootPane().getActionMap().put("cancelar-login", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelar();
            }
        });
    }
}
