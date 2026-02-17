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

    private ServerSocket servidor;
    private Socket conexion;
    private DataOutputStream salida;
    private DataInputStream entrada;

    private Lock lock;
    private Queue<Ticket> colaTickets;
    private int cantidadTickets;

    public Servidor() throws IOException {
        this.servidor = new ServerSocket(1900);
        this.lock = new ReentrantLock();
        this.colaTickets = new LinkedList<>();
        this.cantidadTickets = 0;
        
        this.iniciarServidor();
    }

    public void iniciarServidor() throws IOException {
        System.out.println("Servidor iniciado");

        while (true) {
            Socket conexion = servidor.accept();
            System.out.println("Cliente conectado: " + conexion.getInetAddress());
        }
    }

    public void registrarTicker(Ticket ticket) {
        lock.lock();
        try {
            this.cantidadTickets++;
            ticket.setId(this.cantidadTickets);
            colaTickets.add(ticket);
        } finally {
            lock.unlock();
        }
    }
}
