package com.mycompany.practicafinalmtpa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class GestorPersistencia {
    private final String ARCHIVO_BD = "usuarios_persistentes.txt";

    public ArrayList<Usuario> cargarDatos() {
        ArrayList<Usuario> datos = new ArrayList<Usuario>();
        File archivo = new File(ARCHIVO_BD);
        
        if (!archivo.exists()) {
            return datos;
        }
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("\\|");
                if (partes.length == 2) {
                    datos.add(new Usuario(partes[0], partes[1]));
                }
            }
            br.close();
        } catch (Exception e) {
        }
        
        return datos;
    }

    public void guardarNuevoUsuario(Usuario u) {
        try {
            FileWriter fw = new FileWriter(ARCHIVO_BD, true);
            fw.write(u.getNombre() + "|" + u.getClave() + "\n");
            fw.close();
        } catch (Exception e) {
        }
    }
}