package com.conexa.clarity.ui.timer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.conexa.clarity.R;
import android.widget.Button;

import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;
import com.google.android.material.button.MaterialButton;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FocusTimerActivity extends AppCompatActivity {
    private boolean estaCorriendo = false;
    private long tiempoSeleccionado = 5 * 60 * 1000L;
    private MaterialButton boton5min;
    private MaterialButton boton15min;
    private MaterialButton boton25min;
    private Button botonComenzar;
    private CountDownTimer temporizador;
    private Button botonReiniciar;
    private TareaDao tareaDao;
    private List<Tarea> tareasPendientes;
    private Tarea tareaSeleccionada;
    private TextView tvTituloTarea;
    private TextView tvSubtituloTarea;
    private View cardTareaSeleccionada;
    private final int idUsuarioActual = 1;
    private BottomNavigationView bottomNav;


    private long tiempoRestante = tiempoSeleccionado;
    private TextView tvTiempoRestante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_timer);
        bottomNav = findViewById(R.id.bottomNavigationView);
        configurarNavegacion();

        tareaDao = new TareaDao(this);

        tvTituloTarea = findViewById(R.id.tvTituloTarea);
        tvSubtituloTarea = findViewById(R.id.tvSubtituloTarea);
        cardTareaSeleccionada = findViewById(R.id.cardTareaSeleccionada);

        cargarTareasPendientes();

        cardTareaSeleccionada.setOnClickListener(v -> mostrarSelectorTareas());

        tvTiempoRestante = findViewById(R.id.tvTiempoRestante);
        boton5min = findViewById(R.id.boton5Min);
        boton15min = findViewById(R.id.boton15Min);
        boton25min = findViewById(R.id.boton25Min);
        botonComenzar = findViewById(R.id.botonComenzar);
        botonReiniciar = findViewById(R.id.botonReiniciar);
        actualizarReloj();

        boton5min.setOnClickListener(v -> {
            tiempoSeleccionado = 5 * 60 * 1000L;
            tiempoRestante = tiempoSeleccionado;
            actualizarReloj();
            actualizarColorBoton(boton5min);
        });
        boton15min.setOnClickListener(v -> {
            tiempoSeleccionado = 15 * 60 * 1000L;
            tiempoRestante = tiempoSeleccionado;
            actualizarReloj();
            actualizarColorBoton(boton15min);

        });
        boton25min.setOnClickListener(v -> {
            tiempoSeleccionado = 25 * 60 * 1000L;
            tiempoRestante = tiempoSeleccionado;
            actualizarReloj();
            actualizarColorBoton(boton25min);
        });

        botonComenzar.setOnClickListener(v -> {
            if(estaCorriendo){
                temporizador.cancel();
                estaCorriendo = false;
                botonComenzar.setText("Continuar");
                habilitarBotonMin(true);
            }else{
                estaCorriendo = true;
                botonComenzar.setText("Pausar");
                habilitarBotonMin(false);

                temporizador = new CountDownTimer(tiempoRestante, 1000) {
                    public void onTick ( long millisUntilFinished){
                        tiempoRestante = millisUntilFinished;
                        actualizarReloj();
                    }
                    public void onFinish () {
                        tiempoRestante = 0;
                        actualizarReloj();
                    }
                }.start();

            }
        });
        botonReiniciar.setOnClickListener(v -> {
            if(temporizador != null){
                temporizador.cancel();

            }
            estaCorriendo =false;
            tiempoRestante = tiempoSeleccionado;

            actualizarReloj();
            botonComenzar.setText("Comenzar");
            habilitarBotonMin(true);
        });


    }
    private void actualizarReloj(){
        long segundos = tiempoRestante / 1000;
        long minutos = segundos / 60;
        long segundosRestantes = segundos % 60;

        String tiempo = String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutos,
                segundosRestantes

        );
        tvTiempoRestante.setText(tiempo);
    }
    private void actualizarColorBoton(MaterialButton seleccionado){
        int colorSeleccionado = getColor(R.color.verde_principal);
        int colorNoSeleccionado = getColor(R.color.verde_secundario);

        boton5min.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        boton5min == seleccionado ? colorSeleccionado : colorNoSeleccionado
                )
        );
        boton15min.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        boton15min == seleccionado ? colorSeleccionado : colorNoSeleccionado
                )
        );
        boton25min.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        boton25min == seleccionado ? colorSeleccionado : colorNoSeleccionado
                )
        );

    }
    private void habilitarBotonMin(boolean habilitar){
        boton5min.setEnabled(habilitar);
        boton15min.setEnabled(habilitar);
        boton25min.setEnabled(habilitar);
    }
    private void cargarTareasPendientes(){
        List<Tarea> todasLasTareas =
                tareaDao.obtenerTareasPorUsuario(idUsuarioActual);
        tareasPendientes = new ArrayList<>();

        for (Tarea tarea : todasLasTareas){
            if (!tarea.isCompletada()){
                tareasPendientes.add(tarea);
            }
        }
    }

    private void mostrarSelectorTareas() {
        if (tareasPendientes == null || tareasPendientes.isEmpty()) {
            Toast.makeText(
                    this,
                    "No hay tareas pendientes para seleccionar",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        String[] titulos = new String[tareasPendientes.size()];

        for (int i = 0; i < tareasPendientes.size(); i++) {
            titulos[i] = tareasPendientes.get(i).getTitulo();
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar tarea")
                .setItems(titulos, (dialog, posicion) -> {
                    tareaSeleccionada = tareasPendientes.get(posicion);

                    tvTituloTarea.setText(tareaSeleccionada.getTitulo());
                    tvSubtituloTarea.setText(
                            "Tarea seleccionada para esta sesión"
                    );
                })
                .setNegativeButton("Cancelar", null)
                .show();

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