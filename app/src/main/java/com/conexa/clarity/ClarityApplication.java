package com.conexa.clarity;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.conexa.clarity.data.local.PreferenciaUsuarioDao;
import com.conexa.clarity.util.SesionUsuario;

public class ClarityApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        int idUsuario = SesionUsuario.obtenerId(this);

        String tema = "CLARO";

        if (idUsuario != SesionUsuario.SIN_SESION) {

            PreferenciaUsuarioDao dao = new PreferenciaUsuarioDao(this);

            tema = dao.obtenerTema(idUsuario);
        }

        if ("OSCURO".equals(tema)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
