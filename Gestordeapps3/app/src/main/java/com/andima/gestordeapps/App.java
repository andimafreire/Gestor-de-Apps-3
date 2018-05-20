package com.andima.gestordeapps;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by Andima on 14/03/2018.
 */

public class App implements Comparable<App>{

    private String paquete;
    private String nombre;
    private Drawable icono;
    private boolean instalada;

    public App(String pPaquete, String pNombre, Drawable pIcono, boolean pIntalada) {
        paquete = pPaquete;
        nombre = pNombre;
        icono = pIcono;
        instalada = pIntalada;
    }

    public String getNombre() {
        return nombre;
    }
    public String getPaquete() {
        return paquete;
    }
    public Drawable getIcono() {
        return icono;
    }
    public boolean isInstalada() {
        return instalada;
    }

    @Override
    //compareTo de la interfaz Comparable, utilizado para ordenar las apps por nombre
    public int compareTo(@NonNull App pApp) {
        return getNombre().compareToIgnoreCase(pApp.getNombre());
    }


}