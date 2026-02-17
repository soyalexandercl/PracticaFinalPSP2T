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
        this.listaTickets = new LinkedList<>();
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

    public void registrarTicket(Ticket ticket) {
        lock.lock();
        try {
            this.cantidadTickets++;
            ticket.setId(this.cantidadTickets);
            listaTickets.add(ticket);

            System.out.println("Ticked creado con éxito");
        } finally {
            lock.unlock();
        }
    }
    
    public void registrarTecnicoSimulado(int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            Tecnico tecnico = new Tecnico("Tecnico-" + (this.listaTecnicos.size() + 1));
            this.listaTecnicos.add(tecnico);
            tecnico.start();
        }
    }
}
