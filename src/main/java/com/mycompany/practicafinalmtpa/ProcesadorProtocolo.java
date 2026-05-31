package com.mycompany.practicafinalmtpa;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProcesadorProtocolo {
    private ServidorDifusion servidorCentral;

    public ProcesadorProtocolo(ServidorDifusion servidorCentral) {
        this.servidorCentral = servidorCentral;
    }

    private String obtenerFechaActual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public void procesarMensaje(String mensaje, ClienteDifusion cliente) {
        String[] campos = mensaje.split("\\|");
        if (campos.length == 0) {
            return;
        }
        
        String comando = campos[0].toUpperCase();
        String usuarioActual = cliente.getNombreUsuario();

        try {
            if (!comando.equals("REGISTER") && !comando.equals("LOGIN")) {
                if (usuarioActual == null) {
                    cliente.enviarMensaje("ERROR|E003|Usuario no autenticado");
                    return;
                }
                if (servidorCentral.isEnMantenimiento()) {
                    cliente.enviarMensaje("ERROR|E008|Servidor en mantenimiento");
                    return;
                }
            }

            switch (comando) {
                case "REGISTER":
                    String nuevoNombre = campos[1];
                    String claveAsignada = servidorCentral.registrarUsuario(nuevoNombre);
                    
                    if (claveAsignada != null) {
                        cliente.enviarMensaje("OK|REGISTER|" + nuevoNombre + "|" + claveAsignada);
                    } else {
                        cliente.enviarMensaje("ERROR|E001|Usuario ya existe");
                    }
                    break;

                case "LOGIN":
                    String usuarioIntento = campos[1];
                    String claveIntento = campos[2];
                    
                    if (servidorCentral.validarLogin(usuarioIntento, claveIntento)) {
                        cliente.setNombreUsuario(usuarioIntento);
                        servidorCentral.agregarClienteConectado(cliente);
                        String salonesDisponibles = servidorCentral.getListaNombresSalones();
                        cliente.enviarMensaje("OK|LOGIN|" + usuarioIntento + "|" + salonesDisponibles);
                    } else {
                        cliente.enviarMensaje("ERROR|E002|Credenciales incorrectas");
                    }
                    break;

                case "LOGOUT":
                    cliente.enviarMensaje("OK|LOGOUT|" + usuarioActual);
                    servidorCentral.gestionarDesconexion(cliente);
                    cliente.cerrarSockets();
                    break;

                case "LIST_SALONS":
                    String todosLosSalones = servidorCentral.getListaNombresSalones();
                    cliente.enviarMensaje("OK|LIST_SALONS|" + todosLosSalones);
                    break;

                case "JOIN_SALON":
                    String salonDestino = campos[2];
                    Salon s = servidorCentral.getSalon(salonDestino);
                    
                    if (s != null) {
                        s.agregarUsuario(cliente);
                        cliente.enviarMensaje("OK|JOIN_SALON|" + salonDestino + "|" + s.getNumeroMensajes());
                        s.enviarHistorialA(cliente);
                        cliente.enviarMensaje("END_SALON_HISTORY|" + salonDestino);
                        s.difundirMensaje("NOTIFY|JOIN|" + salonDestino + "|" + usuarioActual + "|" + obtenerFechaActual());
                    } else {
                        cliente.enviarMensaje("ERROR|E004|Salón no encontrado");
                    }
                    break;

                case "LEAVE_SALON":
                    String salonAbandono = campos[2];
                    Salon sl = servidorCentral.getSalon(salonAbandono);
                    
                    if (sl != null && sl.contieneUsuario(usuarioActual)) {
                        sl.quitarUsuario(cliente);
                        cliente.enviarMensaje("OK|LEAVE_SALON|" + salonAbandono);
                        sl.difundirMensaje("NOTIFY|LEAVE|" + salonAbandono + "|" + usuarioActual + "|" + obtenerFechaActual());
                    } else {
                        cliente.enviarMensaje("ERROR|E004|Salón no encontrado");
                    }
                    break;

                case "MSG_SALON":
                    String nombreSalonChat = campos[2];
                    String contenidoMensaje = campos[3];
                    
                    if (contenidoMensaje.length() > 190) {
                        cliente.enviarMensaje("ERROR|E005|Mensaje demasiado largo (>190 caracteres)");
                        return;
                    }
                    
                    Salon salonChat = servidorCentral.getSalon(nombreSalonChat);
                    if (salonChat != null && salonChat.contieneUsuario(usuarioActual)) {
                        String fecha = obtenerFechaActual();
                        cliente.enviarMensaje("OK|MSG_SALON|" + nombreSalonChat + "|" + fecha);
                        
                        String tramaDifusion = "MSG_SALON|" + nombreSalonChat + "|" + usuarioActual + "|" + fecha + "|" + contenidoMensaje;
                        salonChat.registrarMensaje(tramaDifusion);
                        salonChat.difundirMensaje(tramaDifusion);
                    } else {
                        cliente.enviarMensaje("ERROR|E004|Salón no encontrado");
                    }
                    break;

                case "HISTORY_SALON":
                    String histSalon = campos[2];
                    String fechaHist = campos[3];
                    Salon sh = servidorCentral.getSalon(histSalon);
                    if (sh != null) {
                        cliente.enviarMensaje("OK|HISTORY_SALON|" + histSalon + "|" + fechaHist + "|" + sh.getNumeroMensajes());
                        sh.enviarHistorialA(cliente);
                        cliente.enviarMensaje("END_SALON_HISTORY|" + histSalon);
                    } else {
                        cliente.enviarMensaje("ERROR|E004|Salón no encontrado");
                    }
                    break;

                case "LIST_USERS":
                    String usuariosOnline = servidorCentral.getListaUsuariosConectados();
                    cliente.enviarMensaje("OK|LIST_USERS|" + usuariosOnline);
                    break;

                case "LIST_SALON_USERS":
                    String salonObjetivo = campos[2];
                    Salon sUsuarios = servidorCentral.getSalon(salonObjetivo);
                    if (sUsuarios != null) {
                        String listaSalonUsuarios = sUsuarios.getListaUsuarios();
                        cliente.enviarMensaje("OK|LIST_SALON_USERS|" + salonObjetivo + "|" + listaSalonUsuarios);
                    } else {
                        cliente.enviarMensaje("ERROR|E004|Salón no encontrado");
                    }
                    break;

                case "PRIVATE_OPEN":
                    String destinatarioOpen = campos[2];
                    if (!servidorCentral.existeUsuarioEnBD(destinatarioOpen)) {
                        cliente.enviarMensaje("ERROR|E007|Usuario destino no existe");
                        return;
                    }
                    ClienteDifusion clienteDestOpen = servidorCentral.getClienteActivo(destinatarioOpen);
                    if (clienteDestOpen != null) {
                        cliente.enviarMensaje("OK|PRIVATE_OPEN|" + destinatarioOpen);
                        clienteDestOpen.enviarMensaje("NOTIFY|PRIVATE_REQUEST|" + usuarioActual);
                    } else {
                        cliente.enviarMensaje("ERROR|E006|Usuario destino no conectado");
                    }
                    break;

                case "PRIVATE_MSG":
                    String receptor = campos[2];
                    String contenidoPriv = campos[3];
                    ClienteDifusion destinoCli = servidorCentral.getClienteActivo(receptor);
                    
                    if (destinoCli != null) {
                        String fechaPrivado = obtenerFechaActual();
                        cliente.enviarMensaje("OK|PRIVATE_MSG|" + receptor + "|" + fechaPrivado);
                        destinoCli.enviarMensaje("PRIVATE_MSG|" + usuarioActual + "|" + fechaPrivado + "|" + contenidoPriv);
                    } else {
                        cliente.enviarMensaje("ERROR|E006|Usuario destino no conectado");
                    }
                    break;

                case "PRIVATE_CLOSE":
                    String destinatarioClose = campos[2];
                    ClienteDifusion clienteDestClose = servidorCentral.getClienteActivo(destinatarioClose);
                    cliente.enviarMensaje("OK|PRIVATE_CLOSE|" + destinatarioClose);
                    if (clienteDestClose != null) {
                        clienteDestClose.enviarMensaje("NOTIFY|PRIVATE_CLOSED|" + usuarioActual);
                    }
                    break;

                case "HEARTBEAT":
                    cliente.actualizarHeartbeat();
                    cliente.enviarMensaje("OK|HEARTBEAT|" + usuarioActual + "|" + obtenerFechaActual());
                    break;

                default:
                    cliente.enviarMensaje("ERROR|E010|Parámetros incorrectos");
            }
        } catch (Exception e) {
            cliente.enviarMensaje("ERROR|E010|Parámetros incorrectos");
        }
    }
}