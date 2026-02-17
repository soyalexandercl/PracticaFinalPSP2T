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
            
            boolean finalizado = false;
            while (!finalizado) {
                Object respuesta = entrada.readObject();
                System.out.println("[NOTIFICACIÓN]: " + respuesta);

                Ticket ticketActualizado = (Ticket) respuesta;
                if (ticketActualizado.getEstado().equals("RESUELTO")) {
                    finalizado = true;
                }
            }
            
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en la comunicación con el servidor: " + e.getMessage());
        }
    }
}