package com.practica.servidor;

import com.practica.tecnico.Tecnico;
import com.practica.util.Ticket;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ServidorSwing — Interfaz gráfica del servidor HelpDesk.
 *
 * Clases internas por responsabilidad:
 *   - ModeloTablaTickets   → Modelo de datos para el JTable
 *   - PanelSimularCliente  → Formulario para inyectar tickets sin pasar por red
 *   - PanelEstadoSistema   → Estadísticas en tiempo real
 *   - PanelTablaTickets    → Tabla de tickets
 *   - PanelAcciones        → Botones y filtros
 *   - VentanaPrincipal     → Ensamblado general con refresco automático
 *
 * Usa directamente: com.practica.util.Ticket, com.practica.servidor.Servidor,
 * com.practica.tecnico.Tecnico (requiere getNombre(), ver Tecnico.java).
 */
public class ServidorSwing {

    public static final Font FUENTE_GLOBAL = new Font("SansSerif", Font.PLAIN, 13);

    public static final String ESTADO_TODOS      = "Todos";
    public static final String PRIORIDAD_TODAS   = "Todas";

    public static final String[] PRIORIDADES        = {"ALTA", "MEDIA", "BAJA"};
    public static final String[] OPCIONES_ESTADO    = {ESTADO_TODOS, "PENDIENTE", "EN_PROCESO", "RESUELTO"};
    public static final String[] OPCIONES_PRIORIDAD = {PRIORIDAD_TODAS, "ALTA", "MEDIA", "BAJA"};

    public static void main(String[] argumentos) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception excepcion) {
            System.err.println("No se pudo cargar el look & feel: " + excepcion.getMessage());
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    // ══════════════════════════════════════════════════════════════
    // MODELO DE TABLA  —  usa com.practica.util.Ticket directamente
    // ══════════════════════════════════════════════════════════════

    static class ModeloTablaTickets extends AbstractTableModel {

        private static final String[] COLUMNAS = {"ID", "Cliente", "Prioridad", "Estado", "Técnico"};

        private List<Ticket> listaTickets;

        public ModeloTablaTickets(List<Ticket> listaTickets) {
            this.listaTickets = listaTickets;
        }

        public void refrescar(List<Ticket> nuevaLista) {
            this.listaTickets = nuevaLista;
            fireTableDataChanged();
        }

        public Ticket obtenerTicket(int fila) {
            return listaTickets.get(fila);
        }

        @Override
        public int getRowCount() { return listaTickets.size(); }

        @Override
        public int getColumnCount() { return COLUMNAS.length; }

        @Override
        public String getColumnName(int columna) { return COLUMNAS[columna]; }

        @Override
        public boolean isCellEditable(int fila, int columna) { return false; }

        @Override
        public Object getValueAt(int fila, int columna) {
            Ticket ticket = listaTickets.get(fila);
            switch (columna) {
                case 0: return ticket.getId();
                case 1: return ticket.getNombreCliente();
                case 2: return ticket.getPrioridad();
                case 3: return ticket.getEstado();
                case 4: return ticket.getTecnicoAsignado().isEmpty() ? "—" : ticket.getTecnicoAsignado();
                default: return "";
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PANEL: SIMULAR CLIENTE
    // Crea un Ticket real y lo registra en el Servidor directamente
    // sin pasar por socket de red, usando registrarTicketDirecto()
    // ══════════════════════════════════════════════════════════════

    static class PanelSimularCliente extends JPanel {

        private JTextField campoCliente;
        private JComboBox<String> comboPrioridad;
        private JTextArea areaDescripcion;
        private JButton botonCrear;
        private JButton botonLimpiar;

        private Servidor servidor;
        private Runnable callbackAlCrear;

        public PanelSimularCliente(Servidor servidor, Runnable callbackAlCrear) {
            this.servidor        = servidor;
            this.callbackAlCrear = callbackAlCrear;

            campoCliente    = new JTextField(20);
            comboPrioridad  = new JComboBox<>(PRIORIDADES);
            areaDescripcion = new JTextArea(4, 20);
            areaDescripcion.setLineWrap(true);
            areaDescripcion.setWrapStyleWord(true);

            botonCrear   = new JButton("Crear");
            botonLimpiar = new JButton("Limpiar");

            aplicarFuenteGlobal(campoCliente, comboPrioridad, areaDescripcion, botonCrear, botonLimpiar);

            construirLayout();
            configurarEventos();
        }

        private void construirLayout() {
            setBorder(new TitledBorder("Simular Cliente"));
            setPreferredSize(new Dimension(260, 0));
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets  = new Insets(6, 8, 4, 8);
            gbc.anchor  = GridBagConstraints.WEST;
            gbc.weightx = 1.0;

            JLabel etiquetaCliente     = new JLabel("Cliente:");
            JLabel etiquetaPrioridad   = new JLabel("Prioridad:");
            JLabel etiquetaDescripcion = new JLabel("Descripción:");
            aplicarFuenteGlobal(etiquetaCliente, etiquetaPrioridad, etiquetaDescripcion);

            gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0;
            add(etiquetaCliente, gbc);
            gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            add(campoCliente, gbc);

            gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
            add(etiquetaPrioridad, gbc);
            gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
            add(comboPrioridad, gbc);

            gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
            add(etiquetaDescripcion, gbc);
            gbc.gridy = 5; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
            add(new JScrollPane(areaDescripcion), gbc);

            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panelBotones.add(botonCrear);
            panelBotones.add(botonLimpiar);

            gbc.gridy = 6; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
            add(panelBotones, gbc);
        }

        private void configurarEventos() {
            botonCrear.addActionListener(e -> {
                String nombreCliente = campoCliente.getText().trim();
                String descripcion   = areaDescripcion.getText().trim();

                if (nombreCliente.isEmpty() || descripcion.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Los campos Cliente y Descripción son obligatorios.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Ticket(String nombreCliente, String descripcion, String prioridad)
                String prioridad   = (String) comboPrioridad.getSelectedItem();
                Ticket nuevoTicket = new Ticket(nombreCliente, descripcion, prioridad);

                // registrarTicketDirecto existe en Servidor.java
                servidor.registrarTicketDirecto(nuevoTicket);

                JOptionPane.showMessageDialog(this,
                    "Ticket simulado creado para: " + nombreCliente,
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

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
    // Calcula contadores filtrando sobre servidor.getListaTickets()
    // ══════════════════════════════════════════════════════════════

    static class PanelEstadoSistema extends JPanel {

        private JLabel etiquetaPendientes;
        private JLabel etiquetaEnProceso;
        private JLabel etiquetaResueltos;
        private JLabel etiquetaTecnicos;

        private Servidor servidor;

        public PanelEstadoSistema(Servidor servidor) {
            this.servidor      = servidor;
            etiquetaPendientes = new JLabel();
            etiquetaEnProceso  = new JLabel();
            etiquetaResueltos  = new JLabel();
            etiquetaTecnicos   = new JLabel();

            aplicarFuenteGlobal(etiquetaPendientes, etiquetaEnProceso, etiquetaResueltos, etiquetaTecnicos);

            setBorder(new TitledBorder("Estado del sistema"));
            setPreferredSize(new Dimension(0, 60));
            setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

            add(etiquetaPendientes);
            add(etiquetaEnProceso);
            add(etiquetaResueltos);
            add(etiquetaTecnicos);

            actualizar();
        }

        public void actualizar() {
            List<Ticket> listaTickets = servidor.getListaTickets();

            long pendientes = listaTickets.stream()
                .filter(t -> "PENDIENTE".equals(t.getEstado())).count();
            long enProceso = listaTickets.stream()
                .filter(t -> "EN_PROCESO".equals(t.getEstado())).count();
            long resueltos = listaTickets.stream()
                .filter(t -> "RESUELTO".equals(t.getEstado())).count();
            int tecnicosActivos = servidor.getListaTecnicos().size();

            etiquetaPendientes.setText("Pendientes: "       + pendientes);
            etiquetaEnProceso.setText("En proceso: "        + enProceso);
            etiquetaResueltos.setText("Resueltos: "         + resueltos);
            etiquetaTecnicos.setText("Técnicos activos: "   + tecnicosActivos);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PANEL: TABLA DE TICKETS
    // ══════════════════════════════════════════════════════════════

    static class PanelTablaTickets extends JPanel {

        private JTable tablaTickets;
        private ModeloTablaTickets modeloTabla;

        public PanelTablaTickets(Servidor servidor) {
            // Arranca con la lista actual del servidor
            modeloTabla  = new ModeloTablaTickets(servidor.getListaTickets());
            tablaTickets = new JTable(modeloTabla);

            tablaTickets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tablaTickets.setRowHeight(24);
            tablaTickets.setFont(FUENTE_GLOBAL);
            tablaTickets.getTableHeader().setFont(FUENTE_GLOBAL);
            tablaTickets.setGridColor(new Color(210, 210, 210));
            tablaTickets.setDefaultRenderer(Object.class, new RenderizadorFuenteUniforme());

            tablaTickets.getColumnModel().getColumn(0).setPreferredWidth(40);
            tablaTickets.getColumnModel().getColumn(1).setPreferredWidth(110);
            tablaTickets.getColumnModel().getColumn(2).setPreferredWidth(70);
            tablaTickets.getColumnModel().getColumn(3).setPreferredWidth(100);
            tablaTickets.getColumnModel().getColumn(4).setPreferredWidth(100);

            setBorder(new TitledBorder("Tickets"));
            setLayout(new BorderLayout());
            add(new JScrollPane(tablaTickets), BorderLayout.CENTER);
        }

        public void actualizarTabla(List<Ticket> listaTickets) {
            // Guardamos la fila seleccionada antes de refrescar
            // para que el Timer no borre la selección del usuario
            int filaSeleccionada = tablaTickets.getSelectedRow();

            modeloTabla.refrescar(listaTickets);

            // Restauramos la selección si la fila sigue existiendo
            if (filaSeleccionada >= 0 && filaSeleccionada < tablaTickets.getRowCount()) {
                tablaTickets.setRowSelectionInterval(filaSeleccionada, filaSeleccionada);
            }
        }

        public Ticket obtenerTicketSeleccionado() {
            int fila = tablaTickets.getSelectedRow();
            return fila == -1 ? null : modeloTabla.obtenerTicket(fila);
        }

        private static class RenderizadorFuenteUniforme extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor,
                    boolean estaSeleccionado, boolean tieneFoco, int fila, int columna) {
                Component celda = super.getTableCellRendererComponent(
                    tabla, valor, estaSeleccionado, tieneFoco, fila, columna);
                celda.setFont(FUENTE_GLOBAL);
                return celda;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PANEL: ACCIONES Y FILTROS
    // ══════════════════════════════════════════════════════════════

    static class PanelAcciones extends JPanel {

        private JButton botonActualizar;
        private JButton botonAsignar;
        private JButton botonSimularTecnico;
        private JButton botonResolver;
        private JButton botonVerDetalle;

        private JComboBox<String> comboFiltroEstado;
        private JComboBox<String> comboFiltroPrioridad;

        private Servidor servidor;
        private PanelTablaTickets panelTablaTickets;
        private PanelEstadoSistema panelEstadoSistema;

        public PanelAcciones(Servidor servidor, PanelTablaTickets panelTablaTickets,
                             PanelEstadoSistema panelEstadoSistema) {
            this.servidor           = servidor;
            this.panelTablaTickets  = panelTablaTickets;
            this.panelEstadoSistema = panelEstadoSistema;

            botonActualizar     = new JButton("Actualizar");
            botonAsignar        = new JButton("Asignar");
            botonSimularTecnico = new JButton("Simular Técnico");
            botonResolver       = new JButton("Resolver");
            botonVerDetalle     = new JButton("Ver detalle");

            comboFiltroEstado    = new JComboBox<>(OPCIONES_ESTADO);
            comboFiltroPrioridad = new JComboBox<>(OPCIONES_PRIORIDAD);

            aplicarFuenteGlobal(botonActualizar, botonAsignar, botonSimularTecnico,
                                botonResolver, botonVerDetalle,
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
            panelBotones.add(botonSimularTecnico);
            panelBotones.add(botonResolver);
            panelBotones.add(botonVerDetalle);

            JLabel etiquetaEstado    = new JLabel("  Filtro — Estado:");
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

            // Crea un nuevo Tecnico con hilo propio y lo agrega al servidor
            // Llama a registrarTecnicoSimulado(int) que ya existe en Servidor.java
            botonSimularTecnico.addActionListener(e -> {
                servidor.registrarTecnicoSimulado(1);
                refrescar();
                int totalTecnicos = servidor.getListaTecnicos().size();
                JOptionPane.showMessageDialog(this,
                    "Nuevo técnico creado: Tecnico-" + totalTecnicos
                    + "\nTotal técnicos activos: " + totalTecnicos,
                    "Técnico Simulado", JOptionPane.INFORMATION_MESSAGE);
            });

            // Llama a asignarTecnicoManual(Ticket, String) que existe en Servidor.java
            botonAsignar.addActionListener(e -> {
                Ticket ticket = obtenerSeleccionado();
                if (ticket == null) return;

                if (!"PENDIENTE".equals(ticket.getEstado())) {
                    JOptionPane.showMessageDialog(this,
                        "Solo se pueden asignar tickets en estado PENDIENTE.",
                        "No permitido", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                List<Tecnico> listaTecnicos = servidor.getListaTecnicos();
                if (listaTecnicos.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "No hay técnicos disponibles.\nUse 'Simular Técnico' para crear uno.",
                        "Sin técnicos", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // getNombre() está en Tecnico.java (ver archivo adjunto)
                String[] nombresTecnicos = listaTecnicos.stream()
                    .map(Tecnico::getNombre)
                    .toArray(String[]::new);

                String tecnicoElegido = (String) JOptionPane.showInputDialog(this,
                    "Seleccione técnico para el ticket #" + ticket.getId(),
                    "Asignar Técnico", JOptionPane.QUESTION_MESSAGE,
                    null, nombresTecnicos, nombresTecnicos[0]);

                if (tecnicoElegido != null) {
                    servidor.asignarTecnicoManual(ticket, tecnicoElegido);
                    refrescar();
                }
            });

            // Llama a resolverTicketManual(Ticket) que existe en Servidor.java
            botonResolver.addActionListener(e -> {
                Ticket ticket = obtenerSeleccionado();
                if (ticket == null) return;

                if ("RESUELTO".equals(ticket.getEstado())) {
                    JOptionPane.showMessageDialog(this,
                        "El ticket #" + ticket.getId() + " ya está resuelto.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Marcar como RESUELTO el ticket #" + ticket.getId() + "?",
                    "Resolver Ticket", JOptionPane.YES_NO_OPTION);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    servidor.resolverTicketManual(ticket);
                    refrescar();
                }
            });

            botonVerDetalle.addActionListener(e -> {
                Ticket ticket = obtenerSeleccionado();
                if (ticket == null) return;

                // Usa los getters reales de com.practica.util.Ticket
                String tecnicoMostrado = ticket.getTecnicoAsignado().isEmpty()
                    ? "—" : ticket.getTecnicoAsignado();

                String detalle = String.format(
                    "ID:          %d%n" +
                    "Cliente:     %s%n" +
                    "Prioridad:   %s%n" +
                    "Estado:      %s%n" +
                    "Técnico:     %s%n" +
                    "Descripción: %s",
                    ticket.getId(),
                    ticket.getNombreCliente(),
                    ticket.getPrioridad(),
                    ticket.getEstado(),
                    tecnicoMostrado,
                    ticket.getDescripcion());

                JTextArea areaDetalle = new JTextArea(detalle);
                areaDetalle.setEditable(false);
                areaDetalle.setFont(new Font("Monospaced", Font.PLAIN, 13));
                areaDetalle.setBackground(UIManager.getColor("Panel.background"));

                JOptionPane.showMessageDialog(this, areaDetalle,
                    "Detalle Ticket #" + ticket.getId(), JOptionPane.INFORMATION_MESSAGE);
            });
        }

        public void refrescar() {
            String estadoFiltro    = (String) comboFiltroEstado.getSelectedItem();
            String prioridadFiltro = (String) comboFiltroPrioridad.getSelectedItem();

            // Parte de la lista completa del servidor
            List<Ticket> listaFiltrada = servidor.getListaTickets();

            if (!ESTADO_TODOS.equals(estadoFiltro)) {
                String estadoFinal = estadoFiltro;
                listaFiltrada = listaFiltrada.stream()
                    .filter(t -> estadoFinal.equals(t.getEstado()))
                    .collect(Collectors.toList());
            }

            if (!PRIORIDAD_TODAS.equals(prioridadFiltro)) {
                String prioridadFinal = prioridadFiltro;
                listaFiltrada = listaFiltrada.stream()
                    .filter(t -> prioridadFinal.equalsIgnoreCase(t.getPrioridad()))
                    .collect(Collectors.toList());
            }

            panelTablaTickets.actualizarTabla(listaFiltrada);
            panelEstadoSistema.actualizar();
        }

        private Ticket obtenerSeleccionado() {
            Ticket ticket = panelTablaTickets.obtenerTicketSeleccionado();
            if (ticket == null) {
                JOptionPane.showMessageDialog(this,
                    "Seleccione un ticket de la tabla primero.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            }
            return ticket;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // VENTANA PRINCIPAL
    // ══════════════════════════════════════════════════════════════

    static class VentanaPrincipal extends JFrame {

        public VentanaPrincipal() {
            // Servidor ya modificado: constructor vacío, no bloquea
            Servidor servidor = new Servidor();

            // iniciarServidor() bloquea el hilo → corre en hilo demonio para no congelar la UI
            Thread hiloServidor = new Thread(() -> servidor.iniciarServidor());
            hiloServidor.setDaemon(true);
            hiloServidor.start();

            PanelEstadoSistema  panelEstadoSistema  = new PanelEstadoSistema(servidor);
            PanelTablaTickets   panelTablaTickets   = new PanelTablaTickets(servidor);
            PanelAcciones       panelAcciones       = new PanelAcciones(servidor, panelTablaTickets, panelEstadoSistema);
            PanelSimularCliente panelSimularCliente = new PanelSimularCliente(
                servidor, () -> panelAcciones.refrescar());

            JPanel panelDerecho = new JPanel(new BorderLayout(0, 6));
            panelDerecho.add(panelEstadoSistema, BorderLayout.NORTH);
            panelDerecho.add(panelTablaTickets,  BorderLayout.CENTER);
            panelDerecho.add(panelAcciones,      BorderLayout.SOUTH);

            JPanel contenido = new JPanel(new BorderLayout(10, 0));
            contenido.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            contenido.add(panelSimularCliente, BorderLayout.WEST);
            contenido.add(panelDerecho,        BorderLayout.CENTER);

            setTitle("HelpDesk — Servidor (Vista Principal)");
            setContentPane(contenido);
            setMinimumSize(new Dimension(960, 600));
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setLocationRelativeTo(null);
            pack();

            // Observer: el Servidor llama a refrescar() solo cuando su estado cambia.
            // No hay polling; la UI se actualiza únicamente ante eventos reales.
            servidor.setCallbackCambio(() -> panelAcciones.refrescar());

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    int respuesta = JOptionPane.showConfirmDialog(VentanaPrincipal.this,
                        "¿Desea cerrar el servidor?", "Confirmar cierre", JOptionPane.YES_NO_OPTION);
                    if (respuesta == JOptionPane.YES_OPTION) System.exit(0);
                }
            });
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UTILIDAD: aplicar fuente global a componentes
    // ══════════════════════════════════════════════════════════════

    public static void aplicarFuenteGlobal(JComponent... componentes) {
        for (JComponent componente : componentes) {
            componente.setFont(FUENTE_GLOBAL);
        }
    }
}