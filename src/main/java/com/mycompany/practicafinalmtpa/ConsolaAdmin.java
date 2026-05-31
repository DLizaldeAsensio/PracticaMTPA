package com.mycompany.practicafinalmtpa;

import java.util.Scanner;

public class ConsolaAdmin {
    private ServidorDifusion servidor;

    public ConsolaAdmin(ServidorDifusion servidor) {
        this.servidor = servidor;
    }

    public void iniciar() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Consola Del Servidor\n");
        System.out.println("Ordenes: Estado, Rechazar, Mantenimiento, Salir\n");
        while (true) {
            String comando = sc.nextLine();
            
            if (comando.equalsIgnoreCase("RECHAZAR")) {
                servidor.cambiarEstadoAdmision();
            } else if (comando.equalsIgnoreCase("MANTENIMIENTO")) {
                servidor.cambiarEstadoMantenimiento();
            } else if (comando.equalsIgnoreCase("ESTADO")) {
                servidor.imprimirEstado();
            } else if (comando.equalsIgnoreCase("SALIR")){
                System.exit(0);
            } else {
                System.out.println("Comando desconocido.");
            }
        }
    }
}