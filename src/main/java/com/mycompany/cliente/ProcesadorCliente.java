package com.mycompany.cliente;

public class ProcesadorCliente {

    public void procesarMensaje(String mensaje) {
        String[] campos = mensaje.split("\\|");
        if (campos.length == 0) return;

        String cabecera = campos[0]; 

        try {
            if (cabecera.equals("OK")) {
                procesarExito(campos);
            } else if (cabecera.equals("ERROR")) {
                procesarError(campos);
            } else if (cabecera.equals("NOTIFY")) {
                procesarNotificacion(campos);
            } else if (cabecera.equals("MSG_SALON")) {
                System.out.println("[" + campos[1] + "] " + campos[2] + " (" + campos[3] + "): " + campos[4]);
            } else if (cabecera.equals("PRIVATE_MSG")) {
                System.out.println("[Privado de " + campos[1] + "]: " + campos[3]);
            } else if (cabecera.equals("END_SALON_HISTORY")) {
                System.out.println("--- Fin del historial ---");
            } else {
                System.out.println("Mensaje no reconocido: " + mensaje);
            }
        } catch (Exception e) {
            System.out.println("Error procesando trama: " + mensaje);
        }
    }

    private void procesarExito(String[] campos) {
        String comandoOriginal = campos[1];
        
        switch (comandoOriginal) {
            case "LOGIN":
                System.out.println("¡Login correcto! Salones: " + campos[3]);
                break;
                
            case "REGISTER":
                System.out.println("¡Registro correcto! Tu clave es: " + campos[3]);
                break;
                
            case "JOIN_SALON":
                System.out.println("Entrando al salón: " + campos[2]);
                break;
                
            case "LIST_SALONS":
                System.out.println("Lista de salones recibida: " + campos[2]);
                break;
                
            case "LIST_USERS":
                System.out.println("Lista de usuarios global recibida: " + campos[2]);
                break;
                
            case "LIST_SALON_USERS":
                System.out.println("Lista de usuarios en el salón recibida: " + campos[2]);
                break;
                
            default:
                System.out.println("Operación exitosa: " + comandoOriginal);
                break;
        }
    }

    private void procesarError(String[] campos) {
        String codigoError = campos[1];
        String descripcion = campos[2];
        System.out.println("ERROR [" + codigoError + "]: " + descripcion);
    }

    private void procesarNotificacion(String[] campos) {
        String tipo = campos[1];
        if (tipo.equals("JOIN")) {
            System.out.println(">>> " + campos[3] + " ha entrado al salón " + campos[2]);
        } else if (tipo.equals("LEAVE")) {
            System.out.println("<<< " + campos[3] + " ha salido del salón " + campos[2]);
        } else if (tipo.equals("PRIVATE_REQUEST")) {
            System.out.println(">>> " + campos[2] + " quiere chatear en privado contigo.");
        }
    }
}