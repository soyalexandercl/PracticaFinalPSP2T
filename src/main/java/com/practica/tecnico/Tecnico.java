package com.practica.tecnico;

import com.practica.servidor.Servidor;
import com.practica.util.Ticket;

public class Tecnico extends Thread {

    private String nombre;
    private Servidor servidor;

    public Tecnico(String nombre, Servidor servidor) {
        this.nombre = nombre;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        System.out.println("Técnico iniciado");
        try {
            while (true) {
                // 1. Tomar el ticket (Ya viene como EN_PROCESO desde el servidor)
                Ticket ticket = servidor.tomarTicket(this.nombre);

                // 2. Notificar al cliente que hemos empezado
                servidor.notificarCliente(ticket);

                System.out.println("[TÉCNICO " + nombre + "] Atendiendo Ticket #" + ticket.getId());

                // 3. Simular el tiempo de trabajo
                Thread.sleep(50000);

                // 4. CORRECCIÓN: Cambiar a RESUELTO (antes tenías EN_PROCESO aquí)
                ticket.setEstado("RESUELTO");

                // 5. Notificar la resolución
                servidor.notificarCliente(ticket);

                System.out.println("[TÉCNICO " + nombre + "] Ticket #" + ticket.getId() + " RESUELTO.");
            }
        } catch (InterruptedException e) {
            System.err.println("Técnico interrumpido");
        }
    }
}
