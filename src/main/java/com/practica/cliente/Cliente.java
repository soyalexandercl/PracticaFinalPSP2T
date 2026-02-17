package com.practica.cliente;

import com.practica.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Cliente {
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    
    public Cliente() throws IOException {
        this.socket = new Socket("localhost", 1900);
        this.entrada = new ObjectInputStream(socket.getInputStream());
        this.salida = new ObjectOutputStream(socket.getOutputStream());
    }
    
    public void crearTicket() {

    }
}
    