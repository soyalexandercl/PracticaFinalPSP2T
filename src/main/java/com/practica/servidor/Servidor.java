package com.practica.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Servidor {
    private ServerSocket servidor;
    private Socket conexion;
    private DataOutputStream salida;
    private DataInputStream entrada;
    
    private Queue<String> colaTickets;
    
    public Servidor() throws IOException {
        this.servidor = new ServerSocket(1900);
        this.colaTickets = new LinkedList<>();
        
        this.iniciarServidor();
    }
    
    public void iniciarServidor() throws IOException {
        System.out.println("Servidor iniciado");
        
        while (true) {
            Socket conexion = servidor.accept();
            System.out.println("Cliente conectado: " + conexion.getInetAddress());
        }
    }
    
    public void crearTicket() {
        
    }
}
