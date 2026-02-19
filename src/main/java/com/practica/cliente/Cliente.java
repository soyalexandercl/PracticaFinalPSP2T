package com.practica.cliente;

import com.practica.util.Ticket;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class Cliente {

    public String generarPrioridad(String descripcion) {
        String[] prioridadAlta = {"urgente", "bloqueado", "emergencia"};
        String[] prioridadMedia = {"error", "falla", "fallo", "lento"};

        String texto = descripcion.toLowerCase();

        for (String palabra : prioridadAlta) {
            if (texto.contains(palabra)) {
                return "ALTA";
            }
        }

        for (String palabra : prioridadMedia) {
            if (texto.contains(palabra)) {
                return "MEDIA";
            }
        }

        return "BAJA";
    }

    public void registrarTicket(Ticket ticket, Consumer<Ticket> callbackActualizacion) {
        try {
            Socket socket = new Socket("localhost", 1900);
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            salida.writeObject(ticket);

            boolean terminado = false;
            while (!terminado) {
                Object respuesta = entrada.readObject();

                if (respuesta instanceof String) {
                    // Confirmación de registro — no se procesa en la UI
                } else if (respuesta instanceof Ticket) {
                    Ticket ticketActualizado = (Ticket) respuesta;
                    callbackActualizacion.accept(ticketActualizado);

                    if (ticketActualizado.getEstado().equals("RESUELTO")) {
                        terminado = true;
                    }
                }
            }

            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
