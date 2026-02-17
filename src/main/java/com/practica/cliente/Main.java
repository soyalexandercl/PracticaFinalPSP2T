package com.practica.cliente;

import com.practica.util.Ticket;

public class Main {

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        Ticket ticket = new Ticket("Jhon", "Problema de red", "ALTA");
        cliente.crearTicket(ticket);
    }
}
