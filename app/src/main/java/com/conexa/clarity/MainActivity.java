package com.conexa.clarity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.data.local.LogrosDao;
import com.conexa.clarity.data.local.PreferenciaUsuarioDao;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.local.UsuarioDao;
import com.conexa.clarity.data.model.Tarea;
import com.conexa.clarity.data.model.Usuario;
import com.conexa.clarity.ui.PerfilesMenoresActivity;
import com.conexa.clarity.ui.TareasHomeAdapter;
import com.conexa.clarity.ui.auth.LoginActivity;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.logros.LogrosyProgreso;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.conexa.clarity.ui.settings.AjustesActivity;
import com.conexa.clarity.ui.tareas.FormularioTareaActivity;
import com.conexa.clarity.ui.tareas.TareasActivity;
import com.conexa.clarity.ui.timer.FocusTimerActivity;
import com.conexa.clarity.util.SesionUsuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvSaludoUsuario;
    private TextView tvNivelUsuario;
    private TextView tvPuntosTotales;
    private TextView tvSinTareas;
    private TextView tvMensajeMotivacional;

    private RecyclerView rvTareasHoy;
    private BottomNavigationView bottomNav;
    private ImageButton btnAbrirAjustes;
    private MaterialCardView cardFocusRapido;
    private FloatingActionButton fabAgregarTarea;
    private LinearProgressIndicator progresoNivel;

    private View panelParental;
    private View panelGeneral;

    private TareasHomeAdapter adapter;
    private TareaDao tareaDao;
    private UsuarioDao usuarioDao;
    private PreferenciaUsuarioDao preferenciaDao;
    private LogrosDao logrosDao;

    private int idUsuarioActual;
    private boolean modoParentalActivo = false;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );

        idUsuarioActual =
                SesionUsuario.obtenerId(this);

        if (idUsuarioActual
                == SesionUsuario.SIN_SESION) {

            Intent intent =
                    new Intent(
                            this,
                            LoginActivity.class
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

            return;
        }

        preferenciaDao =
                new PreferenciaUsuarioDao(this);

        tareaDao =
                new TareaDao(this);

        usuarioDao =
                new UsuarioDao(this);

        logrosDao =
                new LogrosDao(this);

        inicializarVistas();
        configurarNavegacion();
        configurarEventos();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cargarModoParental();
        cargarDatosUsuario();
        actualizarProgresoLogros();

        if (!modoParentalActivo) {

            cargarTareasDelDia();
        }

        if (bottomNav != null) {

            bottomNav.setSelectedItemId(
                    R.id.nav_inicio
            );
        }
    }

    private void inicializarVistas() {

        tvSaludoUsuario =
                findViewById(
                        R.id.tvSaludoUsuario
                );

        tvNivelUsuario =
                findViewById(
                        R.id.tvNivelUsuario
                );

        tvPuntosTotales =
                findViewById(
                        R.id.tvPuntosTotales
                );

        tvSinTareas =
                findViewById(
                        R.id.tvSinTareas
                );

        rvTareasHoy =
                findViewById(
                        R.id.rvTareasHoy
                );

        bottomNav =
                findViewById(
                        R.id.bottomNavigationView
                );

        btnAbrirAjustes =
                findViewById(
                        R.id.btnAbrirAjustes
                );

        cardFocusRapido =
                findViewById(
                        R.id.cardFocusRapido
                );

        fabAgregarTarea =
                findViewById(
                        R.id.fabAgregarTareaInicio
                );

        panelParental =
                findViewById(
                        R.id.panelParental
                );

        panelGeneral =
                findViewById(
                        R.id.panelGeneral
                );

        tvMensajeMotivacional =
                findViewById(
                        R.id.tvMensajeMotivacional
                );

        progresoNivel =
                findViewById(
                        R.id.progresoNivel
                );

        if (bottomNav != null) {

            bottomNav.setItemActiveIndicatorEnabled(
                    true
            );
        }

        configurarListaTareas();
    }

    private void configurarListaTareas() {

        if (rvTareasHoy == null) {
            return;
        }

        rvTareasHoy.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new TareasHomeAdapter(
                        new ArrayList<>(),
                        (tarea, isChecked, position) -> {

                            if (!isChecked
                                    || tareaDao == null) {

                                return;
                            }

                            try {

                                boolean actualizada =
                                        tareaDao.actualizarCompletada(
                                                tarea.getId(),
                                                true
                                        );

                                if (actualizada) {

                                    tarea.setCompletada(
                                            true
                                    );

                                    Toast.makeText(
                                            this,
                                            "¡Excelente paso! Tarea cumplida",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                } else {

                                    Toast.makeText(
                                            this,
                                            "Primero completá todos los micro-pasos de esta tarea",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                                cargarDatosUsuario();
                                cargarTareasDelDia();
                                actualizarProgresoLogros();

                            } catch (Exception exception) {

                                exception.printStackTrace();
                            }
                        }
                );

        rvTareasHoy.setAdapter(
                adapter
        );
    }

    private void actualizarProgresoLogros() {

        if (logrosDao == null
                || progresoNivel == null
                || idUsuarioActual <= 0) {

            return;
        }

        try {

            int totalLogros =
                    logrosDao
                            .contarLogrosDisponibles();

            int logrosObtenidos =
                    logrosDao
                            .contarLogrosObtenidos(
                                    idUsuarioActual
                            );

            int porcentaje =
                    logrosDao
                            .calcularPorcentajeLogros(
                                    idUsuarioActual
                            );

            progresoNivel.setProgressCompat(
                    porcentaje,
                    true
            );

            if (tvPuntosTotales != null) {

                tvPuntosTotales.setText(
                        "🏆 " +
                                logrosObtenidos +
                                " de " +
                                totalLogros +
                                " logros"
                );
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            progresoNivel.setProgressCompat(
                    0,
                    false
            );

            if (tvPuntosTotales != null) {

                tvPuntosTotales.setText(
                        "🏆 0 logros"
                );
            }
        }
    }

    private void abrirPantalla(
            Class<?> pantallaDestino
    ) {

        Intent intent =
                new Intent(
                        MainActivity.this,
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

    private void configurarNavegacion() {

        if (bottomNav == null) {
            return;
        }

        bottomNav.setSelectedItemId(
                R.id.nav_inicio
        );

        bottomNav.setOnItemSelectedListener(
                item -> {

                    int itemId =
                            item.getItemId();

                    if (itemId
                            == R.id.nav_inicio) {

                        return true;

                    } else if (
                            itemId == R.id.nav_tareas
                    ) {

                        abrirPantalla(
                                TareasActivity.class
                        );

                        return true;

                    } else if (
                            itemId == R.id.nav_recursos
                    ) {

                        abrirPantalla(
                                RecursosActivity.class
                        );

                        return true;

                    } else if (
                            itemId == R.id.nav_chat
                    ) {

                        abrirPantalla(
                                ChatActivity.class
                        );

                        return true;

                    } else if (
                            itemId == R.id.nav_enfoque
                    ) {

                        abrirPantalla(
                                FocusTimerActivity.class
                        );

                        return true;
                    }

                    return false;
                }
        );
    }

    private void configurarEventos() {

        ImageButton btnAbrirLogros =
                findViewById(
                        R.id.btnAbrirLogros
                );

        if (btnAbrirLogros != null) {

            btnAbrirLogros.setOnClickListener(
                    view -> {

                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        LogrosyProgreso.class
                                );

                        intent.putExtra(
                                "id_usuario",
                                idUsuarioActual
                        );

                        startActivity(intent);
                    }
            );
        }

        if (btnAbrirAjustes != null) {

            btnAbrirAjustes.setOnClickListener(
                    view -> {

                        Intent intent =
                                new Intent(
                                        this,
                                        AjustesActivity.class
                                );

                        intent.putExtra(
                                "id_usuario",
                                idUsuarioActual
                        );

                        startActivity(intent);
                    }
            );
        }

        if (cardFocusRapido != null) {

            cardFocusRapido.setOnClickListener(
                    view -> {

                        Intent intent =
                                new Intent(
                                        this,
                                        FocusTimerActivity.class
                                );

                        intent.putExtra(
                                "id_usuario",
                                idUsuarioActual
                        );

                        startActivity(intent);
                    }
            );
        }

        if (fabAgregarTarea != null) {

            fabAgregarTarea.setOnClickListener(
                    view -> {

                        Intent intent =
                                new Intent(
                                        this,
                                        FormularioTareaActivity.class
                                );

                        intent.putExtra(
                                "id_usuario",
                                idUsuarioActual
                        );

                        startActivity(intent);
                    }
            );
        }

        View btnAdministrarMenores =
                findViewById(
                        R.id.btnAdministrarMenores
                );

        if (btnAdministrarMenores != null) {

            btnAdministrarMenores.setOnClickListener(
                    view -> {

                        if (modoParentalActivo) {

                            Intent intent =
                                    new Intent(
                                            this,
                                            PerfilesMenoresActivity.class
                                    );

                            intent.putExtra(
                                    "id_usuario",
                                    idUsuarioActual
                            );

                            startActivity(intent);
                        }
                    }
            );
        }

        View btnVerTodasTareas =
                findViewById(
                        R.id.btnVerTodasTareas
                );

        if (btnVerTodasTareas != null) {

            btnVerTodasTareas.setOnClickListener(
                    view ->
                            abrirPantalla(
                                    TareasActivity.class
                            )
            );
        }
    }

    private void cargarDatosUsuario() {

        try {

            if (usuarioDao != null) {

                Usuario usuario =
                        usuarioDao.obtenerPorId(
                                idUsuarioActual
                        );

                if (usuario != null
                        && tvSaludoUsuario != null) {

                    tvSaludoUsuario.setText(
                            "Hola, " +
                                    usuario.getNombre() +
                                    "!"
                    );

                    if (tvNivelUsuario != null) {

                        tvNivelUsuario.setText(
                                modoParentalActivo
                                        ? "Modo Parental"
                                        : "Modo General"
                        );
                    }

                    return;
                }
            }

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        if (tvSaludoUsuario != null) {

            tvSaludoUsuario.setText(
                    "¡Hola!"
            );
        }

        if (tvNivelUsuario != null) {

            tvNivelUsuario.setText(
                    "Bienvenido a Clarity"
            );
        }
    }

    private void cargarTareasDelDia() {

        List<Tarea> pendientes =
                new ArrayList<>();

        try {

            if (tareaDao != null) {

                List<Tarea> todas =
                        tareaDao
                                .obtenerTareasPorUsuario(
                                        idUsuarioActual
                                );

                if (todas != null) {

                    for (Tarea tarea : todas) {

                        if (!tarea.isCompletada()) {

                            pendientes.add(
                                    tarea
                            );
                        }
                    }
                }
            }

        } catch (Exception exception) {

            exception.printStackTrace();
        }

        if (pendientes.isEmpty()) {

            if (rvTareasHoy != null) {

                rvTareasHoy.setVisibility(
                        View.GONE
                );
            }

            if (tvSinTareas != null) {

                tvSinTareas.setVisibility(
                        View.VISIBLE
                );
            }

        } else {

            if (rvTareasHoy != null) {

                rvTareasHoy.setVisibility(
                        View.VISIBLE
                );
            }

            if (tvSinTareas != null) {

                tvSinTareas.setVisibility(
                        View.GONE
                );
            }

            if (adapter != null) {

                adapter.actualizarTareas(
                        pendientes
                );
            }
        }
    }

    private void cargarModoParental() {

        modoParentalActivo =
                preferenciaDao
                        .estaModoParentalActivo(
                                idUsuarioActual
                        );

        panelGeneral.setVisibility(
                modoParentalActivo
                        ? View.GONE
                        : View.VISIBLE
        );

        panelParental.setVisibility(
                modoParentalActivo
                        ? View.VISIBLE
                        : View.GONE
        );

        if (fabAgregarTarea != null) {

            fabAgregarTarea.setVisibility(
                    modoParentalActivo
                            ? View.GONE
                            : View.VISIBLE
            );
        }

        tvMensajeMotivacional.setText(
                modoParentalActivo
                        ? "Organizá y acompañá las actividades del menor."
                        : "Un paso a la vez, a tu propio ritmo."
        );
    }
}