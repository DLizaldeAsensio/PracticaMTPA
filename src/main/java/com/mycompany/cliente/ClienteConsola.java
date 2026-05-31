package com.mycompany.cliente;

import java.util.Scanner;

public class ClienteConsola {
    //IMPORTANTE
    //ESTA CLASE HABRA QUE SUSTITUIRLA CON LA INTERFAZ CREO. ESTO SOLO VALE DE PRUEBA HASTA QUE SE HAYA IMPLEMENTADO LA INTERFAZ.
    public static void main(String[] args) {
        System.out.println("=== CLIENTE POR CONSOLA (MODO PRUEBA) ===");

        ProcesadorCliente procesador = new ProcesadorCliente();
        GestorConexion gestorConexion = new GestorConexion(procesador);

        if (gestorConexion.conectar("127.0.0.1", 9000)) {
            System.out.println("Escribe comandos (Ej: REGISTER|Pepe o LOGIN|Pepe|1000). Escribe SALIR para apagar.");
            System.out.println("--------------------------------------------------------------------------");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String texto = scanner.nextLine();
                
                if (texto.equalsIgnoreCase("SALIR")) {
                    gestorConexion.desconectar();
                    System.out.println("Apagando cliente...");
                    break;
                }
                
                gestorConexion.enviarMensaje(texto);
            }
            scanner.close();
            
        } else {
            System.out.println("❌ ERROR: No se pudo conectar. ¿Seguro que el Servidor está encendido?");
        }
    }
}