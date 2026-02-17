package com.practica.util;

import java.io.Serializable;

public class Ticket implements Serializable {
    private int id;
    private String nombreCliente;
    private String descripcion;
    private String prioridad;
    private String estado;
    private String tecnicoAsignado;

    public Ticket(String nombreCliente, String descripcion, String prioridad) {
        this.nombreCliente = nombreCliente;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = "PENDIENTE";
        this.tecnicoAsignado = "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreCliente() { return nombreCliente; }
    public String getDescripcion() { return descripcion; }
    public String getPrioridad() { return prioridad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTecnicoAsignado() { return tecnicoAsignado; }
    public void setTecnicoAsignado(String tecnicoAsignado) { this.tecnicoAsignado = tecnicoAsignado; }

    @Override
    public String toString() {
        return "Ticket #" + id + " [" + prioridad + "] - Cliente: " + nombreCliente + " - Estado: " + estado + 
               (tecnicoAsignado.isEmpty() ? "" : " - Técnico: " + tecnicoAsignado);
    }
}