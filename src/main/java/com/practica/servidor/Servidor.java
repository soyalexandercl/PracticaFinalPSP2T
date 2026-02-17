package com.practica.servidor;

import com.practica.tecnico.Tecnico;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.practica.util.Ticket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {

    private ServerSocket serverSocket;

    private Lock lock;
    private List<Tecnico> listaTecnicos;
    private Queue<Ticket> listaTickets;
    private int cantidadTickets;

    public Servidor() {
        this.lock = new ReentrantLock();
        this.listaTecnicos = new ArrayList<>();
        this.listaTickets = new LinkedList<>(); // PENDIENTE | EN_PROCESO | RESUELTO
        this.cantidadTickets = 0;

        this.iniciarServidor();
    }

    public void iniciarServidor() {
        try {
            this.serverSocket = new ServerSocket(1900);
            System.out.println("Servidor iniciado en puerto 1900");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + socket.getInetAddress());

                    new Thread(() -> this.gestionarConexion(socket)).start();
                } catch (IOException e) {
                    System.err.println("Error aceptando conexión: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor: " + e.getMessage());
        }
    }

    private void gestionarConexion(Socket socket) {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            Ticket ticket = (Ticket) entrada.readObject();
            this.registrarTicket(ticket);
            System.out.println("Nuevo ticket");

            salida.writeObject("Ticket registrado con ID: " + ticket.getId());

            salida.close();
            entrada.close();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        }
    }

    public synchronized void registrarTicket(Ticket ticket) {
        this.cantidadTickets++;
        ticket.setId(this.cantidadTickets);
        listaTickets.add(ticket);

        System.out.println("Ticket creado con éxito");
        System.out.println(ticket);
    }

    public synchronized Ticket tomarTicket(String nombreTecnico) {
        Ticket ticket = buscarTicket("ALTA");

        if (ticket == null) {
            ticket = buscarTicket("MEDIA");
        }

        if (ticket == null) {
            ticket = buscarTicket("BAJA");
        }

        if (ticket != null) {
            ticket.setEstado("EN_PROCESO");
            ticket.setTecnicoAsignado(nombreTecnico);
            return ticket;
        }

        return null;
    }

    private Ticket buscarTicket(String prioridad) {
        for (Ticket ticket : listaTickets) {
            if (!ticket.getEstado().equals("RESUELTO") && ticket.getPrioridad().equalsIgnoreCase(prioridad)) {
                return ticket;
            }
        }

        return null;
    }

    public void registrarTecnicoSimulado(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            String nombreTecnico = "Tecnico-" + (this.listaTecnicos.size() + 1);
            Tecnico tecnico = new Tecnico(nombreTecnico, this);
            this.listaTecnicos.add(tecnico);
            tecnico.start();
        }
    }
}
