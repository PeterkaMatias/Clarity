package com.conexa.clarity.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.R;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.conexa.clarity.ui.timer.FocusTimerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private View rootChat;
    private RecyclerView rvMensajes;
    private EditText etMensaje;
    private Chip chipPasoAPaso;
    private Chip chipAjustarPlan;
    private BottomNavigationView bottomNavigation;

    private ChatAdapter chatAdapter;
    private List<Mensaje> listaMensajes;

    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        setContentView(R.layout.activity_chat);

        idUsuario = getIntent().getIntExtra(
                "id_usuario",
                -1
        );

        inicializarVistas();
        configurarAjusteTeclado();
        configurarRecyclerView();
        configurarBotonEnviar();
        configurarSugerencias();
        configurarNavegacion();
        mostrarMensajeInicial();
    }

    private void inicializarVistas() {

        rootChat = findViewById(R.id.rootChat);
        rvMensajes = findViewById(R.id.rvMensajes);
        etMensaje = findViewById(R.id.etMensaje);

        chipPasoAPaso = findViewById(
                R.id.chipPasoAPaso
        );

        chipAjustarPlan = findViewById(
                R.id.chipAjustarPlan
        );

        bottomNavigation = findViewById(
                R.id.bottomNavigation
        );
    }

    private void configurarAjusteTeclado() {

        ViewCompat.setOnApplyWindowInsetsListener(
                rootChat,
                (view, windowInsets) -> {

                    Insets systemBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    Insets teclado =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.ime()
                            );

                    boolean tecladoVisible =
                            windowInsets.isVisible(
                                    WindowInsetsCompat.Type.ime()
                            );

                    int paddingInferior;

                    if (tecladoVisible) {

                        paddingInferior = teclado.bottom;

                        bottomNavigation.setVisibility(
                                View.GONE
                        );

                    } else {

                        paddingInferior = systemBars.bottom;

                        bottomNavigation.setVisibility(
                                View.VISIBLE
                        );
                    }

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            paddingInferior
                    );

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(rootChat);
    }

    private void configurarRecyclerView() {

        listaMensajes = new ArrayList<>();

        chatAdapter = new ChatAdapter(
                listaMensajes
        );

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);

        rvMensajes.setLayoutManager(
                layoutManager
        );

        rvMensajes.setAdapter(
                chatAdapter
        );
    }

    private void mostrarMensajeInicial() {

        Mensaje bienvenida = new Mensaje(
                "Hola, soy Lumi 🌿\n\n" +
                        "Estoy acá para ayudarte a organizar tus tareas " +
                        "y avanzar paso a paso.\n\n" +
                        "¿Qué necesitás organizar hoy?",
                false
        );

        chatAdapter.agregarMensaje(
                bienvenida
        );

        desplazarAlUltimoMensaje();
    }

    private void configurarBotonEnviar() {

        findViewById(R.id.btnEnviar)
                .setOnClickListener(v ->
                        enviarMensaje()
                );

        etMensaje.setOnEditorActionListener(
                (textView, actionId, event) -> {

                    if (actionId ==
                            EditorInfo.IME_ACTION_SEND) {

                        enviarMensaje();
                        return true;
                    }

                    return false;
                }
        );
    }

    private void enviarMensaje() {

        String texto = etMensaje
                .getText()
                .toString()
                .trim();

        if (texto.isEmpty()) {

            etMensaje.setError(
                    "Escribí un mensaje"
            );

            etMensaje.requestFocus();

            return;
        }

        Mensaje mensajeUsuario =
                new Mensaje(
                        texto,
                        true
                );

        chatAdapter.agregarMensaje(
                mensajeUsuario
        );

        etMensaje.setText("");

        desplazarAlUltimoMensaje();
        ocultarTeclado();

        responderTemporalmente(texto);
    }

    private void responderTemporalmente(
            String mensajeUsuario
    ) {

        String respuesta;

        String mensajeMinuscula =
                mensajeUsuario.toLowerCase();

        if (mensajeMinuscula.contains("dividir")
                || mensajeMinuscula.contains("paso")) {

            respuesta =
                    "Claro. Vamos a dividirlo en pasos pequeños 🌿\n\n" +
                            "1. Definí qué necesitás terminar.\n" +
                            "2. Elegí el primer paso más sencillo.\n" +
                            "3. Trabajá solamente en ese paso.\n" +
                            "4. Revisá el avance antes de continuar.";

        } else if (mensajeMinuscula.contains("organizar")
                || mensajeMinuscula.contains("plan")) {

            respuesta =
                    "Podemos organizarlo sin llenar más tu lista 🌿\n\n" +
                            "Primero elegí una prioridad. " +
                            "Después agregamos una tarea corta " +
                            "y dejamos espacio para una pausa.";

        } else {

            respuesta =
                    "Te leí 🌿\n\n" +
                            "Puedo ayudarte a organizar esa idea " +
                            "o dividirla en pasos más pequeños.";
        }

        Mensaje mensajeLumi =
                new Mensaje(
                        respuesta,
                        false
                );

        chatAdapter.agregarMensaje(
                mensajeLumi
        );

        desplazarAlUltimoMensaje();
    }

    private void configurarSugerencias() {

        chipPasoAPaso.setOnClickListener(v -> {

            etMensaje.setText(
                    "Ayudame a dividir una tarea en pasos pequeños"
            );

            prepararCampoDeTexto();
        });

        chipAjustarPlan.setOnClickListener(v -> {

            etMensaje.setText(
                    "Necesito reorganizar mi plan para hoy"
            );

            prepararCampoDeTexto();
        });
    }

    private void prepararCampoDeTexto() {

        etMensaje.setSelection(
                etMensaje.getText().length()
        );

        etMensaje.requestFocus();
        mostrarTeclado();
    }

    private void configurarNavegacion() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_chat
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_chat) {
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

                    if (itemId == R.id.nav_recursos) {

                        abrirPantalla(
                                RecursosActivity.class
                        );

                        return true;
                    }

                    Toast.makeText(
                            this,
                            "Pantalla no disponible",
                            Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }
        );
    }

    private void abrirPantalla(
            Class<?> activityDestino
    ) {

        Intent intent = new Intent(
                ChatActivity.this,
                activityDestino
        );

        intent.putExtra(
                "id_usuario",
                idUsuario
        );

        startActivity(intent);
        finish();
    }

    private void desplazarAlUltimoMensaje() {

        if (listaMensajes == null
                || listaMensajes.isEmpty()) {
            return;
        }

        rvMensajes.post(() ->
                rvMensajes.smoothScrollToPosition(
                        listaMensajes.size() - 1
                )
        );
    }

    private void mostrarTeclado() {

        InputMethodManager inputMethodManager =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (inputMethodManager != null) {

            inputMethodManager.showSoftInput(
                    etMensaje,
                    InputMethodManager.SHOW_IMPLICIT
            );
        }
    }

    private void ocultarTeclado() {

        InputMethodManager inputMethodManager =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (inputMethodManager != null) {

            inputMethodManager.hideSoftInputFromWindow(
                    etMensaje.getWindowToken(),
                    0
            );
        }

        etMensaje.clearFocus();
    }
}