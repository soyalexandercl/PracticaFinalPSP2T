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
        System.out.println("Tenico iniciado");
        
        try {
            while (true) {
                Ticket ticket = servidor.tomarTicket(this.nombre);
                
                servidor.notificarCliente(ticket);
                
                System.out.println("[TÉCNICO " + nombre + "] Atendiendo Ticket #" + ticket.getId());
                Thread.sleep(10000);
                
                ticket.setEstado("RESUELTO");
                
                servidor.notificarCliente(ticket);
                
                System.out.println("[TÉCNICO " + nombre + "] Ticket #" + ticket.getId() + " RESUELTO.");
            }
        } catch (InterruptedException e) {
            System.err.println("Técnico interrumpido");
        }
    }
}