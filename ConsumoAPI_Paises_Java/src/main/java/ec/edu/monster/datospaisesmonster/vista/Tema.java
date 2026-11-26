package ec.edu.monster.datospaisesmonster.vista;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Paleta y componentes visuales compartidos por la vista principal y las
 * subpantallas (diálogos).
 */
public final class Tema {

    public static final Color COLOR_1 = new Color(0x4E, 0xD9, 0xCD);
    public static final Color COLOR_2 = new Color(0x4E, 0xA0, 0xD9);
    public static final Color COLOR_3 = new Color(0x4E, 0x5A, 0xD9);
    /** Morado corporativo adicional (#985FD4). */
    public static final Color COLOR_4 = new Color(0x98, 0x5F, 0xD4);

    public static final Color FONDO = new Color(0xF4, 0xF7, 0xFB);
    public static final Color TEXTO = new Color(0x1F, 0x2A, 0x44);
    public static final Color TEXTO_SUAVE = new Color(0x5B, 0x67, 0x7F);
    public static final Color BORDE = new Color(0xD5, 0xDD, 0xEA);
    public static final Color SELECCION = new Color(0xDD, 0xF6, 0xF3);

    private Tema() {
    }

    public static Font fuente(int estilo, int tamanio) {
        return new Font("Segoe UI", estilo, tamanio);
    }

    /**
     * Icono principal de la aplicación dentro de un círculo blanco.
     *
     * @param tamanio diámetro en píxeles.
     * @return el icono circular o null si no se pudo cargar el recurso.
     */
    public static ImageIcon iconoCircular(int tamanio) {
        BufferedImage original = cargarIcono();
        if (original == null || tamanio <= 0) {
            return null;
        }

        int lado = Math.min(original.getWidth(), original.getHeight());
        int x = (original.getWidth() - lado) / 2;
        int y = 0;

        BufferedImage salida = new BufferedImage(tamanio, tamanio, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = salida.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, tamanio, tamanio);
        g2.drawImage(original, 0, 0, tamanio, tamanio, x, y, x + lado, y + lado, null);

        // Recorta la imagen en forma circular con bordes suavizados.
        g2.setComposite(AlphaComposite.DstIn);
        g2.setColor(Color.BLACK);
        g2.fillOval(0, 0, tamanio, tamanio);
        g2.dispose();
        return new ImageIcon(salida);
    }

    /**
     * Panel transparente que muestra el icono principal, listo para colocar en
     * la esquina superior izquierda de una cabecera.
     */
    public static JPanel panelMarca(int tamanio) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        ImageIcon icono = iconoCircular(tamanio);
        if (icono != null) {
            panel.add(new JLabel(icono), BorderLayout.CENTER);
        }
        return panel;
    }

    /**
     * Imagen del icono para la barra de título y la barra de tareas.
     */
    public static Image iconoVentana() {
        return cargarIcono();
    }

    private static BufferedImage cargarIcono() {
        try (InputStream entrada = Tema.class.getResourceAsStream("/icono-app.jpg")) {
            return entrada == null ? null : ImageIO.read(entrada);
        } catch (IOException e) {
            return null;
        }
    }

    public static JTextField campoTexto() {
        JTextField campo = new JTextField();
        campo.setFont(fuente(Font.PLAIN, 13));
        campo.setForeground(TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(6, 9, 6, 9)));
        return campo;
    }

    public static void prepararTabla(javax.swing.JTable tabla) {
        tabla.setRowHeight(28);
        tabla.setFont(fuente(Font.PLAIN, 13));
        tabla.setForeground(TEXTO);
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(TEXTO);
        tabla.setGridColor(new Color(0xE6, 0xEC, 0xF5));
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new java.awt.Dimension(0, 1));
        javax.swing.table.TableCellRenderer cabecera = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t,
                    Object valor, boolean seleccionada, boolean enfocada, int fila, int columna) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, valor, seleccionada, enfocada, fila, columna);
                lbl.setOpaque(true);
                lbl.setBackground(COLOR_3);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(fuente(Font.BOLD, 12));
                lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        };
        tabla.getTableHeader().setDefaultRenderer(cabecera);
        tabla.getTableHeader().setOpaque(true);
        tabla.getTableHeader().setFont(fuente(Font.BOLD, 12));
        tabla.getTableHeader().setBackground(COLOR_3);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 32));

        // Todas las celdas alineadas a la izquierda (incluidos los números).
        javax.swing.table.DefaultTableCellRenderer izquierda =
                new javax.swing.table.DefaultTableCellRenderer();
        izquierda.setHorizontalAlignment(JLabel.LEFT);
        tabla.setDefaultRenderer(Object.class, izquierda);
    }

    /**
     * Botón plano con esquinas redondeadas y efecto hover/presionado.
     */
    public static class Boton extends JButton {

        private final Color base;

        public Boton(String texto, Color base) {
            super(texto);
            this.base = base;
            setFont(fuente(Font.BOLD, 12));
            setForeground(Color.WHITE);
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(9, 16, 9, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color color = base;
            if (!isEnabled()) {
                color = new Color(0xC3, 0xCB, 0xDA);
            } else if (getModel().isPressed()) {
                color = oscurecer(base, 0.18f);
            } else if (getModel().isRollover()) {
                color = oscurecer(base, 0.10f);
            }

            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();

            super.paintComponent(g);
        }

        private static Color oscurecer(Color color, float factor) {
            return new Color(
                    Math.max(0, Math.round(color.getRed() * (1 - factor))),
                    Math.max(0, Math.round(color.getGreen() * (1 - factor))),
                    Math.max(0, Math.round(color.getBlue() * (1 - factor))));
        }
    }

    /**
     * Panel con degradado de los colores del tema, incluido el morado.
     */
    public static class PanelDegradado extends JPanel {

        public PanelDegradado() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = Math.max(1, getWidth());
            g2.setPaint(new LinearGradientPaint(
                    new Point2D.Float(0, 0),
                    new Point2D.Float(ancho, 0),
                    new float[]{0f, 0.38f, 0.72f, 1f},
                    new Color[]{COLOR_3, COLOR_2, COLOR_4, COLOR_1}));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void decorarCabecera(JComponent componente) {
        componente.setOpaque(false);
    }
}
