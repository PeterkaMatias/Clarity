package com.conexa.clarity.ui.tareas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TareasActivity extends AppCompatActivity implements TareasAdapter.OnTareaClickListener {
    private TareaDao tareaDao;
    private TareasAdapter adapter;
    private RecyclerView rvTareas;
    private TextView tvSinTareas;
    private ChipGroup chipGroupEstado;
    private ChipGroup chipGroupPrioridad;
    private FloatingActionButton fabAgregar;

    private int idUsuarioActual = 1;
    private String filtroEstado = "TODAS";
    private String filtroPrioridad = "TODAS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tareas);

        // Obtener ID del usuario del intent (o usar 1 por defecto)
        idUsuarioActual = getIntent().getIntExtra("id_usuario", 1);

        tareaDao = new TareaDao(this);

        rvTareas = findViewById(R.id.rvTareas);
        tvSinTareas = findViewById(R.id.tvSinTareas);
        chipGroupEstado = findViewById(R.id.chipGroupEstado);
        chipGroupPrioridad = findViewById(R.id.chipGroupPrioridad);
        fabAgregar = findViewById(R.id.fabAgregarTarea);

        rvTareas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TareasAdapter(tareaDao, this);
        rvTareas.setAdapter(adapter);

        configurarFiltros();

        fabAgregar.setOnClickListener(v -> {
            //Toast.makeText(this, "Crear tarea (Modulo 3.2)", Toast.LENGTH_LONG).show(); <de prueba
            // Proximo paso: abrir FormularioTareaActivity
            Intent intent = new Intent(this, FormularioTareaActivity.class);
            intent.putExtra("id_usuario" , idUsuarioActual);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTareas();
    }

    private void configurarFiltros() {
        // Filtro por Estado
        chipGroupEstado.setOnCheckedStateChangeListener((gruop, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                filtroEstado = "TODAS";
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    String texto = chip.getText().toString();
                    if (texto.equalsIgnoreCase("Pendientes")) {
                        filtroEstado = Tarea.ESTADO_PENDIENTE;
                    } else if (texto.equalsIgnoreCase("En Progreso")) {
                        filtroEstado = Tarea.ESTADO_EN_PROGRESO;
                    } else if (texto.equalsIgnoreCase("Completadas")) {
                        filtroEstado = Tarea.ESTADO_COMPLETADA;
                    } else {
                        filtroEstado = "TODAS";
                    }
                }
            }
            cargarTareas();
        });
        // Filtro por Prioridad
        chipGroupPrioridad.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                filtroPrioridad = "TODAS";
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    String texto = chip.getText().toString();
                    if (texto.equalsIgnoreCase("Alta")) {
                        filtroPrioridad = Tarea.PRIORIDAD_ALTA;
                    } else if (texto.equalsIgnoreCase("Media")) {
                        filtroPrioridad = Tarea.PRIORIDAD_MEDIA;
                    } else if (texto.equalsIgnoreCase("Baja")) {
                        filtroPrioridad = Tarea.PRIORIDAD_BAJA;
                    } else {
                        filtroPrioridad = "TODAS";
                    }
                }
            }
            cargarTareas();
        });
    }

    private void cargarTareas(){
        List<Tarea> base = tareaDao.obtenerPrincipales(idUsuarioActual);
        List<Tarea> filtradas = new ArrayList<>();
        for(Tarea t : base){
            boolean cumpleEstado = filtroEstado.equals("TODAS")  || t.getEstado().equalsIgnoreCase(filtroEstado);
            boolean cumplePrioridad = filtroPrioridad.equals("TODAS") || t.getPrioridad().equalsIgnoreCase(filtroPrioridad);

            if(cumpleEstado && cumplePrioridad){
                filtradas.add(t);
            }
        }
        if(filtradas.isEmpty()){
            rvTareas.setVisibility(View.GONE);
            tvSinTareas.setVisibility(View.VISIBLE);
        } else {
            rvTareas.setVisibility(View.VISIBLE);
            tvSinTareas.setVisibility(View.GONE);
            adapter.setTareas(filtradas);
        }
    }

    @Override
    public void onTareaClick(Tarea tarea) {
        // Toast.makeText(this, "Editar: " + tarea.getTitulo(), Toast.LENGTH_SHORT).show(); de prueba
        // Proximo paso; abrir formularioTareaActivity pasando la tarea
        Intent intent = new Intent(this, FormularioTareaActivity.class);
        intent.putExtra("id_usuario", idUsuarioActual);
        intent.putExtra("tarea_editar", tarea);
        startActivity(intent);
    }
    @Override
    public void onTareaCheckChange(Tarea tarea, boolean isChecked){
        String nuevoEstado = isChecked ? Tarea.ESTADO_COMPLETADA : Tarea.ESTADO_PENDIENTE;
        String fechaFinalizado = isChecked ? obtenerFechaActual() : null;

        tareaDao.cambiarEstado(tarea.getIdTarea(), nuevoEstado,fechaFinalizado);
        tarea.setEstado(nuevoEstado);
        tarea.setFechaFinalizado(fechaFinalizado);

        cargarTareas();
    }

    private String obtenerFechaActual(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm" ,Locale.getDefault());
        return sdf.format(new Date());
    }
}