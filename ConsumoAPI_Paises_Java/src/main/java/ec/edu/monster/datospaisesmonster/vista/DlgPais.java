package ec.edu.monster.datospaisesmonster.vista;

import ec.edu.monster.datospaisesmonster.modelo.Pais;
import ec.edu.monster.datospaisesmonster.modelo.Region;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

/**
 * Subpantalla (diálogo modal) para el ingreso o la edición de un país.
 * La vista principal solo abre este diálogo; aquí se validan los datos.
 */
public class DlgPais extends JDialog {

    private final Pais original;
    private Pais resultado;

    private final javax.swing.JTextField txtNombre = Tema.campoTexto();
    private final javax.swing.JTextField txtCapital = Tema.campoTexto();
    private final JComboBox<String> cmbRegion = new JComboBox<>(Region.etiquetas());
    private final javax.swing.JTextField txtPoblacion = Tema.campoTexto();
    private final javax.swing.JTextField txtMoneda = Tema.campoTexto();

    private DlgPais(Window owner, String titulo, String subtitulo, Pais inicial) {
        super(owner, titulo, ModalityType.APPLICATION_MODAL);
        this.original = inicial;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        add(crearCabecera(titulo, subtitulo), BorderLayout.NORTH);
        add(crearFormulario(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        if (inicial != null) {
            txtNombre.setText(valor(inicial.getNombre()));
            txtCapital.setText(valor(inicial.getCapital()));
            cmbRegion.setSelectedItem(valor(inicial.getRegion()));
            txtPoblacion.setText(String.valueOf(inicial.getPoblacion()));
            txtMoneda.setText(valor(inicial.getMoneda()));
        }

        getRootPane().setDefaultButton(null);
        pack();
        setLocationRelativeTo(owner);
        txtNombre.requestFocusInWindow();
    }

    /**
     * Abre la subpantalla y devuelve el país ingresado, o null si se cancela.
     *
     * @param owner ventana padre.
     * @param titulo título de la subpantalla.
     * @param subtitulo texto de apoyo bajo el título.
     * @param inicial país a editar, o null para un ingreso nuevo.
     */
    public static Pais mostrar(Window owner, String titulo, String subtitulo, Pais inicial) {
        DlgPais dialogo = new DlgPais(owner, titulo, subtitulo, inicial);
        dialogo.setVisible(true);
        return dialogo.resultado;
    }

    private JPanel crearCabecera(String titulo, String subtitulo) {
        Tema.PanelDegradado cabecera = new Tema.PanelDegradado();
        cabecera.setLayout(new javax.swing.BoxLayout(cabecera, javax.swing.BoxLayout.Y_AXIS));
        cabecera.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel(subtitulo);
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
        contenedor.setBorder(new EmptyBorder(18, 22, 8, 22));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);

        cmbRegion.setFont(Tema.fuente(Font.PLAIN, 13));
        cmbRegion.setBackground(Color.WHITE);
        cmbRegion.setPreferredSize(new Dimension(220, 30));

        txtNombre.setPreferredSize(new Dimension(260, 30));
        txtCapital.setPreferredSize(new Dimension(260, 30));
        txtPoblacion.setPreferredSize(new Dimension(260, 30));
        txtMoneda.setPreferredSize(new Dimension(260, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(formulario, gbc, 0, "Nombre:", txtNombre);
        agregarCampo(formulario, gbc, 1, "Capital:", txtCapital);
        agregarCampo(formulario, gbc, 2, "Región:", cmbRegion);
        agregarCampo(formulario, gbc, 3, "Población:", txtPoblacion);
        agregarCampo(formulario, gbc, 4, "Moneda:", txtMoneda);

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
        gbc.weightx = 0;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));

        JButton btnCancelar = new Tema.Boton("Cancelar", new Color(0x8A, 0x93, 0xA6));
        JButton btnGuardar = new Tema.Boton(original == null ? "Registrar país" : "Guardar cambios", Tema.COLOR_3);

        btnCancelar.addActionListener(e -> {
            resultado = null;
            dispose();
        });
        btnGuardar.addActionListener(e -> guardar());

        panel.add(btnCancelar);
        panel.add(btnGuardar);
        return panel;
    }

    private void guardar() {
        try {
            Pais pais = leerFormulario();
            resultado = pais;
            dispose();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Pais leerFormulario() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre del país es obligatorio.");
        }

        String capital = txtCapital.getText().trim();
        String region = (String) cmbRegion.getSelectedItem();
        if (region == null || region.isBlank()) {
            region = Region.OTROS.getEtiqueta();
        }

        long poblacion = 0;
        String textoPoblacion = txtPoblacion.getText().trim();
        if (!textoPoblacion.isEmpty()) {
            try {
                poblacion = Long.parseLong(textoPoblacion);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("La población debe ser un número entero.");
            }
            if (poblacion < 0) {
                throw new IllegalArgumentException("La población no puede ser negativa.");
            }
        }

        String moneda = txtMoneda.getText().trim();

        Pais pais = new Pais(nombre, capital, region, poblacion, moneda);
        if (original != null) {
            pais.setId(original.getId());
        }
        return pais;
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }
}
