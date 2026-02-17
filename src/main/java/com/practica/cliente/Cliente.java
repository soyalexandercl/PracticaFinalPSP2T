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
    
    public void crearTicket(Ticket ticket) {
        try {
            this.socket = new Socket("localhost", 1900);
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            salida.writeObject(ticket);
            System.out.println("Ticket enviado con éxito");
            System.out.println(entrada.readObject());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en la comunicación con el servidor: " + e.getMessage());
        }
    }
}