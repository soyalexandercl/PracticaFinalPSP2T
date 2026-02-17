package com.practica.cliente;

import com.practica.util.Ticket;
import java.io.*;
import java.net.Socket;

public class Cliente {
    public void crearTicket(Ticket ticket) {
        try {
            Socket socket = new Socket("localhost", 1900);
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            salida.writeObject(ticket);
            System.out.println("[CLIENTE] Ticket enviado.");

            boolean terminado = false;
            while (!terminado) {
                Object respuesta = entrada.readObject();
                
                if (respuesta instanceof String) {
                    System.out.println("[SERVIDOR] " + respuesta);
                } else if (respuesta instanceof Ticket) {
                    Ticket ticketActualizado = (Ticket) respuesta;
                    System.out.println("[NOTIFICACIÓN] " + ticketActualizado);
                    
                    if (ticketActualizado.getEstado().equals("RESUELTO")) {
                        terminado = true;
                    }
                }
            }
            
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[INFO] Conexión finalizada por el servidor.");
        }
    }
}