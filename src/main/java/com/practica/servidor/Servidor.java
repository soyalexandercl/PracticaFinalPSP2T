package com.practica.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Servidor {

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;

    private Lock lock;
    private Queue<Ticket> colaTickets;
    private int cantidadTickets;

    public Servidor() throws IOException {
        this.serverSocket = new ServerSocket(1900);
        this.lock = new ReentrantLock();
        this.colaTickets = new LinkedList<>();
        this.cantidadTickets = 0;
        
        this.iniciarServidor();
    }

    public void iniciarServidor() throws IOException {
        System.out.println("Servidor iniciado");

        while (true) {
            Socket conexion = serverSocket.accept();
            System.out.println("Cliente conectado: " + conexion.getInetAddress());
        }
    }

    public void registrarTicker(Ticket ticket) {
        lock.lock();
        try {
            this.cantidadTickets++;
            ticket.setId(this.cantidadTickets);
            colaTickets.add(ticket);
            
            System.out.println("Ticked creado con exito");
        } finally {
            lock.unlock();
        }
    }
}
