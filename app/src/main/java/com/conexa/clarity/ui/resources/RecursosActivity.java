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

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.R;
import com.conexa.clarity.data.local.RecursoDao;
import com.conexa.clarity.data.model.Recurso;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.settings.AjustesActivity;
import com.conexa.clarity.ui.tareas.TareasActivity;
import com.conexa.clarity.ui.timer.FocusTimerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;



import java.util.List;

public class RecursosActivity extends AppCompatActivity
        implements RecursosAdapter.OnRecursoClickListener {

    private RecursoDao recursoDao;
    private RecursosAdapter adapter;

    private RecyclerView rvRecursos;
    private TextView tvSinResultados;
    private SearchView searchView;
    private ChipGroup chipGroup;
    private BottomNavigationView bottomNavigation;

    private String categoriaActual = "TODAS";
    private String queryActual = "";

    private int idUsuarioActual = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recursos);

        idUsuarioActual = getIntent().getIntExtra(
                "id_usuario",
                1
        );

        inicializarVistas();
        configurarAjustes();
        configurarNavegacion();

        recursoDao = new RecursoDao(this);

        configurarLista();
        configurarFiltros();
        cargarDatos();
    }

    private void inicializarVistas() {

        rvRecursos = findViewById(
                R.id.rvRecursos
        );

        tvSinResultados = findViewById(
                R.id.tvSinResultados
        );

        searchView = findViewById(
                R.id.searchViewRecursos
        );

        chipGroup = findViewById(
                R.id.chipGroupCategorias
        );

        bottomNavigation = findViewById(
                R.id.bottomNavigationView
        );


        if (bottomNavigation != null) {

            bottomNavigation.setItemActiveIndicatorEnabled(
                    true
            );
        }
    }

    private void configurarAjustes() {

        View botonAjustes = findViewById(
                R.id.btnAbrirAjustes
        );

        if (botonAjustes != null) {

            botonAjustes.setOnClickListener(view -> {

                Intent intent = new Intent(
                        RecursosActivity.this,
                        AjustesActivity.class
                );

                intent.putExtra(
                        "id_usuario",
                        idUsuarioActual
                );

                startActivity(intent);
            });
        }
    }

    private void configurarLista() {

        rvRecursos.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new RecursosAdapter(
                null,
                this
        );

        rvRecursos.setAdapter(
                adapter
        );
    }

    private void configurarNavegacion() {

        if (bottomNavigation == null) {
            return;
        }


        bottomNavigation.setSelectedItemId(
                R.id.nav_recursos
        );

        bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_tareas) {
                abrirPantalla(TareasActivity.class);
                return true;
            }

            if (itemId == R.id.nav_recursos) {

                return true;
            }

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

            return false;
        });
    }

    private void abrirPantalla(
            Class<?> pantallaDestino
    ) {

        Intent intent = new Intent(
                this,
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

    private void configurarFiltros() {

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(
                            String query
                    ) {

                        queryActual = query;
                        cargarDatos();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(
                            String newText
                    ) {

                        queryActual = newText;
                        cargarDatos();

                        return true;
                    }
                }
        );

        chipGroup.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds.isEmpty()) {

                        categoriaActual = "TODAS";

                    } else {

                        Chip chip = findViewById(
                                checkedIds.get(0)
                        );

                        if (chip != null) {

                            categoriaActual =
                                    chip.getText()
                                            .toString();
                        }
                    }

                    cargarDatos();
                }
        );
    }

    private void cargarDatos() {

        List<Recurso> lista =
                recursoDao.obtenerRecursos(
                        idUsuarioActual,
                        categoriaActual,
                        queryActual
                );

        if (lista == null || lista.isEmpty()) {

            rvRecursos.setVisibility(
                    View.GONE
            );

            tvSinResultados.setVisibility(
                    View.VISIBLE
            );

        } else {

            rvRecursos.setVisibility(
                    View.VISIBLE
            );

            tvSinResultados.setVisibility(
                    View.GONE
            );

            adapter.actualizarLista(
                    lista
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (bottomNavigation != null) {

            bottomNavigation.setSelectedItemId(
                    R.id.nav_recursos
            );
        }

        if (recursoDao != null) {
            cargarDatos();
        }
    }

    @Override
    public void onRecursoClick(
            Recurso recurso
    ) {

        Intent intent = new Intent(
                this,
                DetalleRecursoActivity.class
        );

        intent.putExtra(
                "recurso_data",
                recurso
        );

        intent.putExtra(
                "id_usuario",
                idUsuarioActual
        );

        startActivity(intent);
    }

    @Override
    public void onFavoritoClick(
            Recurso recurso,
            int position
    ) {

        boolean nuevoEstado =
                !recurso.isEsFavorito();

        boolean exito =
                recursoDao.alternarFavorito(
                        idUsuarioActual,
                        recurso.getIdRecurso(),
                        nuevoEstado
                );

        if (exito) {

            recurso.setEsFavorito(
                    nuevoEstado
            );

            adapter.notifyItemChanged(
                    position
            );

            String mensaje;

            if (nuevoEstado) {
                mensaje = "Guardado en Favoritos";
            } else {
                mensaje = "Eliminado de Favoritos";
            }

            Toast.makeText(
                    this,
                    mensaje,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}