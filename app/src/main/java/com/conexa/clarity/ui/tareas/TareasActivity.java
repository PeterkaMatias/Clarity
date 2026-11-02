package com.conexa.clarity.ui.tareas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.conexa.clarity.ui.timer.FocusTimerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TareasActivity extends AppCompatActivity
        implements TareasAdapter.OnTareaClickListener {

    private TareaDao tareaDao;
    private TareasAdapter adapter;

    private RecyclerView rvTareas;
    private TextView tvSinTareas;
    private ChipGroup chipGroupEstado;
    private ChipGroup chipGroupPrioridad;
    private FloatingActionButton fabAgregar;
    private BottomNavigationView bottomNavigation;

    private int idUsuarioActual = 1;

    private String filtroEstado = "TODAS";
    private String filtroPrioridad = "TODAS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tareas);

        idUsuarioActual = getIntent().getIntExtra(
                "id_usuario",
                1
        );

        tareaDao = new TareaDao(this);

        inicializarVistas();
        configurarRecyclerView();
        configurarFiltros();
        configurarBotonAgregar();
        configurarNavegacion();
    }

    private void inicializarVistas() {

        rvTareas = findViewById(
                R.id.rvTareas
        );

        tvSinTareas = findViewById(
                R.id.tvSinTareas
        );

        chipGroupEstado = findViewById(
                R.id.chipGroupEstado
        );

        chipGroupPrioridad = findViewById(
                R.id.chipGroupPrioridad
        );

        fabAgregar = findViewById(
                R.id.fabAgregarTarea
        );

        bottomNavigation = findViewById(
                R.id.bottomNavigationView
        );

        /*
         * Elimina el redondel/indicador violeta
         * del elemento seleccionado.
         */
        if (bottomNavigation != null) {

            bottomNavigation.setItemActiveIndicatorEnabled(
                    false
            );
        }
    }

    private void configurarRecyclerView() {

        rvTareas.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new TareasAdapter(
                tareaDao,
                this
        );

        rvTareas.setAdapter(
                adapter
        );
    }

    private void configurarBotonAgregar() {

        fabAgregar.setOnClickListener(view -> {

            Intent intent = new Intent(
                    TareasActivity.this,
                    FormularioTareaActivity.class
            );

            intent.putExtra(
                    "id_usuario",
                    idUsuarioActual
            );

            startActivity(intent);

            aplicarTransicionSuave();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        cargarTareas();
    }

    private void configurarFiltros() {

        // FILTRO POR ESTADO

        chipGroupEstado.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds.isEmpty()) {

                        filtroEstado = "TODAS";

                    } else {

                        Chip chip = findViewById(
                                checkedIds.get(0)
                        );

                        if (chip != null) {

                            String texto =
                                    chip.getText()
                                            .toString();

                            if (texto.equalsIgnoreCase(
                                    "Pendientes"
                            )) {

                                filtroEstado =
                                        Tarea.ESTADO_PENDIENTE;

                            } else if (
                                    texto.equalsIgnoreCase(
                                            "En Progreso"
                                    )
                            ) {

                                filtroEstado =
                                        Tarea.ESTADO_EN_PROGRESO;

                            } else if (
                                    texto.equalsIgnoreCase(
                                            "Completadas"
                                    )
                            ) {

                                filtroEstado =
                                        Tarea.ESTADO_COMPLETADA;

                            } else {

                                filtroEstado = "TODAS";
                            }
                        }
                    }

                    cargarTareas();
                }
        );

        // FILTRO POR PRIORIDAD

        chipGroupPrioridad.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds.isEmpty()) {

                        filtroPrioridad = "TODAS";

                    } else {

                        Chip chip = findViewById(
                                checkedIds.get(0)
                        );

                        if (chip != null) {

                            String texto =
                                    chip.getText()
                                            .toString();

                            if (texto.equalsIgnoreCase(
                                    "Alta"
                            )) {

                                filtroPrioridad =
                                        Tarea.PRIORIDAD_ALTA;

                            } else if (
                                    texto.equalsIgnoreCase(
                                            "Media"
                                    )
                            ) {

                                filtroPrioridad =
                                        Tarea.PRIORIDAD_MEDIA;

                            } else if (
                                    texto.equalsIgnoreCase(
                                            "Baja"
                                    )
                            ) {

                                filtroPrioridad =
                                        Tarea.PRIORIDAD_BAJA;

                            } else {

                                filtroPrioridad = "TODAS";
                            }
                        }
                    }

                    cargarTareas();
                }
        );
    }

    private void cargarTareas() {

        List<Tarea> base =
                tareaDao.obtenerPrincipales(
                        idUsuarioActual
                );

        List<Tarea> filtradas =
                new ArrayList<>();

        if (base != null) {

            for (Tarea tarea : base) {

                boolean cumpleEstado =
                        filtroEstado.equals("TODAS")
                                || tarea.getEstado()
                                .equalsIgnoreCase(
                                        filtroEstado
                                );

                boolean cumplePrioridad =
                        filtroPrioridad.equals("TODAS")
                                || tarea.getPrioridad()
                                .equalsIgnoreCase(
                                        filtroPrioridad
                                );

                if (cumpleEstado
                        && cumplePrioridad) {

                    filtradas.add(
                            tarea
                    );
                }
            }
        }

        if (filtradas.isEmpty()) {

            rvTareas.setVisibility(
                    View.GONE
            );

            tvSinTareas.setVisibility(
                    View.VISIBLE
            );

        } else {

            rvTareas.setVisibility(
                    View.VISIBLE
            );

            tvSinTareas.setVisibility(
                    View.GONE
            );

            adapter.setTareas(
                    filtradas
            );
        }
    }

    @Override
    public void onTareaClick(
            Tarea tarea
    ) {

        Intent intent = new Intent(
                TareasActivity.this,
                FormularioTareaActivity.class
        );

        intent.putExtra(
                "id_usuario",
                idUsuarioActual
        );

        intent.putExtra(
                "tarea_editar",
                tarea
        );

        startActivity(intent);

        aplicarTransicionSuave();
    }

    @Override
    public void onTareaCheckChange(
            Tarea tarea,
            boolean isChecked
    ) {

        String nuevoEstado =
                isChecked
                        ? Tarea.ESTADO_COMPLETADA
                        : Tarea.ESTADO_PENDIENTE;

        String fechaFinalizado =
                isChecked
                        ? obtenerFechaActual()
                        : null;

        tareaDao.cambiarEstado(
                tarea.getIdTarea(),
                nuevoEstado,
                fechaFinalizado
        );

        tarea.setEstado(
                nuevoEstado
        );

        tarea.setFechaFinalizado(
                fechaFinalizado
        );

        cargarTareas();
    }

    private String obtenerFechaActual() {

        SimpleDateFormat formato =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                );

        return formato.format(
                new Date()
        );
    }

    /*
     * NAVEGACIÓN INFERIOR
     */

    private void configurarNavegacion() {

        if (bottomNavigation == null) {
            return;
        }

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int itemId =
                            item.getItemId();

                    if (itemId == R.id.nav_inicio) {

                        abrirPantalla(
                                MainActivity.class
                        );

                        return true;
                    }

                    if (itemId == R.id.nav_enfoque) {

                        abrirPantalla(
                                FocusTimerActivity.class
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
                }
        );
    }

    private void abrirPantalla(
            Class<?> pantallaDestino
    ) {

        Intent intent = new Intent(
                TareasActivity.this,
                pantallaDestino
        );

        intent.putExtra(
                "id_usuario",
                idUsuarioActual
        );

        startActivity(intent);

        aplicarTransicionSuave();

        finish();
    }

    private void aplicarTransicionSuave() {

        overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
        );
    }
}