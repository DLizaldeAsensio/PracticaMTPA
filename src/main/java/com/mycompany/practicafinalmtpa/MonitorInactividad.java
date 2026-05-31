package com.mycompany.practicafinalmtpa;

public class MonitorInactividad extends Thread {
    private ServidorDifusion servidor;

    public MonitorInactividad(ServidorDifusion servidor) {
        this.servidor = servidor;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(60000);
                servidor.expulsarInactivos(System.currentTimeMillis());
            } catch (Exception e) {
            }
        }
    }
}