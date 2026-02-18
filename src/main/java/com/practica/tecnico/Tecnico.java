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

    public String getNombre() {
        return nombre;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Ticket ticket = servidor.tomarTicket(this.nombre);
                servidor.notificarCliente(ticket);

                Thread.sleep(10000); // Tiempo de trabajo

                ticket.setEstado("RESUELTO");

                servidor.notificarCliente(ticket);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}