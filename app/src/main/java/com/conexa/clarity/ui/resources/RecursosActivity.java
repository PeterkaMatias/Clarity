package com.conexa.clarity.ui.resources;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.RecursoDao;
import com.conexa.clarity.data.model.Recurso;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class RecursosActivity extends AppCompatActivity implements RecursosAdapter.OnRecursoClickListener {

    private RecursoDao recursoDao;
    private RecursosAdapter adapter;
    private RecyclerView rvRecursos;
    private TextView tvSinResultados;
    private SearchView searchView;
    private ChipGroup chipGroup;

    private String categoriaActual = "TODAS";
    private String queryActual = "";
    private final int idUsuarioActual = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recursos);

        recursoDao = new RecursoDao(this);

        rvRecursos = findViewById(R.id.rvRecursos);
        tvSinResultados = findViewById(R.id.tvSinResultados);
        searchView = findViewById(R.id.searchViewRecursos);
        chipGroup = findViewById(R.id.chipGroupCategorias);

        rvRecursos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecursosAdapter(null, this);
        rvRecursos.setAdapter(adapter);

        configurarFiltros();
        cargarDatos();
    }

    private void configurarFiltros() {
        // Filtrado por buscador
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryActual = query;
                cargarDatos();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryActual = newText;
                cargarDatos();
                return true;
            }
        });

        // Filtrado por categorías (Chips)
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                categoriaActual = "TODAS";
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    categoriaActual = chip.getText().toString();
                }
            }
            cargarDatos();
        });
    }

    private void cargarDatos() {
        List<Recurso> lista = recursoDao.obtenerRecursos(idUsuarioActual, categoriaActual, queryActual);
        if (lista.isEmpty()) {
            rvRecursos.setVisibility(View.GONE);
            tvSinResultados.setVisibility(View.VISIBLE);
        } else {
            rvRecursos.setVisibility(View.VISIBLE);
            tvSinResultados.setVisibility(View.GONE);
            adapter.actualizarLista(lista);
        }
    }

    @Override
    public void onRecursoClick(Recurso recurso) {
        Intent intent = new Intent(this, DetalleRecursoActivity.class);
        intent.putExtra("recurso_data", recurso);
        startActivity(intent);
    }

    @Override
    public void onFavoritoClick(Recurso recurso, int position) {
        boolean nuevoEstado = !recurso.isEsFavorito();
        boolean exito = recursoDao.alternarFavorito(idUsuarioActual, recurso.getIdRecurso(), nuevoEstado);
        if (exito) {
            recurso.setEsFavorito(nuevoEstado);
            adapter.notifyItemChanged(position);
            String msj = nuevoEstado ? "Guardado en Favoritos" : "Eliminado de Favoritos";
            Toast.makeText(this, msj, Toast.LENGTH_SHORT).show();
        }
    }
}