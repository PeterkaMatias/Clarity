package com.conexa.clarity.ui.timer;

import static com.google.gson.internal.bind.util.ISO8601Utils.format;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.local.TemporizadorDao;
import com.conexa.clarity.data.model.Tarea;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.conexa.clarity.ui.tareas.TareasActivity;
import com.conexa.clarity.util.SesionUsuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.conexa.clarity.ui.tareas.TareasActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.conexa.clarity.data.model.Temporizador;



public class FocusTimerActivity extends AppCompatActivity {


    private boolean estaCorriendo = false;

    private long tiempoSeleccionado =
            5 * 60 * 1000L;

    private long tiempoRestante =
            tiempoSeleccionado;

    private MaterialButton boton5min;
    private MaterialButton boton15min;
    private MaterialButton boton25min;

    private Button botonComenzar;
    private Button botonReiniciar;

    private CountDownTimer temporizador;

    private TareaDao tareaDao;
    private TemporizadorDao temporizadorDao;
    private List<Tarea> tareasPendientes;
    private Tarea tareaSeleccionada;
    private String fechaInicioSesion;

    private TextView tvTiempoRestante;
    private TextView tvTituloTarea;
    private TextView tvDescripcionTarea;

    private View cardTareaSeleccionada;

    private BottomNavigationView bottomNav;

    private int idUsuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_focus_timer);

        idUsuarioActual = SesionUsuario.obtenerId(this);


        tareaDao = new TareaDao(this);
        temporizadorDao = new TemporizadorDao(this);

        inicializarVistas();
        configurarNavegacion();
        configurarBotonesTiempo();
        configurarBotonComenzar();
        configurarBotonReiniciar();
        configurarSelectorTareas();

        cargarTareasPendientes();
        actualizarReloj();
        actualizarColorBoton(boton5min);
    }

    private void inicializarVistas() {

        bottomNav = findViewById(
                R.id.bottomNavigationView
        );

        tvTituloTarea = findViewById(
                R.id.tvTituloTarea
        );

        tvDescripcionTarea = findViewById(
                R.id.tvDescripcionTarea
        );

        cardTareaSeleccionada = findViewById(
                R.id.cardTareaSeleccionada
        );

        tvTiempoRestante = findViewById(
                R.id.tvTiempoRestante
        );

        boton5min = findViewById(
                R.id.boton5Min
        );

        boton15min = findViewById(
                R.id.boton15Min
        );

        boton25min = findViewById(
                R.id.boton25Min
        );

        botonComenzar = findViewById(
                R.id.botonComenzar
        );

        botonReiniciar = findViewById(
                R.id.botonReiniciar
        );


        if (bottomNav != null) {

            bottomNav.setItemActiveIndicatorEnabled(
                    true
            );
        }
    }

    private void configurarSelectorTareas() {

        if (cardTareaSeleccionada == null) {
            return;
        }

        cardTareaSeleccionada.setOnClickListener(
                view -> mostrarSelectorTareas()
        );
    }

    private void configurarBotonesTiempo() {

        boton5min.setOnClickListener(view -> {

            seleccionarTiempo(
                    5,
                    boton5min
            );
        });

        boton15min.setOnClickListener(view -> {

            seleccionarTiempo(
                    15,
                    boton15min
            );
        });

        boton25min.setOnClickListener(view -> {

            seleccionarTiempo(
                    25,
                    boton25min
            );
        });
    }

    private void seleccionarTiempo(
            int minutos,
            MaterialButton botonSeleccionado
    ) {

        if (estaCorriendo) {
            return;
        }

        tiempoSeleccionado =
                minutos * 60 * 1000L;

        tiempoRestante =
                tiempoSeleccionado;

        actualizarReloj();

        actualizarColorBoton(
                botonSeleccionado
        );
    }

    private void configurarBotonComenzar() {

        botonComenzar.setOnClickListener(view -> {

            if (estaCorriendo) {

                pausarTemporizador();

            } else {

                if(tareaSeleccionada == null){
                    Toast.makeText(
                            this,
                            "Primero tenés que seleccionar una tarea",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                iniciarTemporizador();
            }
        });
    }

    private void iniciarTemporizador() {

        if (tiempoRestante <= 0) {
            tiempoRestante = tiempoSeleccionado;
        }

        if (fechaInicioSesion == null) {
            fechaInicioSesion = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
            ).format(new Date());
        }

        estaCorriendo = true;

        botonComenzar.setText(
                "Pausar"
        );

        habilitarBotonesMinutos(
                false
        );

        temporizador = new CountDownTimer(
                tiempoRestante,
                1000
        ) {

            @Override
            public void onTick(
                    long millisUntilFinished
            ) {

                tiempoRestante =
                        millisUntilFinished;

                actualizarReloj();
            }

            @Override
            public void onFinish() {

                tiempoRestante = 0;
                guardarSesion();
                estaCorriendo = false;

                actualizarReloj();

                botonComenzar.setText(
                        "Comenzar"
                );

                habilitarBotonesMinutos(
                        true
                );

                Toast.makeText(
                        FocusTimerActivity.this,
                        "¡Sesión de enfoque completada!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }.start();
    }

    private void guardarSesion(){
        Temporizador sesion = new Temporizador(
                0,
                idUsuarioActual,
                tareaSeleccionada != null ? tareaSeleccionada.getId() : null,
                (int) (tiempoSeleccionado / 60000),
                fechaInicioSesion,
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date()),
                "Completado"
        );
        sesion.setIdUsuario(idUsuarioActual);

        sesion.setIdTarea(
                tareaSeleccionada != null
                ? tareaSeleccionada.getId()
                        : null
        );

        sesion.setDuracion(
                (int)(tiempoSeleccionado / 60000)
        );
        sesion.setFechaInicio(fechaInicioSesion);

        sesion.setFechaFin(
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date())
        );

        sesion.setEstado("Completado");

        long resultado = temporizadorDao.agregar(sesion);
        Toast.makeText(this, "Resultado de guardado: " + resultado, Toast.LENGTH_LONG).show();

        if(resultado != -1){
            Toast.makeText(
                    this,
                    "Sesion guardada correctamente",
                    Toast.LENGTH_SHORT
            ).show();
            if (tareaSeleccionada != null) {

                tareaDao.cambiarEstado(
                        tareaSeleccionada.getIdTarea(),
                        Tarea.ESTADO_COMPLETADA,
                        new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm",
                                Locale.getDefault()
                        ).format(new Date())
                );
            }
        }
        fechaInicioSesion = null;
    }
    private void pausarTemporizador() {

        if (temporizador != null) {

            temporizador.cancel();
            temporizador = null;
        }

        estaCorriendo = false;

        botonComenzar.setText(
                "Continuar"
        );

        habilitarBotonesMinutos(
                true
        );
    }

    private void configurarBotonReiniciar() {

        botonReiniciar.setOnClickListener(view -> {

            reiniciarTemporizador();
        });
    }

    private void reiniciarTemporizador() {

        if (temporizador != null) {

            temporizador.cancel();
            temporizador = null;
        }

        estaCorriendo = false;
        fechaInicioSesion = null;

        tiempoRestante =
                tiempoSeleccionado;

        actualizarReloj();

        botonComenzar.setText(
                "Comenzar"
        );

        habilitarBotonesMinutos(
                true
        );
    }

    private void actualizarReloj() {

        long segundos =
                tiempoRestante / 1000;

        long minutos =
                segundos / 60;

        long segundosRestantes =
                segundos % 60;

        String tiempo = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutos,
                segundosRestantes
        );

        tvTiempoRestante.setText(
                tiempo
        );
    }

    private void actualizarColorBoton(
            MaterialButton seleccionado
    ) {

        int colorSeleccionado =
                getColor(
                        R.color.verde_principal
                );

        int colorNoSeleccionado =
                getColor(
                        R.color.verde_secundario
                );

        boton5min.setBackgroundTintList(
                ColorStateList.valueOf(
                        boton5min == seleccionado
                                ? colorSeleccionado
                                : colorNoSeleccionado
                )
        );

        boton15min.setBackgroundTintList(
                ColorStateList.valueOf(
                        boton15min == seleccionado
                                ? colorSeleccionado
                                : colorNoSeleccionado
                )
        );

        boton25min.setBackgroundTintList(
                ColorStateList.valueOf(
                        boton25min == seleccionado
                                ? colorSeleccionado
                                : colorNoSeleccionado
                )
        );
    }

    private void habilitarBotonesMinutos(
            boolean habilitar
    ) {

        boton5min.setEnabled(
                habilitar
        );

        boton15min.setEnabled(
                habilitar
        );

        boton25min.setEnabled(
                habilitar
        );
    }

    private void cargarTareasPendientes() {

        List<Tarea> todasLasTareas =
                tareaDao.obtenerTareasPorUsuario(
                        idUsuarioActual
                );

        tareasPendientes =
                new ArrayList<>();

        if (todasLasTareas == null) {
            return;
        }

        for (Tarea tarea : todasLasTareas) {

            if (!tarea.isCompletada()) {

                tareasPendientes.add(
                        tarea
                );
            }
        }
    }

    private void mostrarSelectorTareas() {

        cargarTareasPendientes();

        if (tareasPendientes == null
                || tareasPendientes.isEmpty()) {

            Toast.makeText(
                    this,
                    "No hay tareas pendientes para seleccionar",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] titulos =
                new String[tareasPendientes.size()];

        for (
                int i = 0;
                i < tareasPendientes.size();
                i++
        ) {

            titulos[i] =
                    tareasPendientes
                            .get(i)
                            .getTitulo();
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccionar tarea")
                .setItems(
                        titulos,
                        (dialog, posicion) -> {

                            tareaSeleccionada =
                                    tareasPendientes.get(
                                            posicion
                                    );

                            tvTituloTarea.setText(
                                    tareaSeleccionada
                                            .getTitulo()
                            );

                            String descripcion = tareaSeleccionada.getDescripcion();

                            if (descripcion != null && !descripcion.trim().isEmpty()) {
                                tvDescripcionTarea.setText(descripcion);
                                tvDescripcionTarea.setVisibility(View.VISIBLE);
                            } else {
                                tvDescripcionTarea.setVisibility(View.GONE);
                            }
                        }
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .show();
    }

    /*
     * NAVEGACIÓN INFERIOR
     */

    private void configurarNavegacion() {

        if (bottomNav == null) {
            return;
        }

        bottomNav.setSelectedItemId(
                R.id.nav_enfoque
        );

        bottomNav.setOnItemSelectedListener(item -> {

            int itemId =
                    item.getItemId();

            if (itemId == R.id.nav_tareas) {
                abrirPantalla(TareasActivity.class);
                return true;
            }

            if (itemId == R.id.nav_enfoque) {

                return true;
            }

            if (itemId == R.id.nav_inicio) {

                abrirPantalla(
                        MainActivity.class
                );

                return true;
            }

            if (itemId == R.id.nav_chat) {

                abrirPantalla(
                        ChatActivity.class
                );

                return true;
            }

            if (itemId == R.id.nav_recursos) {

                abrirPantalla(
                        RecursosActivity.class
                );

                return true;
            }

            return false;
        });
    }

    private void abrirPantalla(
            Class<?> pantallaDestino
    ) {

        if (temporizador != null) {

            temporizador.cancel();
            temporizador = null;
        }

        estaCorriendo = false;

        Intent intent = new Intent(
                FocusTimerActivity.this,
                pantallaDestino
        );

        intent.putExtra(
                "id_usuario",
                idUsuarioActual
        );

        startActivity(intent);

        overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
        );

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bottomNav != null) {

            bottomNav.setSelectedItemId(
                    R.id.nav_enfoque
            );
        }

        if (tareaDao != null) {

            cargarTareasPendientes();
        }
    }

    @Override
    protected void onDestroy() {

        if (temporizador != null) {

            temporizador.cancel();
            temporizador = null;
        }

        super.onDestroy();
    }
}