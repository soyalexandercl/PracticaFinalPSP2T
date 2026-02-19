package com.practica.cliente;

import com.practica.util.Ticket;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteSwing {

    public static void main(String[] argumentos) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception excepcion) {
            System.err.println("No se pudo cargar el look & feel: " + excepcion.getMessage());
        }

        ClienteSwing app = new ClienteSwing();

        SwingUtilities.invokeLater(app::mostrarVentana);
    }

    private void mostrarVentana() {
        VentanaPrincipal ventana = new VentanaPrincipal();
        ventana.setVisible(true);
    }

    // ────────────────────────────────────────────────
    // Clase para representar una fila en la tabla
    // ────────────────────────────────────────────────
    private class EntradaTicket {

        private int identificador;
        private String estado;
        private String tecnico;

        public EntradaTicket() {
            this.identificador = 0;
            this.estado = "ENVIANDO...";
            this.tecnico = "—";
        }

        public int getIdentificador() {
            return identificador;
        }

        public String getEstado() {
            return estado;
        }

        public String getTecnico() {
            return tecnico;
        }

        public void setIdentificador(int identificador) {
            this.identificador = identificador;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public void setTecnico(String tecnico) {
            this.tecnico = tecnico;
        }
    }

    // ────────────────────────────────────────────────
    // Modelo de la tabla
    // ────────────────────────────────────────────────
    private class ModeloTablaTickets extends AbstractTableModel {

        private final String[] columnas = {"ID", "Estado", "Técnico"};
        private final List<EntradaTicket> entradas = new ArrayList<>();

        public void agregarEntrada(EntradaTicket entrada) {
            entradas.add(entrada);
            fireTableRowsInserted(entradas.size() - 1, entradas.size() - 1);
        }

        public void actualizarEntrada(EntradaTicket entrada) {
            int indice = entradas.indexOf(entrada);
            if (indice >= 0) {
                fireTableRowsUpdated(indice, indice);
            }
        }

        @Override
        public int getRowCount() {
            return entradas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnas[col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            EntradaTicket entrada = entradas.get(fila);
            return switch (columna) {
                case 0 ->
                    entrada.getIdentificador() == 0 ? "—" : entrada.getIdentificador();
                case 1 ->
                    entrada.getEstado();
                case 2 ->
                    entrada.getTecnico();
                default ->
                    "";
            };
        }
    }

    // ────────────────────────────────────────────────
    // Panel izquierdo: Crear ticket
    // ────────────────────────────────────────────────
    private class PanelCrearTicket extends JPanel {

        private final JTextField campoCliente = new JTextField(20);
        private final JTextArea areaDescripcion = new JTextArea(4, 20);
        private final JButton botonCrear = new JButton("Crear");
        private final JButton botonLimpiar = new JButton("Limpiar");
        private final PanelSeguimientoTickets panelSeguimiento;

        public PanelCrearTicket(PanelSeguimientoTickets panelSeguimiento) {
            this.panelSeguimiento = panelSeguimiento;
            areaDescripcion.setLineWrap(true);
            areaDescripcion.setWrapStyleWord(true);
            construirLayout();
            configurarEventos();
        }

        private void construirLayout() {
            setBorder(new TitledBorder("Crear Ticket"));
            setPreferredSize(new Dimension(260, 0));
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 8, 4, 8);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;

            JLabel etiquetaCliente = new JLabel("Cliente:");
            JLabel etiquetaDescripcion = new JLabel("Descripción:");

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weighty = 0;
            add(etiquetaCliente, gbc);

            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(campoCliente, gbc);

            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.NONE;
            add(etiquetaDescripcion, gbc);

            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            add(new JScrollPane(areaDescripcion), gbc);

            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panelBotones.add(botonCrear);
            panelBotones.add(botonLimpiar);

            gbc.gridy = 4;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
            add(panelBotones, gbc);
        }

        private void configurarEventos() {
            botonCrear.addActionListener(e -> {
                String nombreCliente = campoCliente.getText().trim();
                String descripcion = areaDescripcion.getText().trim();

                if (nombreCliente.isEmpty() || descripcion.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Los campos Cliente y Descripción son obligatorios.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Cliente cliente = new Cliente();
                String prioridad = cliente.generarPrioridad(descripcion);

                Ticket nuevoTicket = new Ticket(nombreCliente, descripcion, prioridad);

                EntradaTicket entrada = new EntradaTicket();
                panelSeguimiento.agregarEntrada(entrada);

                Thread hiloEnvio = new Thread(()
                        -> cliente.registrarTicket(nuevoTicket, ticketActualizado
                                -> SwingUtilities.invokeLater(() -> {
                            entrada.setIdentificador(ticketActualizado.getId());
                            entrada.setEstado(ticketActualizado.getEstado());
                            String tecnico = ticketActualizado.getTecnicoAsignado();
                            entrada.setTecnico(tecnico.isEmpty() ? "—" : tecnico);
                            panelSeguimiento.actualizarEntrada(entrada);
                        })
                        )
                );
                hiloEnvio.setDaemon(true);
                hiloEnvio.start();

                limpiarFormulario();
            });

            botonLimpiar.addActionListener(e -> limpiarFormulario());
        }

        private void limpiarFormulario() {
            campoCliente.setText("");
            areaDescripcion.setText("");
            campoCliente.requestFocus();
        }
    }

    // ────────────────────────────────────────────────
    // Panel derecho: Seguimiento de tickets
    // ────────────────────────────────────────────────
    private class PanelSeguimientoTickets extends JPanel {

        private final ModeloTablaTickets modeloTabla = new ModeloTablaTickets();
        private final JTable tablaTickets = new JTable(modeloTabla);

        public PanelSeguimientoTickets() {
            tablaTickets.setRowHeight(24);
            tablaTickets.setGridColor(new Color(210, 210, 210));
            tablaTickets.setDefaultRenderer(Object.class, new RenderizadorFuenteUniforme());
            tablaTickets.setEnabled(false); // Solo lectura

            tablaTickets.getColumnModel().getColumn(0).setPreferredWidth(50);
            tablaTickets.getColumnModel().getColumn(1).setPreferredWidth(120);
            tablaTickets.getColumnModel().getColumn(2).setPreferredWidth(100);

            setBorder(new TitledBorder("Mis tickets"));
            setLayout(new BorderLayout());
            add(new JScrollPane(tablaTickets), BorderLayout.CENTER);
        }

        public void agregarEntrada(EntradaTicket entrada) {
            modeloTabla.agregarEntrada(entrada);
        }

        public void actualizarEntrada(EntradaTicket entrada) {
            modeloTabla.actualizarEntrada(entrada);
        }

        private class RenderizadorFuenteUniforme extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor,
                    boolean estaSeleccionado, boolean tieneFoco, int fila, int columna) {
                Component celda = super.getTableCellRendererComponent(
                        tabla, valor, estaSeleccionado, tieneFoco, fila, columna);
                // Sin setFont → usa la fuente por defecto del sistema / L&F
                return celda;
            }
        }
    }

    // ────────────────────────────────────────────────
    // Ventana principal
    // ────────────────────────────────────────────────
    private class VentanaPrincipal extends JFrame {

        public VentanaPrincipal() {
            PanelSeguimientoTickets panelSeguimiento = new PanelSeguimientoTickets();
            PanelCrearTicket panelCrearTicket = new PanelCrearTicket(panelSeguimiento);

            JPanel contenido = new JPanel(new BorderLayout(10, 0));
            contenido.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            contenido.add(panelCrearTicket, BorderLayout.WEST);
            contenido.add(panelSeguimiento, BorderLayout.CENTER);

            setTitle("HelpDesk — Cliente");
            setContentPane(contenido);
            setMinimumSize(new Dimension(700, 500));
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setLocationRelativeTo(null);
            pack();
        }
    }
}
