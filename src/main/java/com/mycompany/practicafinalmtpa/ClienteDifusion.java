package com.mycompany.practicafinalmtpa;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClienteDifusion extends Thread {
    private Socket cliente;
    private ServidorDifusion servidorCentral;
    private ProcesadorProtocolo procesador;
    private OutputStream os;
    private InputStream is;
    
    private String usuarioLogueado = null;
    private long tiempoUltimoHeartbeat;

    public ClienteDifusion(Socket sck, ServidorDifusion servidor) throws Exception {
        cliente = sck;
        servidorCentral = servidor;
        procesador = new ProcesadorProtocolo(servidor);
        tiempoUltimoHeartbeat = System.currentTimeMillis(); 
        
        os = cliente.getOutputStream();
        is = cliente.getInputStream();
    }

    public String getNombreUsuario() {
        return usuarioLogueado;
    }

    public void setNombreUsuario(String nombre) {
        this.usuarioLogueado = nombre;
    }

    public long getTiempoUltimoHeartbeat() {
        return tiempoUltimoHeartbeat;
    }

    public void actualizarHeartbeat() {
        this.tiempoUltimoHeartbeat = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            while (true) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int nb;
                
                do {
                    nb = is.read(buffer);
                    if (nb == -1) {
                        break; 
                    }
                    if (nb > 0) {
                        baos.write(buffer, 0, nb);
                    }
                } while (is.available() > 0);

                if (nb == -1) {
                    break; 
                }
                
                String mensajeRecibido = new String(baos.toByteArray()).trim();
                
                if (mensajeRecibido.length() > 0) {
                    System.out.println(mensajeRecibido);
                    procesador.procesarMensaje(mensajeRecibido, this);
                }
            }
        } catch (Exception e) {
        } finally {
            servidorCentral.gestionarDesconexion(this);
            cerrarSockets();
        }
    }

    public void enviarMensaje(String texto) {
        try {
            String textoFinal = texto + "\n";
            os.write(textoFinal.getBytes());
            os.flush();
            
            String destinatario = "Anon";
            if (usuarioLogueado != null) {
                destinatario = usuarioLogueado;
            }
            System.out.println("[Mensaje a " + destinatario + "]: " + texto);
        } catch (Exception e) {}
    }

    public void cerrarSockets() {
        try {
            cliente.close();
        } catch (Exception e) {}
    }
}