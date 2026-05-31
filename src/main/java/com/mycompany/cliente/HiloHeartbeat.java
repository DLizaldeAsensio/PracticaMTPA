package com.mycompany.cliente;

public class HiloHeartbeat extends Thread {
    private GestorConexion conexionRed;
    private String nombreUsuario;
    private boolean activo;

    public HiloHeartbeat(GestorConexion conexionRed, String nombreUsuario) {
        this.conexionRed = conexionRed;
        this.nombreUsuario = nombreUsuario;
        this.activo = true;
    }

    public void detener() {
        this.activo = false;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                Thread.sleep(120000); 
                
                if (activo) {
                    conexionRed.enviarMensaje("HEARTBEAT|" + nombreUsuario);
                }
            } catch (Exception e) {
                break; 
            }
        }
    }
}