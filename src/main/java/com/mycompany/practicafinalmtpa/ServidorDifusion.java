package com.mycompany.practicafinalmtpa;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServidorDifusion extends Thread {
    private ServerSocket servidor;
    private GestorPersistencia gestorBD;
    private boolean aceptandoClientes = true;
    private boolean enMantenimiento = false;

    private ArrayList<ClienteDifusion> clientesConectados;
    private ArrayList<Salon> salones;
    private ArrayList<Usuario> baseDatosUsuarios;
    
    private int proximaClave = 1000;

    public ServidorDifusion() throws Exception {
        servidor = new ServerSocket(9000);
        gestorBD = new GestorPersistencia();
        
        clientesConectados = new ArrayList<ClienteDifusion>();
        salones = new ArrayList<Salon>();
        
        cargarPersistencia();
        
        salones.add(new Salon("Salon_IA"));
        salones.add(new Salon("Salon_Deportes"));
        salones.add(new Salon("Salon_Therian"));
        salones.add(new Salon("Salon_Manga"));
        salones.add(new Salon("Salon_UEMC"));

        MonitorInactividad monitor = new MonitorInactividad(this);
        monitor.start();
        
        this.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket sck = servidor.accept();
                
                if (!aceptandoClientes) {
                    OutputStream osTmp = sck.getOutputStream();
                    String error = "ERROR|E009|Servidor no acepta mas clientes en este momento\n";
                    osTmp.write(error.getBytes());
                    osTmp.flush();
                    sck.close();
                    continue;
                }
                
                ClienteDifusion unCliente = new ClienteDifusion(sck, this);
                unCliente.start();
            }
        } catch (Exception e) {
        }
    }

    private synchronized void cargarPersistencia() {
        baseDatosUsuarios = gestorBD.cargarDatos();
        for (int i = 0; i < baseDatosUsuarios.size(); i++) {
            int claveInt = Integer.parseInt(baseDatosUsuarios.get(i).getClave());
            if (claveInt >= proximaClave) {
                proximaClave = claveInt + 1;
            }
        }
    }

    public synchronized String registrarUsuario(String nombreUsuario) {
        for (int i = 0; i < baseDatosUsuarios.size(); i++) {
            if (baseDatosUsuarios.get(i).getNombre().equals(nombreUsuario)) {
                return null;
            }
        }
        String claveGenerada = String.valueOf(proximaClave);
        proximaClave = proximaClave + 1;
        
        Usuario nuevo = new Usuario(nombreUsuario, claveGenerada);
        baseDatosUsuarios.add(nuevo);
        gestorBD.guardarNuevoUsuario(nuevo);
        
        return claveGenerada;
    }

    public synchronized boolean validarLogin(String nombreUsuario, String clave) {
        for (int i = 0; i < baseDatosUsuarios.size(); i++) {
            Usuario u = baseDatosUsuarios.get(i);
            if (u.getNombre().equals(nombreUsuario) && u.getClave().equals(clave)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean existeUsuarioEnBD(String nombreUsuario) {
        for (int i = 0; i < baseDatosUsuarios.size(); i++) {
            if (baseDatosUsuarios.get(i).getNombre().equals(nombreUsuario)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void agregarClienteConectado(ClienteDifusion cliente) {
        clientesConectados.add(cliente);
    }

    public synchronized Salon getSalon(String nombreSalon) {
        for (int i = 0; i < salones.size(); i++) {
            Salon s = salones.get(i);
            if (s.getNombre().equals(nombreSalon)) {
                return s;
            }
        }
        return null;
    }

    public synchronized ClienteDifusion getClienteActivo(String nombreCliente) {
        for (int i = 0; i < clientesConectados.size(); i++) {
            ClienteDifusion c = clientesConectados.get(i);
            if (c.getNombreUsuario() != null && c.getNombreUsuario().equals(nombreCliente)) {
                return c;
            }
        }
        return null;
    }

    public synchronized String getListaNombresSalones() {
        String resultado = "";
        for (int i = 0; i < salones.size(); i++) {
            resultado = resultado + salones.get(i).getNombre();
            if (i < salones.size() - 1) {
                resultado = resultado + ",";
            }
        }
        return resultado;
    }

    public synchronized String getListaUsuariosConectados() {
        String resultado = "";
        for (int i = 0; i < clientesConectados.size(); i++) {
            ClienteDifusion c = clientesConectados.get(i);
            if (c.getNombreUsuario() != null) {
                resultado = resultado + c.getNombreUsuario();
                if (i < clientesConectados.size() - 1) {
                    resultado = resultado + ",";
                }
            }
        }
        return resultado;
    }

    public synchronized void gestionarDesconexion(ClienteDifusion cliente) {
        clientesConectados.remove(cliente);
        String nombreUsuario = cliente.getNombreUsuario();
        
        if (nombreUsuario != null) {
            String fechaExacta = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            for (int i = 0; i < salones.size(); i++) {
                Salon salon = salones.get(i);
                if (salon.contieneUsuario(nombreUsuario)) {
                    salon.quitarUsuario(cliente);
                    salon.difundirMensaje("NOTIFY|LEAVE|" + salon.getNombre() + "|" + nombreUsuario + "|" + fechaExacta);
                }
            }
        }
    }

    public synchronized void expulsarInactivos(long tiempoActual) {
        for (int i = clientesConectados.size() - 1; i >= 0; i--) {
            ClienteDifusion c = clientesConectados.get(i);
            if ((tiempoActual - c.getTiempoUltimoHeartbeat()) > 300000) {
                c.enviarMensaje("ERROR|E003|Sesión cerrada por inactividad");
                gestionarDesconexion(c);
                c.cerrarSockets();
            }
        }
    }

    public boolean isEnMantenimiento() {
        return enMantenimiento;
    }

    public void cambiarEstadoAdmision() {
        aceptandoClientes = !aceptandoClientes;
        System.out.println("Admisión = " + aceptandoClientes);
    }

    public void cambiarEstadoMantenimiento() {
        enMantenimiento = !enMantenimiento;
        System.out.println("Mantenimiento = " + enMantenimiento);
    }

    public synchronized void imprimirEstado() {
        System.out.println("\nEstado\n");
        System.out.println("1. Usuarios online: " + clientesConectados.size()+"\n");
        for (int i = 0; i < salones.size(); i++) {
            Salon s = salones.get(i);
            System.out.println("Salón: " + s.getNombre());
            System.out.println("     Usuarios: " + s.getNumeroUsuarios());
            System.out.println("     Mensajes: " + s.getNumeroMensajes());
        }
    }
}