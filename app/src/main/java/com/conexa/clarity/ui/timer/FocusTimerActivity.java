package com.conexa.clarity.ui.timer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.conexa.clarity.R;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;

public class FocusTimerActivity extends AppCompatActivity {
    private boolean estaCorriendo = false;
    private long tiempoSeleccionado = 5 * 60 * 1000L;
    private MaterialButton boton5min;
    private MaterialButton boton15min;
    private MaterialButton boton25min;
    private Button botonComenzar;
    private CountDownTimer temporizador;
    private Button botonReiniciar;


    private long tiempoRestante = tiempoSeleccionado;
    private TextView tvTiempoRestante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_timer);
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
}