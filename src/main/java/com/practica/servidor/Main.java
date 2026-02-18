package com.practica.servidor;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServidorSwing.VentanaPrincipal().setVisible(true));
    }
}