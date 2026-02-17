package com.practica.tecnico;

import com.practica.servidor.Servidor;
import com.practica.util.Ticket;

public class Tecnico extends Thread {
    private final String nombre;
    private final Servidor servidor;

    public Tecnico(String nombre, Servidor servidor) {
        this.nombre = nombre;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Ticket ticket = servidor.tomarTicket(this.nombre);
                servidor.notificarCliente(ticket);
                System.out.println("[TÉCNICO] Ticked: #" + ticket.getId() + " tomado");
                
                Thread.sleep(5000); // Tiempo de trabajo
                
                ticket.setEstado("RESUELTO");
                System.out.println("[TÉCNICO] Ticked: #" + ticket.getId() + " resuelto");
                
                servidor.notificarCliente(ticket);
            }
        } catch (InterruptedException e) {
            System.err.println("[HILO] " + nombre + " detenido");
        }
    }
}