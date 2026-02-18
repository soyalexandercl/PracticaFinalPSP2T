package com.practica.servidor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Aplicación HelpDesk — Todo en un único archivo. Internamente separado en
 * clases por responsabilidad: - Ticket → Modelo de datos - GestorDeTickets →
 * Lógica de negocio - ModeloTablaTickets → Modelo para JTable -
 * PanelCrearTicket → Formulario de creación - PanelEstadoSistema → Estadísticas
 * en tiempo real - PanelTablaTickets → Tabla de tickets - PanelAcciones →
 * Botones y filtros - VentanaPrincipal → Ensamblado general
 */
public class ServidorSwing {

    static final Font FUENTE_GLOBAL = new Font("SansSerif", Font.PLAIN, 13);

    public static void main(String[] argumentos) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception excepcion) {
            System.err.println("No se pudo cargar el look & feel del sistema: " + excepcion.getMessage());
        }

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    // ══════════════════════════════════════════════════════════════
    // MODELO DE DATOS
    // ══════════════════════════════════════════════════════════════
    public static class Ticket {

        enum Prioridad {
            BAJA, MEDIA, ALTA
        }

        enum Estado {
            PENDIENTE, EN_PROCESO, RESUELTO
        }

        private final int identificador;
        private final String cliente;
        private final Prioridad prioridad;
        private final String descripcion;
        private Estado estado;
        private String tecnico;

        Ticket(int identificador, String cliente, Prioridad prioridad, String descripcion) {
            this.identificador = identificador;
            this.cliente = cliente;
            this.prioridad = prioridad;
            this.descripcion = descripcion;
            this.estado = Estado.PENDIENTE;
            this.tecnico = "—";
        }

        int getIdentificador() {
            return identificador;
        }

        String getCliente() {
            return cliente;
        }

        Prioridad getPrioridad() {
            return prioridad;
        }

        String getDescripcion() {
            return descripcion;
        }

        Estado getEstado() {
            return estado;
        }

        String getTecnico() {
            return tecnico;
        }

        void setEstado(Estado estado) {
            this.estado = estado;
        }

        void setTecnico(String tecnico) {
            this.tecnico = tecnico;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO
    // ══════════════════════════════════════════════════════════════
    static class GestorDeTickets {

        private final List<Ticket> listaTickets;
        private final AtomicInteger contadorId;
        private final List<String> listaTecnicos;

        GestorDeTickets() {
            listaTickets = new ArrayList<>();
            contadorId = new AtomicInteger(100);
            listaTecnicos = new ArrayList<>(List.of("Ana", "Luis", "Marta", "Carlos", "Elena"));
            cargarDatosDemo();
        }

        private void cargarDatosDemo() {
            Ticket t102 = crearTicket("Cliente 2", Ticket.Prioridad.MEDIA, "Error en sistema");
            t102.setEstado(Ticket.Estado.EN_PROCESO);
            t102.setTecnico("Ana");

            Ticket t103 = crearTicket("Cliente 3", Ticket.Prioridad.ALTA, "Servidor caído");
            t103.setEstado(Ticket.Estado.RESUELTO);
            t103.setTecnico("Luis");

            crearTicket("Cliente 1", Ticket.Prioridad.BAJA, "Problema de acceso");
            crearTicket("Cliente 4", Ticket.Prioridad.MEDIA, "Lentitud en red");

            Ticket t105 = crearTicket("Cliente 5", Ticket.Prioridad.BAJA, "Actualización pendiente");
            t105.setEstado(Ticket.Estado.EN_PROCESO);
            t105.setTecnico("Marta");

            crearTicket("Cliente 6", Ticket.Prioridad.ALTA, "Pérdida de datos");
        }

        Ticket crearTicket(String cliente, Ticket.Prioridad prioridad, String descripcion) {
            Ticket nuevoTicket = new Ticket(contadorId.incrementAndGet(), cliente, prioridad, descripcion);
            listaTickets.add(nuevoTicket);
            return nuevoTicket;
        }

        void asignarTecnico(Ticket ticket, String nombreTecnico) {
            ticket.setTecnico(nombreTecnico);
            if (ticket.getEstado() == Ticket.Estado.PENDIENTE) {
                ticket.setEstado(Ticket.Estado.EN_PROCESO);
            }
        }

        void tomarTicket(Ticket ticket, String nombreTecnico) {
            ticket.setTecnico(nombreTecnico);
            ticket.setEstado(Ticket.Estado.EN_PROCESO);
        }

        void resolverTicket(Ticket ticket) {
            ticket.setEstado(Ticket.Estado.RESUELTO);
        }

        List<Ticket> obtenerTodos() {
            return new ArrayList<>(listaTickets);
        }

        List<String> obtenerTecnicos() {
            return new ArrayList<>(listaTecnicos);
        }

        List<Ticket> filtrar(Ticket.Estado estado, Ticket.Prioridad prioridad) {
            return listaTickets.stream()
                    .filter(t -> estado == null || t.getEstado() == estado)
                    .filter(t -> prioridad == null || t.getPrioridad() == prioridad)
                    .collect(Collectors.toList());
        }

        int contarPendientes() {
            return (int) listaTickets.stream().filter(t -> t.getEstado() == Ticket.Estado.PENDIENTE).count();
        }

        int contarEnProceso() {
            return (int) listaTickets.stream().filter(t -> t.getEstado() == Ticket.Estado.EN_PROCESO).count();
        }

        int contarResueltos() {
            return (int) listaTickets.stream().filter(t -> t.getEstado() == Ticket.Estado.RESUELTO).count();
        }

        int contarTecnicosLibres() {
            long ocupados = listaTickets.stream()
                    .filter(t -> t.getEstado() == Ticket.Estado.EN_PROCESO)
                    .map(Ticket::getTecnico).filter(n -> !n.equals("—")).distinct().count();
            return Math.max(0, listaTecnicos.size() - (int) ocupados);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MODELO DE TABLA
    // ══════════════════════════════════════════════════════════════
    public static class ModeloTablaTickets extends AbstractTableModel {

        private static final String[] COLUMNAS = {"ID", "Cliente", "Prioridad", "Estado", "Técnico"};
        private List<Ticket> listaTickets;

        ModeloTablaTickets() {
            listaTickets = new ArrayList<>();
        }

        void refrescar(List<Ticket> nuevaLista) {
            listaTickets = new ArrayList<>(nuevaLista);
            fireTableDataChanged();
        }

        Ticket obtenerTicket(int fila) {
            return listaTickets.get(fila);
        }

        @Override
        public int getRowCount() {
            return listaTickets.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int col) {
            return COLUMNAS[col];
        }

        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            Ticket ticket = listaTickets.get(fila);
            return switch (columna) {
                case 0 ->
                    ticket.getIdentificador();
                case 1 ->
                    ticket.getCliente();
                case 2 ->
                    ticket.getPrioridad().name();
                case 3 ->
                    ticket.getEstado().name();
                case 4 ->
                    ticket.getTecnico();
                default ->
                    "";
            };
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PANEL: CREAR TICKET
    // ══════════════════════════════════════════════════════════════
    static class PanelCrearTicket extends JPanel {

        private final JTextField campoCliente;
        private final JComboBox<Ticket.Prioridad> comboPrioridad;
        private final JTextArea areaDescripcion;
        private final JButton botonCrear;
        private final JButton botonLimpiar;

        private final GestorDeTickets gestorDeTickets;
        private final Runnable callbackAlCrear;

        PanelCrearTicket(GestorDeTickets gestorDeTickets, Runnable callbackAlCrear) {
            this.gestorDeTickets = gestorDeTickets;
            this.callbackAlCrear = callbackAlCrear;

            campoCliente = new JTextField(20);
            comboPrioridad = new JComboBox<>(Ticket.Prioridad.values());
            areaDescripcion = new JTextArea(4, 20);
            areaDescripcion.setLineWrap(true);
            areaDescripcion.setWrapStyleWord(true);

            botonCrear = new JButton("Crear");
            botonLimpiar = new JButton("Limpiar");

            aplicarFuenteGlobal(campoCliente, comboPrioridad, areaDescripcion, botonCrear, botonLimpiar);

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
            JLabel etiquetaPrioridad = new JLabel("Prioridad:");
            JLabel etiquetaDescripcion = new JLabel("Descripción:");
            aplicarFuenteGlobal(etiquetaCliente, etiquetaPrioridad, etiquetaDescripcion);

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
            add(etiquetaPrioridad, gbc);
            gbc.gridy = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(comboPrioridad, gbc);

            gbc.gridy = 4;
            gbc.fill = GridBagConstraints.NONE;
            add(etiquetaDescripcion, gbc);
            gbc.gridy = 5;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            add(new JScrollPane(areaDescripcion), gbc);

            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panelBotones.add(botonCrear);
            panelBotones.add(botonLimpiar);

            gbc.gridy = 6;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
            add(panelBotones, gbc);
        }

        private void configurarEventos() {
            botonCrear.addActionListener(e -> {
                String cliente = campoCliente.getText().trim();
                String descripcion = areaDescripcion.getText().trim();

                if (cliente.isEmpty() || descripcion.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Los campos Cliente y Descripción son obligatorios.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                gestorDeTickets.crearTicket(cliente, (Ticket.Prioridad) comboPrioridad.getSelectedItem(), descripcion);
                JOptionPane.showMessageDialog(this, "Ticket creado para: " + cliente, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                callbackAlCrear.run();
            });

            botonLimpiar.addActionListener(e -> limpiarFormulario());
        }

        private void limpiarFormulario() {
            campoCliente.setText("");
            areaDescripcion.setText("");
            comboPrioridad.setSelectedIndex(0);
            campoCliente.requestFocus();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PANEL: ESTADO DEL SISTEMA
    // ══════════════════════════════════════════════════════════════
    static class PanelEstadoSistema extends JPanel {

        private final JLabel etiquetaPendientes;
        private final JLabel etiquetaEnProceso;
        private final JLabel etiquetaResueltos;
        private final JLabel etiquetaTecnicosLibres;
        private final GestorDeTickets gestorDeTickets;

        PanelEstadoSistema(GestorDeTickets gestorDeTickets) {
            this.gestorDeTickets = gestorDeTickets;
            etiquetaPendientes = new JLabel();
            etiquetaEnProceso = new JLabel();
            etiquetaResueltos = new JLabel();
            etiquetaTecnicosLibres = new JLabel();

            aplicarFuenteGlobal(etiquetaPendientes, etiquetaEnProceso, etiquetaResueltos, etiquetaTecnicosLibres);

            setBorder(new TitledBorder("Estado del sistema"));
            setPreferredSize(new Dimension(0, 60));
            setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

            add(etiquetaPendientes);
            add(etiquetaEnProceso);
            add(etiquetaResueltos);
            add(etiquetaTecnicosLibres);

            actualizar();
        }

        void actualizar() {
            etiquetaPendientes.setText("Pendientes: " + gestorDeTickets.contarPendientes());
            etiquetaEnProceso.setText("En proceso: " + gestorDeTickets.contarEnProceso());
            etiquetaResueltos.setText("Resueltos: " + gestorDeTickets.contarResueltos());
            etiquetaTecnicosLibres.setText("Técnicos libres: " + gestorDeTickets.contarTecnicosLibres());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PANEL: TABLA DE TICKETS
    // ══════════════════════════════════════════════════════════════
    static class PanelTablaTickets extends JPanel {

        private final JTable tablaTickets;
        private final ModeloTablaTickets modeloTabla;

        PanelTablaTickets(GestorDeTickets gestorDeTickets) {
            modeloTabla = new ModeloTablaTickets();
            tablaTickets = new JTable(modeloTabla);

            tablaTickets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tablaTickets.setRowHeight(24);
            tablaTickets.setFont(FUENTE_GLOBAL);
            tablaTickets.getTableHeader().setFont(FUENTE_GLOBAL);
            tablaTickets.setGridColor(new Color(210, 210, 210));
            tablaTickets.setDefaultRenderer(Object.class, new RenderizadorFuenteUniforme());

            tablaTickets.getColumnModel().getColumn(0).setPreferredWidth(45);
            tablaTickets.getColumnModel().getColumn(1).setPreferredWidth(100);
            tablaTickets.getColumnModel().getColumn(2).setPreferredWidth(80);
            tablaTickets.getColumnModel().getColumn(3).setPreferredWidth(100);
            tablaTickets.getColumnModel().getColumn(4).setPreferredWidth(80);

            setBorder(new TitledBorder("Tickets"));
            setLayout(new BorderLayout());
            add(new JScrollPane(tablaTickets), BorderLayout.CENTER);

            actualizarTabla(gestorDeTickets.obtenerTodos());
        }

        void actualizarTabla(List<Ticket> listaTickets) {
            modeloTabla.refrescar(listaTickets);
        }

        Ticket obtenerTicketSeleccionado() {
            int fila = tablaTickets.getSelectedRow();
            return fila == -1 ? null : modeloTabla.obtenerTicket(fila);
        }

        private static class RenderizadorFuenteUniforme extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor,
                    boolean estaSeleccionado, boolean tieneFoco, int fila, int columna) {
                Component celda = super.getTableCellRendererComponent(tabla, valor, estaSeleccionado, tieneFoco, fila, columna);
                celda.setFont(FUENTE_GLOBAL);
                return celda;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PANEL: ACCIONES Y FILTROS
    // ══════════════════════════════════════════════════════════════
    static class PanelAcciones extends JPanel {

        private final JButton botonActualizar;
        private final JButton botonAsignar;
        private final JButton botonTomar;
        private final JButton botonResolver;
        private final JButton botonVerDetalle;

        private final JComboBox<String> comboFiltroEstado;
        private final JComboBox<String> comboFiltroPrioridad;

        private final GestorDeTickets gestorDeTickets;
        private final PanelTablaTickets panelTablaTickets;
        private final PanelEstadoSistema panelEstadoSistema;

        PanelAcciones(GestorDeTickets gestorDeTickets, PanelTablaTickets panelTablaTickets, PanelEstadoSistema panelEstadoSistema) {
            this.gestorDeTickets = gestorDeTickets;
            this.panelTablaTickets = panelTablaTickets;
            this.panelEstadoSistema = panelEstadoSistema;

            botonActualizar = new JButton("Actualizar");
            botonAsignar = new JButton("Asignar");
            botonTomar = new JButton("Tomar");
            botonResolver = new JButton("Resolver");
            botonVerDetalle = new JButton("Ver detalle");

            comboFiltroEstado = new JComboBox<>(new String[]{"Todos", "PENDIENTE", "EN_PROCESO", "RESUELTO"});
            comboFiltroPrioridad = new JComboBox<>(new String[]{"Todas", "BAJA", "MEDIA", "ALTA"});

            aplicarFuenteGlobal(botonActualizar, botonAsignar, botonTomar, botonResolver, botonVerDetalle,
                    comboFiltroEstado, comboFiltroPrioridad);

            construirLayout();
            configurarEventos();
        }

        private void construirLayout() {
            setBorder(new TitledBorder("Acciones"));
            setLayout(new BorderLayout(0, 4));

            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            panelBotones.add(botonActualizar);
            panelBotones.add(botonAsignar);
            panelBotones.add(botonTomar);
            panelBotones.add(botonResolver);
            panelBotones.add(botonVerDetalle);

            JLabel etiquetaEstado = new JLabel("  Filtro — Estado:");
            JLabel etiquetaPrioridad = new JLabel("  Prioridad:");
            aplicarFuenteGlobal(etiquetaEstado, etiquetaPrioridad);

            JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            panelFiltros.add(etiquetaEstado);
            panelFiltros.add(comboFiltroEstado);
            panelFiltros.add(etiquetaPrioridad);
            panelFiltros.add(comboFiltroPrioridad);

            add(panelBotones, BorderLayout.CENTER);
            add(panelFiltros, BorderLayout.SOUTH);
        }

        private void configurarEventos() {
            botonActualizar.addActionListener(e -> refrescar());
            comboFiltroEstado.addActionListener(e -> refrescar());
            comboFiltroPrioridad.addActionListener(e -> refrescar());

            botonAsignar.addActionListener(e -> {
                Ticket ticket = obtenerSeleccionado();
                if (ticket == null) {
                    return;
                }
                List<String> tecnicos = gestorDeTickets.obtenerTecnicos();
                String[] arreglo = tecnicos.toArray(new String[0]);
                String elegido = (String) JOptionPane.showInputDialog(this,
                        "Seleccione técnico para ticket #" + ticket.getIdentificador(),
                        "Asignar Técnico", JOptionPane.QUESTION_MESSAGE, null, arreglo, arreglo[0]);
                if (elegido != null) {
                    gestorDeTickets.asignarTecnico(ticket, elegido);
                    refrescar();
                }
            });

            botonTomar.addActionListener(e -> {
                Ticket ticket = obtenerSeleccionado();
                if (ticket == null) {
                    return;
                }
                String nombre = JOptionPane.showInputDialog(this,
                        "Su nombre para tomar el ticket #" + ticket.getIdentificador() + ":");
                if (nombre != null && !nombre.trim().isEmpty()) {
                    gestorDeTickets.tomarTicket(ticket, nombre.trim());
                    refrescar();
                }
            });

            botonResolver.addActionListener(e -> {
                Ticket ticket = obtenerSeleccionado();
                if (ticket == null) {
                    return;
                }
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Marcar como RESUELTO el ticket #" + ticket.getIdentificador() + "?",
                        "Resolver", JOptionPane.YES_NO_OPTION);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    gestorDeTickets.resolverTicket(ticket);
                    refrescar();
                }
            });

            botonVerDetalle.addActionListener(e -> {
                Ticket ticket = obtenerSeleccionado();
                if (ticket == null) {
                    return;
                }
                String detalle = String.format(
                        "ID:          %d%nCliente:     %s%nPrioridad:   %s%nEstado:      %s%nTécnico:     %s%nDescripción: %s",
                        ticket.getIdentificador(), ticket.getCliente(), ticket.getPrioridad(),
                        ticket.getEstado(), ticket.getTecnico(), ticket.getDescripcion());
                JTextArea area = new JTextArea(detalle);
                area.setEditable(false);
                area.setFont(new Font("Monospaced", Font.PLAIN, 13));
                area.setBackground(UIManager.getColor("Panel.background"));
                JOptionPane.showMessageDialog(this, area,
                        "Detalle Ticket #" + ticket.getIdentificador(), JOptionPane.INFORMATION_MESSAGE);
            });
        }

        private void refrescar() {
            String estadoFiltro = (String) comboFiltroEstado.getSelectedItem();
            String prioridadFiltro = (String) comboFiltroPrioridad.getSelectedItem();
            Ticket.Estado estado = "Todos".equals(estadoFiltro) ? null : Ticket.Estado.valueOf(estadoFiltro);
            Ticket.Prioridad prioridad = "Todas".equals(prioridadFiltro) ? null : Ticket.Prioridad.valueOf(prioridadFiltro);
            panelTablaTickets.actualizarTabla(gestorDeTickets.filtrar(estado, prioridad));
            panelEstadoSistema.actualizar();
        }

        private Ticket obtenerSeleccionado() {
            Ticket ticket = panelTablaTickets.obtenerTicketSeleccionado();
            if (ticket == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un ticket de la tabla primero.",
                        "Sin selección", JOptionPane.WARNING_MESSAGE);
            }
            return ticket;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // VENTANA PRINCIPAL
    // ══════════════════════════════════════════════════════════════
    static class VentanaPrincipal extends JFrame {

        VentanaPrincipal() {
            GestorDeTickets gestorDeTickets = new GestorDeTickets();

            PanelEstadoSistema panelEstadoSistema = new PanelEstadoSistema(gestorDeTickets);
            PanelTablaTickets panelTablaTickets = new PanelTablaTickets(gestorDeTickets);
            PanelAcciones panelAcciones = new PanelAcciones(gestorDeTickets, panelTablaTickets, panelEstadoSistema);
            PanelCrearTicket panelCrearTicket = new PanelCrearTicket(gestorDeTickets, () -> {
                panelTablaTickets.actualizarTabla(gestorDeTickets.obtenerTodos());
                panelEstadoSistema.actualizar();
            });

            JPanel panelDerecho = new JPanel(new BorderLayout(0, 6));
            panelDerecho.add(panelEstadoSistema, BorderLayout.NORTH);
            panelDerecho.add(panelTablaTickets, BorderLayout.CENTER);
            panelDerecho.add(panelAcciones, BorderLayout.SOUTH);

            JPanel contenido = new JPanel(new BorderLayout(10, 0));
            contenido.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            contenido.add(panelCrearTicket, BorderLayout.WEST);
            contenido.add(panelDerecho, BorderLayout.CENTER);

            setTitle("HelpDesk Client (Swing) — Vista Principal");
            setContentPane(contenido);
            setMinimumSize(new Dimension(900, 580));
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setLocationRelativeTo(null);
            pack();

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    int respuesta = JOptionPane.showConfirmDialog(VentanaPrincipal.this,
                            "¿Desea salir?", "Confirmar salida", JOptionPane.YES_NO_OPTION);
                    if (respuesta == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                }
            });
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UTILIDAD: aplicar fuente global a componentes
    // ══════════════════════════════════════════════════════════════
    static void aplicarFuenteGlobal(JComponent... componentes) {
        for (JComponent componente : componentes) {
            componente.setFont(FUENTE_GLOBAL);
        }
    }
}
