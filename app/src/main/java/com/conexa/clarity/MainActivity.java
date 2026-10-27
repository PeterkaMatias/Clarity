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

import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.local.UsuarioDao;
import com.conexa.clarity.data.model.Tarea;
import com.conexa.clarity.data.model.Usuario;
import com.conexa.clarity.ui.TareasHomeAdapter;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.conexa.clarity.ui.settings.AjustesActivity;
import com.conexa.clarity.ui.timer.FocusTimerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvSaludoUsuario, tvNivelUsuario, tvPuntosTotales, tvSinTareas;
    private RecyclerView rvTareasHoy;
    private BottomNavigationView bottomNav;
    private ImageButton btnAbrirAjustes;
    private MaterialCardView cardFocusRapido;

    private TareasHomeAdapter adapter;
    private TareaDao tareaDao;
    private UsuarioDao usuarioDao;
    private final int idUsuarioActual = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            tareaDao = new TareaDao(this);
            usuarioDao = new UsuarioDao(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        inicializarVistas();
        configurarNavegacion();
        configurarEventos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
        cargarTareasDelDia();
    }

    private void inicializarVistas() {
        tvSaludoUsuario = findViewById(R.id.tvSaludoUsuario);
        tvNivelUsuario = findViewById(R.id.tvNivelUsuario);
        tvPuntosTotales = findViewById(R.id.tvPuntosTotales);
        tvSinTareas = findViewById(R.id.tvSinTareas);
        rvTareasHoy = findViewById(R.id.rvTareasHoy);
        bottomNav = findViewById(R.id.bottomNavigationView);
        btnAbrirAjustes = findViewById(R.id.btnAbrirAjustes);
        cardFocusRapido = findViewById(R.id.cardFocusRapido);

        if (rvTareasHoy != null) {
            rvTareasHoy.setLayoutManager(new LinearLayoutManager(this));
            adapter = new TareasHomeAdapter(new ArrayList<>(), (tarea, isChecked, position) -> {
                if (isChecked && tareaDao != null) {
                    try {
                        tarea.setCompletada(true);
                        tareaDao.actualizarCompletada(tarea.getId(), true);
                        Toast.makeText(this, "Excelente paso! Tarea cumplida", Toast.LENGTH_SHORT).show();
                        cargarDatosUsuario();
                        cargarTareasDelDia();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            rvTareasHoy.setAdapter(adapter);
        }
    }

    private void configurarNavegacion() {
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                return true;
            } else if (itemId == R.id.nav_recursos) {
                startActivity(new Intent(this, RecursosActivity.class));
                return true;
            } else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            } else if (itemId == R.id.nav_timer) {
                startActivity(new Intent(this, FocusTimerActivity.class));
                return true;
            }
            return false;
        });
    }

    private void configurarEventos() {
        if (btnAbrirAjustes != null) {
            btnAbrirAjustes.setOnClickListener(v ->
                    startActivity(new Intent(this, AjustesActivity.class))
            );
        }

        if (cardFocusRapido != null) {
            cardFocusRapido.setOnClickListener(v ->
                    startActivity(new Intent(this, FocusTimerActivity.class))
            );
        }
    }

    private void cargarDatosUsuario() {
        try {
            if (usuarioDao != null) {
                Usuario usuario = usuarioDao.obtenerPorId(idUsuarioActual);
                if (usuario != null && tvSaludoUsuario != null) {
                    tvSaludoUsuario.setText("Hola, " + usuario.getNombre() + "!");
                    if (tvNivelUsuario != null) tvNivelUsuario.setText("Modo " + usuario.getTipoPerfil());
                    if (tvPuntosTotales != null) tvPuntosTotales.setText("✨ Enfoque activo");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tvSaludoUsuario != null) tvSaludoUsuario.setText("Hola!");
        if (tvNivelUsuario != null) tvNivelUsuario.setText("Bienvenido a Clarity");
        if (tvPuntosTotales != null) tvPuntosTotales.setText("✨ 0 pts");
    }

    private void cargarTareasDelDia() {
        List<Tarea> pendientes = new ArrayList<>();
        try {
            if (tareaDao != null) {
                List<Tarea> todas = tareaDao.obtenerTareasPorUsuario(idUsuarioActual);
                if (todas != null) {
                    for (Tarea t : todas) {
                        if (!t.isCompletada()) {
                            pendientes.add(t);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pendientes.isEmpty()) {
            if (rvTareasHoy != null) rvTareasHoy.setVisibility(View.GONE);
            if (tvSinTareas != null) tvSinTareas.setVisibility(View.VISIBLE);
        } else {
            if (rvTareasHoy != null) rvTareasHoy.setVisibility(View.VISIBLE);
            if (tvSinTareas != null) tvSinTareas.setVisibility(View.GONE);
            if (adapter != null) adapter.actualizarTareas(pendientes);
        }
    }
}