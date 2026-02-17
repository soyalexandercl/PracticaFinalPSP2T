package com.practica.cliente;

import com.practica.util.Ticket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Cliente {
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    
    public Cliente() {
        
    }
    
    public void crearTicket(Ticket ticket) throws IOException {
        this.socket = new Socket("localhost", 1900);
        this.entrada = new ObjectInputStream(socket.getInputStream());
        this.salida = new ObjectOutputStream(socket.getOutputStream());
        
        salida.writeObject(ticket);
        
        System.out.println("Ticket enviado con éxito");
    }
}