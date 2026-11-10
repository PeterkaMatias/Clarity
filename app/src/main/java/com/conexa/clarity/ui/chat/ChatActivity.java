package com.conexa.clarity.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.conexa.clarity.BuildConfig;
import com.conexa.clarity.MainActivity;
import com.conexa.clarity.R;
import com.conexa.clarity.data.remote.ClaudeApi;
import com.conexa.clarity.data.remote.ClaudeRequest;
import com.conexa.clarity.data.remote.ClaudeResponse;
import com.conexa.clarity.data.remote.RetrofitClient;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.conexa.clarity.ui.timer.FocusTimerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.conexa.clarity.ui.tareas.TareasActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private View rootChat;
    private RecyclerView rvMensajes;
    private EditText etMensaje;
    private Chip chipPasoAPaso;
    private Chip chipAjustarPlan;
    private BottomNavigationView bottomNavigation;

    private ChatAdapter chatAdapter;
    private List<Mensaje> listaMensajes;

    private ClaudeApi claudeApi;

    private int idUsuario;
    private boolean consultaEnCurso = false;

    /*
     * Variables para animar:
     *
     * Lumi está pensando
     * Lumi está pensando.
     * Lumi está pensando..
     * Lumi está pensando...
     */

    private final Handler handlerAnimacion =
            new Handler(Looper.getMainLooper());

    private Runnable animacionPensando;

    private int posicionMensajePensando = -1;
    private int cantidadPuntos = 0;

    private static final String MODELO =
            "claude-haiku-5-5";

    private static final String PROMPT_SISTEMA =
            "Sos Lumi, el asistente virtual de Clarity. " +
                    "Funcionás como un asistente general, práctico y fácil de entender. " +
                    "Podés responder preguntas generales, explicar temas, sugerir recetas, " +
                    "ayudar con tareas académicas, organizar actividades y dividir " +
                    "objetivos complejos en pasos pequeños. " +
                    "Respondé en español rioplatense, con un tono cálido, natural, " +
                    "claro y directo. " +
                    "Las respuestas deben ser relativamente breves, pero completas. " +
                    "Cuando una explicación requiera varios pasos, usá una lista ordenada. " +
                    "Cuando te pidan una receta, incluí un nombre para la receta, " +
                    "los ingredientes necesarios y pasos simples de preparación. " +
                    "No es obligatorio usar todos los ingredientes mencionados " +
                    "si alguno no combina con la preparación. " +
                    "Cuando te pidan dividir una tarea, devolvé entre 3 y 7 micro-pasos " +
                    "concretos y accionables. " +
                    "No inventes información actual o en tiempo real. " +
                    "Si la consulta necesita datos actuales, indicá brevemente que no tenés " +
                    "acceso a información en tiempo real. " +
                    "No realices diagnósticos médicos ni reemplaces asesoramiento profesional.";

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

        claudeApi = RetrofitClient
                .getClient()
                .create(ClaudeApi.class);

        inicializarVistas();
        configurarAjusteTeclado();
        configurarRecyclerView();
        configurarBotonEnviar();
        configurarSugerenciasPredeterminadas();
        configurarNavegacion();
        mostrarMensajeInicial();
        generarSugerenciasConClaude();
    }

    private void inicializarVistas() {

        rootChat = findViewById(
                R.id.rootChat
        );

        rvMensajes = findViewById(
                R.id.rvMensajes
        );

        etMensaje = findViewById(
                R.id.etMensaje
        );

        chipPasoAPaso = findViewById(
                R.id.chipPasoAPaso
        );

        chipAjustarPlan = findViewById(
                R.id.chipAjustarPlan
        );

        bottomNavigation = findViewById(
                R.id.bottomNavigation
        );
        bottomNavigation.setItemActiveIndicatorEnabled(true);
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

                    if (tecladoVisible) {

                        bottomNavigation.setVisibility(
                                View.GONE
                        );


                        view.setPadding(
                                systemBars.left,
                                systemBars.top,
                                systemBars.right,
                                teclado.bottom
                        );

                    } else {

                        bottomNavigation.setVisibility(
                                View.VISIBLE
                        );

                        view.setPadding(
                                systemBars.left,
                                systemBars.top,
                                systemBars.right,
                                0
                        );
                    }

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(
                rootChat
        );
    }
    private void configurarRecyclerView() {

        listaMensajes =
                new ArrayList<>();

        chatAdapter =
                new ChatAdapter(
                        listaMensajes
                );

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(
                true
        );

        rvMensajes.setLayoutManager(
                layoutManager
        );

        rvMensajes.setAdapter(
                chatAdapter
        );
    }

    private void mostrarMensajeInicial() {

        Mensaje bienvenida =
                new Mensaje(
                        "Hola, soy Lumi 🌿\n\n" +
                                "Puedo ayudarte con tareas, recetas, estudio, " +
                                "organización y consultas simples.\n\n" +
                                "¿Qué necesitás hoy?",
                        false
                );

        chatAdapter.agregarMensaje(
                bienvenida
        );

        desplazarAlUltimoMensaje();
    }

    private void configurarBotonEnviar() {

        findViewById(
                R.id.btnEnviar
        ).setOnClickListener(
                view -> enviarMensaje()
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

        if (consultaEnCurso) {

            Toast.makeText(
                    this,
                    "Lumi todavía está preparando la respuesta",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String texto =
                etMensaje
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

        ConversationMemory.agregarMensajeUsuario(
                texto
        );

        consultarClaude();
    }

    private void consultarClaude() {

        if (apiKeyNoConfigurada()) {

            agregarRespuestaLumi(
                    "No encontré la API key de Claude. " +
                            "Revisá CLAUDE_API_KEY en local.properties."
            );

            return;
        }

        consultaEnCurso = true;

        cambiarEstadoFormulario(
                false
        );

        /*
         * Guardamos la posición del mensaje temporal.
         */

        posicionMensajePensando =
                listaMensajes.size();

        chatAdapter.agregarMensaje(
                new Mensaje(
                        "Lumi está pensando 🌿",
                        false
                )
        );

        iniciarAnimacionPensando(
                posicionMensajePensando
        );

        desplazarAlUltimoMensaje();

        ClaudeRequest request =
                new ClaudeRequest(
                        MODELO,
                        2000,
                        PROMPT_SISTEMA,
                        ConversationMemory.obtenerMensajes()
                );

        claudeApi.enviarMensaje(
                BuildConfig.CLAUDE_API_KEY,
                request
        ).enqueue(
                new Callback<ClaudeResponse>() {

                    @Override
                    public void onResponse(
                            Call<ClaudeResponse> call,
                            Response<ClaudeResponse> response
                    ) {

                        /*
                         * Primero detenemos la animación
                         * y eliminamos la burbuja temporal.
                         */

                        detenerAnimacionPensando();
                        eliminarMensajePensando();

                        consultaEnCurso = false;

                        cambiarEstadoFormulario(
                                true
                        );

                        if (!response.isSuccessful()) {

                            String detalleError =
                                    leerDetalleError(
                                            response
                                    );

                            agregarRespuestaLumi(
                                    "No pude conectarme correctamente con Claude.\n\n" +
                                            "Código HTTP: " +
                                            response.code() +
                                            detalleError
                            );

                            return;
                        }

                        ClaudeResponse respuestaClaude =
                                response.body();

                        if (respuestaClaude == null) {

                            agregarRespuestaLumi(
                                    "Claude respondió sin contenido."
                            );

                            return;
                        }

                        String textoRespuesta =
                                respuestaClaude.obtenerTexto();

                        if (textoRespuesta == null
                                || textoRespuesta.trim().isEmpty()) {

                            String motivo =
                                    respuestaClaude.getStopReason();

                            if ("max_tokens".equals(
                                    motivo
                            )) {

                                agregarRespuestaLumi(
                                        "La respuesta superó el límite disponible. " +
                                                "Probá nuevamente con una consulta más corta."
                                );

                            } else {

                                agregarRespuestaLumi(
                                        "Claude respondió, pero no devolvió " +
                                                "un mensaje de texto. Motivo: " +
                                                (motivo != null
                                                        ? motivo
                                                        : "desconocido")
                                );
                            }

                            return;
                        }

                        ConversationMemory.agregarMensajeLumi(
                                textoRespuesta
                        );

                        agregarRespuestaLumi(
                                textoRespuesta
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<ClaudeResponse> call,
                            Throwable throwable
                    ) {

                        detenerAnimacionPensando();
                        eliminarMensajePensando();

                        consultaEnCurso = false;

                        cambiarEstadoFormulario(
                                true
                        );

                        String mensajeError =
                                throwable.getMessage();

                        if (mensajeError == null
                                || mensajeError.trim().isEmpty()) {

                            mensajeError =
                                    "Error de conexión desconocido";
                        }

                        agregarRespuestaLumi(
                                "No pude conectarme a Internet.\n\n" +
                                        mensajeError
                        );
                    }
                }
        );
    }

    /*
     * ANIMACIÓN DE:
     *
     * Lumi está pensando
     * Lumi está pensando.
     * Lumi está pensando..
     * Lumi está pensando...
     */

    private void iniciarAnimacionPensando(
            int posicion
    ) {

        detenerAnimacionPensando();

        posicionMensajePensando =
                posicion;

        cantidadPuntos = 0;

        animacionPensando =
                new Runnable() {

                    @Override
                    public void run() {

                        if (!consultaEnCurso
                                || posicionMensajePensando < 0
                                || posicionMensajePensando
                                >= listaMensajes.size()) {

                            return;
                        }

                        cantidadPuntos =
                                (cantidadPuntos + 1) % 4;

                        StringBuilder puntos =
                                new StringBuilder();

                        for (
                                int i = 0;
                                i < cantidadPuntos;
                                i++
                        ) {

                            puntos.append(".");
                        }

                        chatAdapter.actualizarMensaje(
                                posicionMensajePensando,
                                "Lumi está pensando" +
                                        puntos +
                                        " 🌿"
                        );

                        handlerAnimacion.postDelayed(
                                this,
                                450
                        );
                    }
                };

        handlerAnimacion.post(
                animacionPensando
        );
    }

    private void detenerAnimacionPensando() {

        if (animacionPensando != null) {

            handlerAnimacion.removeCallbacks(
                    animacionPensando
            );

            animacionPensando = null;
        }

        cantidadPuntos = 0;
    }

    private void eliminarMensajePensando() {

        if (posicionMensajePensando >= 0
                && posicionMensajePensando
                < listaMensajes.size()) {

            listaMensajes.remove(
                    posicionMensajePensando
            );

            chatAdapter.notifyItemRemoved(
                    posicionMensajePensando
            );
        }

        posicionMensajePensando = -1;
    }

    /*
     * SUGERENCIAS PREDETERMINADAS
     */

    private void configurarSugerenciasPredeterminadas() {

        establecerSugerencia(
                chipPasoAPaso,
                "Dividir una tarea"
        );

        establecerSugerencia(
                chipAjustarPlan,
                "Organizar mi día"
        );
    }

    /*
     * SUGERENCIAS GENERADAS POR CLAUDE
     */

    private void generarSugerenciasConClaude() {

        if (apiKeyNoConfigurada()) {

            return;
        }

        String promptSugerencias =
                "Generá exactamente 2 sugerencias diferentes " +
                        "para iniciar una conversación con Lumi. " +
                        "Las sugerencias pueden tratar sobre recetas, estudio, " +
                        "organización, explicaciones simples o tareas cotidianas. " +
                        "Cada sugerencia debe tener entre 3 y 6 palabras " +
                        "y como máximo 35 caracteres. " +
                        "No repitas las mismas categorías en las dos sugerencias. " +
                        "Devolvé únicamente las dos sugerencias, " +
                        "una por línea, sin números, viñetas, comillas ni explicación.";

        String sistemaSugerencias =
                "Generás sugerencias breves, claras y variadas " +
                        "para empezar una conversación con un asistente general.";

        List<ClaudeRequest.Message> mensajesSugerencias =
                new ArrayList<>();

        mensajesSugerencias.add(
                new ClaudeRequest.Message(
                        "user",
                        promptSugerencias
                )
        );

        ClaudeRequest request =
                new ClaudeRequest(
                        MODELO,
                        1000,
                        sistemaSugerencias,
                        mensajesSugerencias
                );

        claudeApi.enviarMensaje(
                BuildConfig.CLAUDE_API_KEY,
                request
        ).enqueue(
                new Callback<ClaudeResponse>() {

                    @Override
                    public void onResponse(
                            Call<ClaudeResponse> call,
                            Response<ClaudeResponse> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            return;
                        }

                        String texto =
                                response.body()
                                        .obtenerTexto();

                        if (texto == null
                                || texto.trim().isEmpty()) {

                            return;
                        }

                        mostrarSugerenciasGeneradas(
                                texto
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<ClaudeResponse> call,
                            Throwable throwable
                    ) {

                        /*
                         * No hacemos nada.
                         * Si falla, quedan las sugerencias
                         * predeterminadas.
                         */
                    }
                }
        );
    }

    private void mostrarSugerenciasGeneradas(
            String respuesta
    ) {

        String[] lineas =
                respuesta.split(
                        "\\r?\\n"
                );

        List<String> sugerencias =
                new ArrayList<>();

        for (String linea : lineas) {

            String sugerencia =
                    limpiarSugerencia(
                            linea
                    );

            if (!sugerencia.isEmpty()
                    && !sugerencias.contains(
                    sugerencia
            )) {

                sugerencias.add(
                        sugerencia
                );
            }

            if (sugerencias.size() == 2) {

                break;
            }
        }

        if (sugerencias.size() < 2) {

            return;
        }

        establecerSugerencia(
                chipPasoAPaso,
                sugerencias.get(0)
        );

        establecerSugerencia(
                chipAjustarPlan,
                sugerencias.get(1)
        );
    }

    private void establecerSugerencia(
            Chip chip,
            String sugerencia
    ) {

        chip.setText(
                sugerencia
        );

        chip.setOnClickListener(
                view -> {

                    etMensaje.setText(
                            sugerencia
                    );

                    prepararCampoDeTexto();
                }
        );
    }

    private String limpiarSugerencia(
            String texto
    ) {

        if (texto == null) {

            return "";
        }

        String resultado =
                texto.trim();

        resultado =
                resultado.replaceFirst(
                        "^[\\-•*]+\\s*",
                        ""
                );

        resultado =
                resultado.replaceFirst(
                        "^\\d+[.)-]?\\s*",
                        ""
                );

        resultado =
                resultado.replace(
                        "\"",
                        ""
                );

        resultado =
                resultado.replace(
                        "'",
                        ""
                );

        resultado =
                resultado.trim();

        if (resultado.length() > 40) {

            resultado =
                    resultado.substring(
                            0,
                            40
                    ).trim();
        }

        return resultado;
    }

    /*
     * MÉTODOS AUXILIARES
     */

    private boolean apiKeyNoConfigurada() {

        return BuildConfig.CLAUDE_API_KEY.isEmpty()
                || BuildConfig.CLAUDE_API_KEY.equals(
                "API_KEY_NO_CONFIGURADA"
        );
    }

    private void agregarRespuestaLumi(
            String respuesta
    ) {

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

    private String leerDetalleError(
            Response<ClaudeResponse> response
    ) {

        if (response.errorBody() == null) {

            return "";
        }

        try {

            String error =
                    response.errorBody()
                            .string();

            if (error.length() > 400) {

                error =
                        error.substring(
                                0,
                                400
                        );
            }

            return "\n\nDetalle:\n" +
                    error;

        } catch (IOException exception) {

            return "";
        }
    }

    private void cambiarEstadoFormulario(
            boolean habilitado
    ) {

        etMensaje.setEnabled(
                habilitado
        );

        findViewById(
                R.id.btnEnviar
        ).setEnabled(
                habilitado
        );

        chipPasoAPaso.setEnabled(
                habilitado
        );

        chipAjustarPlan.setEnabled(
                habilitado
        );
    }

    private void prepararCampoDeTexto() {

        etMensaje.setSelection(
                etMensaje
                        .getText()
                        .length()
        );

        etMensaje.requestFocus();

        mostrarTeclado();
    }

    /*
     * NAVEGACIÓN INFERIOR
     */

    private void configurarNavegacion() {

        bottomNavigation.setSelectedItemId(
                R.id.nav_chat
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int itemId =
                            item.getItemId();

                    if (itemId == R.id.nav_tareas){
                        abrirPantalla(TareasActivity.class);
                        return true;
                    }

                    if (itemId ==
                            R.id.nav_chat) {

                        return true;
                    }

                    if (itemId ==
                            R.id.nav_inicio) {

                        abrirPantalla(
                                MainActivity.class
                        );

                        return true;
                    }

                    if (itemId ==
                            R.id.nav_enfoque) {

                        abrirPantalla(
                                FocusTimerActivity.class
                        );

                        return true;
                    }

                    if (itemId ==
                            R.id.nav_recursos) {

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
            Class<?> pantallaDestino
    ) {

        Intent intent = new Intent(
                this,
                pantallaDestino
        );

        intent.putExtra(
                "id_usuario",
                idUsuario
        );

        startActivity(intent);

        overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
        );

        finish();
    }

    /*
     * SCROLL Y TECLADO
     */

    private void desplazarAlUltimoMensaje() {

        if (listaMensajes == null
                || listaMensajes.isEmpty()) {

            return;
        }

        rvMensajes.post(
                () ->
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

    @Override
    protected void onDestroy() {

        detenerAnimacionPensando();

        super.onDestroy();
    }
}