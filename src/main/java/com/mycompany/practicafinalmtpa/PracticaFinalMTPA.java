package com.mycompany.practicafinalmtpa;

public class PracticaFinalMTPA {

    public static void main(String[] args) {
        try {
            ServidorDifusion servidor = new ServidorDifusion();
            ConsolaAdmin consola = new ConsolaAdmin(servidor);
            consola.iniciar();
        } catch (Exception e) {
        }
    }
}