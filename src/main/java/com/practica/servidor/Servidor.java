package com.practica.servidor;

import com.practica.tecnico.Tecnico;
import com.practica.util.Ticket;

import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {

    private ServerSocket serverSocket;
    private final List<Tecnico> listaTecnicos = new ArrayList<>();
    private final List<Ticket> listaTickets = new ArrayList<>();
    private final Map<Integer, ObjectOutputStream> listaClientes = new HashMap<>();
    private int cantidadTickets = 0;

    // Contador que se incrementa cada vez que el estado del servidor cambia.
    // La UI lo consulta periódicamente para saber si necesita refrescarse,
    // sin necesidad de callbacks ni modificaciones complejas.
    private volatile int contadorCambios = 0;

    public Servidor() {
    }

    public void iniciarServidor() {
        try {
            this.serverSocket = new ServerSocket(1900);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> gestionarConexion(socket)).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void gestionarConexion(Socket socket) {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            Object peticion = entrada.readObject();
            if (peticion instanceof Ticket) {
                registrarTicket((Ticket) peticion, salida);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized void registrarTicket(Ticket ticket, ObjectOutputStream salida) {
        this.cantidadTickets++;
        ticket.setId(this.cantidadTickets);
        listaTickets.add(ticket);
        if (salida != null) {
            listaClientes.put(ticket.getId(), salida);
            try {
                salida.writeObject("Ticket registrado.");
                salida.flush();
            } catch (IOException e) {
                listaClientes.remove(ticket.getId());
            }
        }
        contadorCambios++;
        notifyAll();
    }

    public synchronized void registrarTicketDirecto(Ticket ticket) {
        registrarTicket(ticket, null);
    }

    public synchronized Ticket tomarTicket(String nombreTecnico) throws InterruptedException {
        String[] prioridades = {"ALTA", "MEDIA", "BAJA"};
        while (true) {
            Ticket ticket = null;
            for (int i = 0; i < prioridades.length && ticket == null; i++) {
                ticket = buscarTicket(prioridades[i]);
            }
            if (ticket != null) {
                ticket.setEstado("EN_PROCESO");
                ticket.setTecnicoAsignado(nombreTecnico);
                contadorCambios++;
                return ticket;
            }
            wait();
        }
    }

    private Ticket buscarTicket(String prioridad) {
        for (Ticket ticket : listaTickets) {
            if (ticket.getEstado().equals("PENDIENTE") && ticket.getPrioridad().equalsIgnoreCase(prioridad)) {
                return ticket;
            }
        }
        return null;
    }

    public void notificarCliente(Ticket ticket) {
        ObjectOutputStream salida = listaClientes.get(ticket.getId());
        if (salida != null) {
            try {
                salida.reset();
                salida.writeObject(ticket);
                salida.flush();
                if (ticket.getEstado().equals("RESUELTO")) {
                    listaClientes.remove(ticket.getId());
                }
            } catch (IOException e) {
                listaClientes.remove(ticket.getId());
            }
        }
        contadorCambios++;
    }

    public void registrarTecnicoSimulado(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            String nombreTecnico = "Tecnico-" + (this.listaTecnicos.size() + 1);
            Tecnico tecnico = new Tecnico(nombreTecnico, this);
            this.listaTecnicos.add(tecnico);
            tecnico.start();
        }
        contadorCambios++;
    }

    public synchronized void asignarTecnicoManual(Ticket ticket, String nombreTecnico) {
        ticket.setEstado("EN_PROCESO");
        ticket.setTecnicoAsignado(nombreTecnico);
        contadorCambios++;
        notifyAll();
    }

    public synchronized void resolverTicketManual(Ticket ticket) {
        ticket.setEstado("RESUELTO");
        notificarCliente(ticket);
        notifyAll();
    }

    // Permite a la UI saber si algo cambió desde la última vez que consultó
    public int getContadorCambios() {
        return contadorCambios;
    }

    public synchronized List<Ticket> getListaTickets() {
        return Collections.unmodifiableList(listaTickets);
    }

    public List<Tecnico> getListaTecnicos() {
        return Collections.unmodifiableList(listaTecnicos);
    }
}