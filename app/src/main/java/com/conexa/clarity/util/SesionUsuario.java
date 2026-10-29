package com.conexa.clarity.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SesionUsuario {

    public static final int SIN_SESION = -1;

    private static SharedPreferences libreta(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences("sesion", Context.MODE_PRIVATE);
    }

    // Anota qué usuario ingresó
    public static void guardar(Context context, int idUsuario) {
        libreta(context).edit().putInt("id_usuario", idUsuario).apply();
    }

    // Devuelve el id del usuario
    public static int obtenerId(Context context) {
        return libreta(context).getInt("id_usuario", SIN_SESION);
    }

    // Borra la sesión
    public static void cerrar(Context context) {
        libreta(context).edit().clear().apply();
    }
}