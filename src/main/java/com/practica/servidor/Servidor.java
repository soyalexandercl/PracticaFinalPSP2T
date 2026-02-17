package com.practica.servidor;

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

public class Servidor {

    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    private Lock lock;
    private Queue<Ticket> colaTickets;
    private int cantidadTickets;

    public Servidor() throws IOException, ClassNotFoundException {
        this.lock = new ReentrantLock();
        this.colaTickets = new LinkedList<>();
        this.cantidadTickets = 0;
        
        this.iniciarServidor();
    }

    public void iniciarServidor() throws IOException, ClassNotFoundException {
        this.serverSocket = new ServerSocket(1900);
        System.out.println("Servidor iniciado");

        while (true) {
            Socket conexion = serverSocket.accept();
            System.out.println("Cliente conectado: " + conexion.getInetAddress());
            
            new Thread();
            
            entrada = new ObjectInputStream(conexion.getInputStream());
            Ticket ticket = (Ticket) entrada.readObject();
            
            this.registrarTicker(ticket);
        }
    }

    public void registrarTicker(Ticket ticket) {
        lock.lock();
        try {
            this.cantidadTickets++;
            ticket.setId(this.cantidadTickets);
            colaTickets.add(ticket);
            
            System.out.println("Ticked creado con éxito");
        } finally {
            lock.unlock();
        }
    }
}
