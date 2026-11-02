package com.conexa.clarity.ui.logros;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.R;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;


public class LogrosyProgreso extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private MaterialButton botonDia;
    private MaterialButton botonSemana;
    private MaterialButton botonMes;
    private MaterialButton botonAnio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progresoylogros);

        bottomNav = findViewById(R.id.bottomNavigationView);
        botonDia = findViewById(R.id.botonDia);
        botonMes = findViewById(R.id.botonMes);
        botonSemana = findViewById(R.id.botonSemana);
        botonAnio = findViewById(R.id.botonAnio);

        botonDia.setOnClickListener(v -> actualizarBotonSeleccionado(botonDia));
        botonSemana.setOnClickListener(v -> actualizarBotonSeleccionado(botonSemana));
        botonMes.setOnClickListener(v -> actualizarBotonSeleccionado(botonMes));
        botonAnio.setOnClickListener(v -> actualizarBotonSeleccionado(botonAnio));

        actualizarBotonSeleccionado(botonDia);
        configurarNavegacion();
    }
    private void actualizarBotonSeleccionado(MaterialButton seleccionado){
        MaterialButton[] botones = {
                botonDia, botonSemana, botonMes, botonAnio
        };
        for (MaterialButton boton: botones){
            if(boton == seleccionado){
                boton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                            getColor(R.color.verde_principal)
                        )
                );
                boton.setTextColor(getColor(R.color.blanco_superficie));
            }
            else {
                boton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                getColor(R.color.verde_secundario)
                        )
                );
                boton.setTextColor(getColor(R.color.gris_texto));
            }
        }
    }
    private void configurarNavegacion() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_timer);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_recursos) {
                startActivity(new Intent(this, RecursosActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_timer) {
                return true;
            }

            return false;
        });
    }


}
