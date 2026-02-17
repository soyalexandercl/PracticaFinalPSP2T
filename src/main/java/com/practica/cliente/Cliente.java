package com.practica.cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Cliente {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    
    public Cliente() throws IOException {
        this.socket = new Socket("localhost", 1900);
        this.entrada = new DataInputStream(socket.getInputStream());
        this.salida = new DataOutputStream(socket.getOutputStream());
    }
}
    