package ec.edu.monster.datospaisesmonster.vista;

import ec.edu.monster.datospaisesmonster.controlador.PaisController;
import ec.edu.monster.datospaisesmonster.modelo.Pais;
import ec.edu.monster.datospaisesmonster.modelo.Region;
import ec.edu.monster.datospaisesmonster.modelo.ReporteEstadistico;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Vista principal del taller.
 *
 * El ingreso y la edición de países se realizan en subpantallas (DlgPais).
 * Las acciones Actualizar y Eliminar permanecen deshabilitadas hasta que el
 * usuario seleccione una fila guardada en la tabla.
 */
public class FrmPais extends JFrame {

    private final PaisController controller = new PaisController();
    private final List<Pais> paisesEnTabla = new ArrayList<>();
    private final String usuarioSesion;

    private DefaultTableModel modeloTabla;
    private JTable tblPaises;
    private JTextField txtBuscarNombre;
    private JComboBox<String> cmbRegionFiltro;

    private JButton btnNuevo;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnImportar;
    private JButton btnListar;
    private JButton btnReportes;
    private JButton btnBorrar;
    private JButton btnBuscarNombre;
    private JButton btnFiltrarRegion;

    private JLabel lblEstado;
    private JLabel lblSeleccion;
    private JLabel lblUsuario;

    public FrmPais() {
        this(null);
    }

    public FrmPais(String usuario) {
        this.usuarioSesion = usuario;
        initComponents();
        cargarTabla(controller.listarPaises());
        actualizarEstadoInicial();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Taller Consumo API - Países (MVC)");
        setLayout(new BorderLayout());
        getContentPane().setBackground(Tema.FONDO);

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);

        conectarEventos();

        setMinimumSize(new Dimension(880, 540));

        // Ajusta el tamaño a la pantalla disponible (considera escalado de Windows).
        Dimension pantalla = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int ancho = Math.min(1080, Math.max(880, pantalla.width - 100));
        int alto = Math.min(680, Math.max(540, pantalla.height - 110));
        setSize(ancho, alto);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JPanel crearCabecera() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new javax.swing.BoxLayout(contenedor, javax.swing.BoxLayout.Y_AXIS));
        contenedor.setBackground(Tema.FONDO);

        contenedor.add(crearTitulo());
        contenedor.add(crearBarraAcciones());
        contenedor.add(crearBarraBusqueda());
        return contenedor;
    }

    private JPanel crearTitulo() {
        Tema.PanelDegradado cabecera = new Tema.PanelDegradado();
        cabecera.setLayout(new BorderLayout(12, 0));
        cabecera.setBorder(new EmptyBorder(14, 22, 14, 18));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Países · Taller de consumo de API");
        lblTitulo.setFont(Tema.fuente(Font.BOLD, 21));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(
                "REST Countries v5 · MySQL · Patrón MVC con DAO");
        lblSubtitulo.setFont(Tema.fuente(Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(0xEA, 0xF3, 0xFF));

        textos.add(lblTitulo);
        textos.add(javax.swing.Box.createVerticalStrut(3));
        textos.add(lblSubtitulo);

        // Botón de limpiar en la parte superior.
        btnLimpiar = new Tema.Boton("Limpiar", new Color(0x35, 0x47, 0x6B));
        btnLimpiar.setToolTipText("Limpia la búsqueda, el filtro y la selección de la tabla");

        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelDerecha.setOpaque(false);
        panelDerecha.add(btnLimpiar);

        cabecera.add(textos, BorderLayout.CENTER);
        cabecera.add(panelDerecha, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearBarraAcciones() {
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                new EmptyBorder(10, 18, 10, 18)));

        // Un FlowLayout central permite que los botones bajen de línea solos
        // cuando la ventana es angosta, sin recortar acciones.
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setBackground(Color.WHITE);

        btnNuevo = new Tema.Boton("Nuevo ingreso de país", Tema.COLOR_3);
        btnNuevo.setToolTipText("Abre una subpantalla para registrar un país");
        btnActualizar = new Tema.Boton("Actualizar", Tema.COLOR_2);
        btnActualizar.setToolTipText("Requiere seleccionar una fila guardada en la tabla");
        btnEliminar = new Tema.Boton("Eliminar", Tema.COLOR_1);
        btnEliminar.setForeground(Tema.TEXTO);
        btnEliminar.setToolTipText("Requiere seleccionar una fila guardada en la tabla");

        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);

        btnImportar = new Tema.Boton("Importar todos", Tema.COLOR_2);
        btnImportar.setToolTipText("Descarga todos los países de la API y los guarda en MySQL");
        btnListar = new Tema.Boton("Listar BD", new Color(0x6C, 0x7A, 0x95));
        btnListar.setToolTipText("Vuelve a cargar la tabla desde MySQL");
        btnReportes = new Tema.Boton("Reportes", new Color(0x6C, 0x7A, 0x95));
        btnReportes.setToolTipText("Muestra los reportes estadísticos");

        btnBorrar = new Tema.Boton("Borrar datos", new Color(0xC9, 0x4F, 0x4F));
        btnBorrar.setToolTipText("Verifica los datos y vacía la tabla con TRUNCATE");

        acciones.add(btnNuevo);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        acciones.add(btnImportar);
        acciones.add(btnListar);
        acciones.add(btnReportes);
        acciones.add(btnBorrar);

        barra.add(acciones, BorderLayout.CENTER);
        return barra;
    }

    private JPanel crearBarraBusqueda() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(Tema.FONDO);
        barra.setBorder(new EmptyBorder(4, 14, 0, 14));

        JLabel lblBuscar = new JLabel("Buscar por nombre (API):");
        lblBuscar.setFont(Tema.fuente(Font.PLAIN, 13));
        lblBuscar.setForeground(Tema.TEXTO);

        txtBuscarNombre = Tema.campoTexto();
        txtBuscarNombre.setPreferredSize(new Dimension(220, 30));

        btnBuscarNombre = new Tema.Boton("Buscar", Tema.COLOR_2);

        JLabel lblRegion = new JLabel("Región:");
        lblRegion.setFont(Tema.fuente(Font.PLAIN, 13));
        lblRegion.setForeground(Tema.TEXTO);

        cmbRegionFiltro = new JComboBox<>(Region.etiquetasFiltro());
        cmbRegionFiltro.setFont(Tema.fuente(Font.PLAIN, 13));
        cmbRegionFiltro.setBackground(Color.WHITE);
        cmbRegionFiltro.setPreferredSize(new Dimension(150, 30));

        btnFiltrarRegion = new Tema.Boton("Filtrar", Tema.COLOR_3);

        barra.add(lblBuscar);
        barra.add(txtBuscarNombre);
        barra.add(btnBuscarNombre);
        barra.add(javax.swing.Box.createHorizontalStrut(14));
        barra.add(lblRegion);
        barra.add(cmbRegionFiltro);
        barra.add(btnFiltrarRegion);
        return barra;
    }

    private JPanel crearCentro() {
        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Capital", "Región", "Población", "Moneda"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblPaises = new JTable(modeloTabla);
        tblPaises.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPaises.setAutoCreateRowSorter(true);
        tblPaises.setFillsViewportHeight(true);
        Tema.prepararTabla(tblPaises);

        tblPaises.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblPaises.getColumnModel().getColumn(1).setPreferredWidth(230);
        tblPaises.getColumnModel().getColumn(2).setPreferredWidth(170);
        tblPaises.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblPaises.getColumnModel().getColumn(4).setPreferredWidth(130);
        tblPaises.getColumnModel().getColumn(5).setPreferredWidth(100);

        tblPaises.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoSeleccion();
            }
        });

        // Doble clic: abre la subpantalla de actualización de la fila seleccionada.
        tblPaises.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblPaises.getSelectedRow() >= 0) {
                    abrirActualizacion();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblPaises);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Tema.FONDO);
        centro.setBorder(new EmptyBorder(6, 18, 12, 18));
        centro.add(scroll, BorderLayout.CENTER);
        return centro;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                new EmptyBorder(8, 18, 8, 18)));

        lblEstado = new JLabel(" ");
        lblEstado.setFont(Tema.fuente(Font.PLAIN, 12));
        lblEstado.setForeground(Tema.TEXTO_SUAVE);

        lblSeleccion = new JLabel("Ninguna fila seleccionada.");
        lblSeleccion.setFont(Tema.fuente(Font.BOLD, 12));
        lblSeleccion.setForeground(Tema.COLOR_3);
        lblSeleccion.setHorizontalAlignment(SwingConstants.RIGHT);

        lblUsuario = new JLabel("Sesión: " + (esVacio(usuarioSesion) ? "invitado" : usuarioSesion));
        lblUsuario.setFont(Tema.fuente(Font.BOLD, 12));
        lblUsuario.setForeground(Tema.TEXTO_SUAVE);

        javax.swing.JSeparator separador = new javax.swing.JSeparator(SwingConstants.VERTICAL);
        separador.setPreferredSize(new Dimension(1, 16));
        separador.setForeground(Tema.BORDE);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setBackground(Color.WHITE);
        derecha.add(lblSeleccion);
        derecha.add(separador);
        derecha.add(lblUsuario);

        barra.add(lblEstado, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private void conectarEventos() {
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        btnActualizar.addActionListener(this::btnActualizarActionPerformed);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        btnImportar.addActionListener(this::btnImportarActionPerformed);
        btnListar.addActionListener(this::btnListarActionPerformed);
        btnReportes.addActionListener(this::btnReportesActionPerformed);
        btnBorrar.addActionListener(this::btnBorrarActionPerformed);
        btnBuscarNombre.addActionListener(this::btnBuscarNombreActionPerformed);
        btnFiltrarRegion.addActionListener(this::btnFiltrarRegionActionPerformed);
        getRootPane().setDefaultButton(btnBuscarNombre);
    }

    // ------------------------------------------------------------------
    // Estado de la tabla y de los botones
    // ------------------------------------------------------------------

    private Pais paisSeleccionado() {
        int filaVista = tblPaises.getSelectedRow();
        if (filaVista < 0) {
            return null;
        }
        int filaModelo = tblPaises.convertRowIndexToModel(filaVista);
        if (filaModelo < 0 || filaModelo >= paisesEnTabla.size()) {
            return null;
        }
        return paisesEnTabla.get(filaModelo);
    }

    /**
     * Actualizar y Eliminar solo se habilitan cuando hay una fila guardada
     * (con ID) seleccionada.
     */
    private void actualizarEstadoSeleccion() {
        Pais pais = paisSeleccionado();
        boolean editable = pais != null && pais.getId() > 0;

        btnActualizar.setEnabled(editable);
        btnEliminar.setEnabled(editable);

        if (pais == null) {
            lblSeleccion.setText("Ninguna fila seleccionada.");
        } else if (pais.getId() <= 0) {
            lblSeleccion.setText("Resultado de API sin guardar: " + valor(pais.getNombre()));
        } else {
            lblSeleccion.setText("Seleccionado: " + valor(pais.getNombre())
                    + " (ID " + pais.getId() + ")");
        }
    }

    private void actualizarEstadoInicial() {
        String modo = controller.apiEnDemo() ? "demo (1 país de muestra)" : "real";
        if (paisesEnTabla.isEmpty()) {
            lblEstado.setText("Listo. Tabla vacía o sin conexión. API: " + modo
                    + ". Use \"Nuevo ingreso de país\" o \"Importar todos\".");
        } else {
            lblEstado.setText("Listo. Países en BD: " + paisesEnTabla.size()
                    + ". API: " + modo + ".");
        }
        actualizarEstadoSeleccion();
    }

    private void cargarTabla(List<Pais> lista) {
        paisesEnTabla.clear();
        paisesEnTabla.addAll(lista);
        modeloTabla.setRowCount(0);

        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-EC"));
        for (Pais pais : lista) {
            modeloTabla.addRow(new Object[]{
                pais.getId() > 0 ? pais.getId() : "",
                valor(pais.getNombre()),
                valor(pais.getCapital()),
                valor(pais.getRegion()),
                formato.format(pais.getPoblacion()),
                valor(pais.getMoneda())
            });
        }

        tblPaises.clearSelection();
        actualizarEstadoSeleccion();

        if (lblEstado != null) {
            lblEstado.setText("Mostrando " + lista.size() + " país(es).");
        }
    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void btnNuevoActionPerformed(ActionEvent evt) {
        Pais nuevo = DlgPais.mostrar(this,
                "Nuevo ingreso de país",
                "Complete los datos y registre el país en MySQL.",
                null);

        if (nuevo == null) {
            lblEstado.setText("Registro cancelado.");
            return;
        }

        if (controller.guardarPais(nuevo)) {
            cargarTabla(controller.listarPaises());
            lblEstado.setText("País guardado: " + nuevo.getNombre());
            JOptionPane.showMessageDialog(this,
                    "País guardado correctamente.",
                    "Guardar", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar el país. Verifique la conexión a MySQL.",
                    "Guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnActualizarActionPerformed(ActionEvent evt) {
        abrirActualizacion();
    }

    private void abrirActualizacion() {
        Pais seleccionado = paisSeleccionado();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una fila de la tabla para actualizarla.",
                    "Actualizar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (seleccionado.getId() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Este país proviene de una búsqueda en la API y aún no está guardado.\n"
                    + "Use \"Nuevo ingreso de país\" para registrarlo antes de actualizarlo.",
                    "Actualizar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pais editado = DlgPais.mostrar(this,
                "Actualizar país",
                "Modifique los datos del país seleccionado.",
                copiar(seleccionado));

        if (editado == null) {
            lblEstado.setText("Actualización cancelada.");
            return;
        }

        if (controller.actualizarPais(editado)) {
            cargarTabla(controller.listarPaises());
            lblEstado.setText("País actualizado: " + editado.getNombre());
            JOptionPane.showMessageDialog(this,
                    "País actualizado correctamente.",
                    "Actualizar", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo actualizar. Verifique que el país exista y que MySQL esté disponible.",
                    "Actualizar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnEliminarActionPerformed(ActionEvent evt) {
        Pais seleccionado = paisSeleccionado();
        if (seleccionado == null || seleccionado.getId() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una fila guardada en la tabla para eliminarla.",
                    "Eliminar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el país \"" + valor(seleccionado.getNombre()) + "\"?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        if (controller.eliminarPais(seleccionado.getId())) {
            cargarTabla(controller.listarPaises());
            lblEstado.setText("País eliminado: " + valor(seleccionado.getNombre()));
            JOptionPane.showMessageDialog(this,
                    "País eliminado correctamente.",
                    "Eliminar", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar el país. Verifique la conexión a MySQL.",
                    "Eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnLimpiarActionPerformed(ActionEvent evt) {
        txtBuscarNombre.setText("");
        cmbRegionFiltro.setSelectedIndex(0);
        tblPaises.clearSelection();
        cargarTabla(controller.listarPaises());
        txtBuscarNombre.requestFocusInWindow();
        lblEstado.setText("Vista y selección limpiadas. Países: " + paisesEnTabla.size());
    }

    private void btnImportarActionPerformed(ActionEvent evt) {
        setBotonesHabilitados(false);
        lblEstado.setText("Importando países desde la API...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return controller.importarPaises();
            }

            @Override
            protected void done() {
                try {
                    int importados = get();
                    cargarTabla(controller.listarPaises());

                    String mensaje = "Importación finalizada.\nPaíses guardados: " + importados;
                    if (importados == 0) {
                        mensaje += "\n\nNo se guardó ningún país. Verifique la conexión a MySQL.";
                    }
                    JOptionPane.showMessageDialog(FrmPais.this, mensaje,
                            "Importación", JOptionPane.INFORMATION_MESSAGE);
                    lblEstado.setText("Importación finalizada. Países guardados: " + importados);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("Importación interrumpida", e);
                } catch (ExecutionException e) {
                    mostrarError("No se pudo importar", e.getCause());
                } finally {
                    setBotonesHabilitados(true);
                }
            }
        }.execute();
    }

    private void btnListarActionPerformed(ActionEvent evt) {
        cargarTabla(controller.listarPaises());
        lblEstado.setText("Listado actualizado desde MySQL. Países: " + paisesEnTabla.size());
    }

    private void btnBuscarNombreActionPerformed(ActionEvent evt) {
        String nombre = txtBuscarNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba un nombre para buscar.",
                    "Búsqueda", JOptionPane.WARNING_MESSAGE);
            txtBuscarNombre.requestFocusInWindow();
            return;
        }

        setBotonesHabilitados(false);
        lblEstado.setText("Buscando \"" + nombre + "\" en la API...");

        new SwingWorker<List<Pais>, Void>() {
            @Override
            protected List<Pais> doInBackground() {
                return controller.buscarPorNombreEnAPI(nombre);
            }

            @Override
            protected void done() {
                try {
                    List<Pais> resultados = get();
                    cargarTabla(resultados);

                    if (resultados.isEmpty()) {
                        JOptionPane.showMessageDialog(FrmPais.this,
                                "No se encontraron países con ese nombre.",
                                "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
                        lblEstado.setText("Sin resultados para \"" + nombre + "\".");
                    } else {
                        String demo = controller.apiEnDemo() ? " (API en modo demo)" : "";
                        lblEstado.setText("Resultados de API: " + resultados.size() + demo
                                + ". Aún no guardados.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("Búsqueda interrumpida", e);
                } catch (ExecutionException e) {
                    mostrarError("No se pudo buscar en la API", e.getCause());
                } finally {
                    setBotonesHabilitados(true);
                }
            }
        }.execute();
    }

    private void btnFiltrarRegionActionPerformed(ActionEvent evt) {
        String region = (String) cmbRegionFiltro.getSelectedItem();
        List<Pais> lista = "Todas".equals(region)
                ? controller.listarPaises()
                : controller.buscarPorRegion(region);

        cargarTabla(lista);
        lblEstado.setText("Filtro por región: " + region + ". Países: " + lista.size());
    }

    private void btnReportesActionPerformed(ActionEvent evt) {
        ReporteEstadistico reporte = controller.generarReporte();

        JTextArea area = new JTextArea(reporte.toTexto(), 20, 55);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, scroll,
                "Reportes estadísticos", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Verifica cuántos países hay en la base y cuántos reporta la API antes
     * de vaciar la tabla con TRUNCATE.
     */
    private void btnBorrarActionPerformed(ActionEvent evt) {
        setBotonesHabilitados(false);
        lblEstado.setText("Verificando datos antes de borrar...");

        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                return new int[]{
                    controller.contarPaises(),
                    controller.totalPaisesEnApi()
                };
            }

            @Override
            protected void done() {
                try {
                    int[] datos = get();
                    confirmarBorrado(datos[0], datos[1]);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("Verificación interrumpida", e);
                } catch (ExecutionException e) {
                    mostrarError("No se pudo verificar la base de datos", e.getCause());
                } finally {
                    setBotonesHabilitados(true);
                }
            }
        }.execute();
    }

    private void confirmarBorrado(int enBase, int enApi) {
        if (enBase == 0) {
            lblEstado.setText("La tabla ya está vacía.");
            JOptionPane.showMessageDialog(this,
                    "La tabla pais ya está vacía. No hay datos por borrar.",
                    "Borrar datos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (enBase < 0) {
            lblEstado.setText("No se pudo consultar la base de datos.");
            JOptionPane.showMessageDialog(this,
                    "No se pudo consultar la base de datos.\n"
                    + "Verifique que MySQL esté disponible.",
                    "Borrar datos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Se eliminarán TODOS los registros de la tabla pais con TRUNCATE.\n\n");
        mensaje.append("Países guardados en la base de datos: ").append(enBase).append('\n');

        if (enApi > 0) {
            mensaje.append("Países reportados por la API: ").append(enApi).append("\n\n");
            if (enBase >= enApi) {
                mensaje.append("Verificación: la base ya contiene todos los países de la API.\n\n");
            } else {
                mensaje.append("Atención: aún faltan ")
                        .append(enApi - enBase)
                        .append(" país(es) por importar.\n\n");
            }
        } else {
            mensaje.append("\nNo se pudo verificar el total de la API.\n\n");
        }

        mensaje.append("Esta acción no se puede deshacer. ¿Desea continuar?");

        Object[] opciones = {"Sí, borrar", "Cancelar"};
        int opcion = JOptionPane.showOptionDialog(this,
                mensaje.toString(),
                "Borrar datos (TRUNCATE)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[1]);

        if (opcion != 0) {
            lblEstado.setText("Borrado cancelado.");
            return;
        }

        if (controller.borrarTodosLosPaises()) {
            cargarTabla(controller.listarPaises());
            lblEstado.setText("Datos borrados con TRUNCATE. Registros eliminados: " + enBase);
            JOptionPane.showMessageDialog(this,
                    "Se eliminaron " + enBase + " registro(s).\n"
                    + "La tabla pais quedó vacía y el ID vuelve a iniciar en 1.",
                    "Borrar datos", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron borrar los datos. Verifique la conexión a MySQL.",
                    "Borrar datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Pais copiar(Pais pais) {
        Pais copia = new Pais(pais.getNombre(), pais.getCapital(), pais.getRegion(),
                pais.getPoblacion(), pais.getMoneda());
        copia.setId(pais.getId());
        return copia;
    }

    private void setBotonesHabilitados(boolean habilitado) {
        btnNuevo.setEnabled(habilitado);
        btnLimpiar.setEnabled(habilitado);
        btnImportar.setEnabled(habilitado);
        btnListar.setEnabled(habilitado);
        btnReportes.setEnabled(habilitado);
        btnBorrar.setEnabled(habilitado);
        btnBuscarNombre.setEnabled(habilitado);
        btnFiltrarRegion.setEnabled(habilitado);
        tblPaises.setEnabled(habilitado);
        txtBuscarNombre.setEnabled(habilitado);
        cmbRegionFiltro.setEnabled(habilitado);

        if (habilitado) {
            actualizarEstadoSeleccion();
        } else {
            btnActualizar.setEnabled(false);
            btnEliminar.setEnabled(false);
        }
    }

    private void mostrarError(String titulo, Throwable error) {
        String mensaje = error == null ? "Error desconocido" : error.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = error == null ? "Error desconocido" : error.toString();
        }
        if (error != null) {
            error.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
        lblEstado.setText(titulo + ": " + mensaje);
    }
}
