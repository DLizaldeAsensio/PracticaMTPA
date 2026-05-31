package com.mycompany.practicafinalmtpa;

import java.util.ArrayList;

public class Salon {
    private String nombre;
    private ArrayList<ClienteDifusion> usuarios;
    private ArrayList<String> historialMensajes;

    public Salon(String nombre) {
        this.nombre = nombre;
        this.usuarios = new ArrayList<ClienteDifusion>();
        this.historialMensajes = new ArrayList<String>();
    }

    public String getNombre() {
        return nombre;
    }

    public synchronized int getNumeroUsuarios() {
        return usuarios.size();
    }

    public synchronized int getNumeroMensajes() {
        return historialMensajes.size();
    }

    public synchronized void agregarUsuario(ClienteDifusion cliente) {
        if (!usuarios.contains(cliente)) {
            usuarios.add(cliente);
        }
    }

    public synchronized void quitarUsuario(ClienteDifusion cliente) {
        usuarios.remove(cliente);
    }

    public synchronized boolean contieneUsuario(String nombreUsuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            ClienteDifusion c = usuarios.get(i);
            if (c.getNombreUsuario() != null && c.getNombreUsuario().equals(nombreUsuario)) {
                return true;
            }
        }
        return false;
    }

    public synchronized String getListaUsuarios() {
        String resultado = "";
        for (int i = 0; i < usuarios.size(); i++) {
            ClienteDifusion c = usuarios.get(i);
            if (c.getNombreUsuario() != null) {
                resultado = resultado + c.getNombreUsuario();
                if (i < usuarios.size() - 1) {
                    resultado = resultado + ",";
                }
            }
        }
        return resultado;
    }

    public synchronized void registrarMensaje(String mensajeFormateado) {
        historialMensajes.add(mensajeFormateado);
    }

    public synchronized void difundirMensaje(String mensajeFormateado) {
        for (int i = 0; i < usuarios.size(); i++) {
            usuarios.get(i).enviarMensaje(mensajeFormateado);
        }
    }

    public synchronized void enviarHistorialA(ClienteDifusion cliente) {
        for (int i = 0; i < historialMensajes.size(); i++) {
            cliente.enviarMensaje(historialMensajes.get(i));
        }
    }
}