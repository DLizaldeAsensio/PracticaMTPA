package com.mycompany.cliente;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class GestorConexion extends Thread {
    private Socket socket;
    private InputStream entrada;
    private OutputStream salida;
    private ProcesadorCliente procesador;
    private boolean conectado;

    public GestorConexion(ProcesadorCliente procesador) {
        this.procesador = procesador;
        this.conectado = false;
    }

    public boolean conectar(String ip, int puerto) {
        try {
            socket = new Socket(ip, puerto);
            entrada = socket.getInputStream();
            salida = socket.getOutputStream();
            conectado = true;
            this.start(); 
            return true;
        } catch (Exception e) {
            System.out.println("No se pudo conectar al servidor.");
            return false;
        }
    }

    public void enviarMensaje(String texto) {
        if (conectado && salida != null) {
            try {
                String textoFinal = texto + "\n"; 
                salida.write(textoFinal.getBytes());
                salida.flush();
            } catch (Exception e) {
                System.out.println("Error enviando mensaje.");
            }
        }
    }

    public void desconectar() {
        conectado = false;
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = null;
            int nb;
            
            while (true) {
                baos = new ByteArrayOutputStream();
                do {
                    nb = entrada.read(buffer);
                    if (nb == -1) break; 
                    baos.write(buffer, 0, nb);
                } while (entrada.available() > 0);
                
                if (nb == -1) break;

                String textoRecibido = new String(baos.toByteArray());
                String[] mensajes = textoRecibido.split("\n");
                for (int i = 0; i < mensajes.length; i++) {
                    String msj = mensajes[i].trim();
                    if (msj.length() > 0) {
                        procesador.procesarMensaje(msj);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
}