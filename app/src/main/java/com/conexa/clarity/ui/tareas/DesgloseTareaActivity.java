package com.conexa.clarity.ui.tareas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.BuildConfig;
import com.conexa.clarity.R;
import com.conexa.clarity.data.local.RecompensasDao;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;
import com.conexa.clarity.data.remote.ClaudeApi;
import com.conexa.clarity.data.remote.ClaudeRequest;
import com.conexa.clarity.data.remote.ClaudeResponse;
import com.conexa.clarity.data.remote.RetrofitClient;
import com.conexa.clarity.util.SesionUsuario;
import com.google.android.material.chip.ChipGroup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DesgloseTareaActivity
        extends AppCompatActivity {

    private static final String MODELO =
            "claude-haiku-5-5";

    private TextView tvTituloPadre;
    private ChipGroup chipGroupCantidad;
    private Button btnGenerar;
    private Button btnAceptar;
    private Button btnAgregarPaso;
    private ProgressBar pbCargando;
    private View layoutListaPreliminar;
    private RecyclerView rvPasos;

    private MicroPasosPreliminarAdapter adapter;
    private TareaDao tareaDao;
    private RecompensasDao recompensasDao;
    private ClaudeApi claudeApi;

    private int idTareaPadre = 0;
    private String tituloTareaPadre = "";
    private int idUsuarioActual;

    private boolean generandoPasos = false;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_desglose_tarea
        );

        tareaDao =
                new TareaDao(this);

        recompensasDao =
                new RecompensasDao(this);

        claudeApi =
                RetrofitClient
                        .getClient()
                        .create(ClaudeApi.class);

        idTareaPadre =
                getIntent().getIntExtra(
                        "id_tarea_padre",
                        0
                );

        tituloTareaPadre =
                getIntent().getStringExtra(
                        "titulo_tarea"
                );

        idUsuarioActual =
                SesionUsuario.obtenerId(this);

        if (tituloTareaPadre == null
                || tituloTareaPadre.trim().isEmpty()) {

            tituloTareaPadre =
                    "Tarea sin título";
        }

        inicializarVistas();
        configurarRecyclerView();
        configurarEventos();
    }

    private void inicializarVistas() {

        tvTituloPadre =
                findViewById(
                        R.id.tvTituloTareaPadre
                );

        chipGroupCantidad =
                findViewById(
                        R.id.chipGroupCantidadPasos
                );

        btnGenerar =
                findViewById(
                        R.id.btnGenerarPasos
                );

        btnAceptar =
                findViewById(
                        R.id.btnAceptarPasos
                );

        btnAgregarPaso =
                findViewById(
                        R.id.btnAgregarPasoManual
                );

        pbCargando =
                findViewById(
                        R.id.pbCargandoIA
                );

        layoutListaPreliminar =
                findViewById(
                        R.id.layoutListaPreliminar
                );

        rvPasos =
                findViewById(
                        R.id.rvPasosPreliminares
                );

        tvTituloPadre.setText(
                tituloTareaPadre
        );
    }

    private void configurarRecyclerView() {

        rvPasos.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new MicroPasosPreliminarAdapter();

        rvPasos.setAdapter(
                adapter
        );
    }

    private void configurarEventos() {

        btnGenerar.setOnClickListener(
                view -> solicitarDesgloseIA()
        );

        btnAgregarPaso.setOnClickListener(
                view -> {

                    adapter.agregarPaso(
                            "Nuevo micro-paso"
                    );

                    rvPasos.smoothScrollToPosition(
                            adapter.getItemCount() - 1
                    );
                }
        );

        btnAceptar.setOnClickListener(
                view ->
                        guardarMicroPasosEnBaseDeDatos()
        );
    }

    private void solicitarDesgloseIA() {

        if (generandoPasos) {

            Toast.makeText(
                    this,
                    "Claude todavía está generando los pasos",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (apiKeyNoConfigurada()) {

            Toast.makeText(
                    this,
                    "No se encontró la API key de Claude",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int cantidad =
                chipGroupCantidad.getCheckedChipId()
                        == R.id.chip5Pasos
                        ? 5
                        : 3;

        generandoPasos = true;

        pbCargando.setVisibility(
                View.VISIBLE
        );

        btnGenerar.setEnabled(
                false
        );

        btnAceptar.setEnabled(
                false
        );

        String sistema =
                "Sos Lumi, el asistente de Clarity. " +
                        "Tu función es dividir tareas concretas en " +
                        "micro-pasos breves, específicos y realizables. " +
                        "Los pasos deben estar directamente relacionados " +
                        "con la tarea indicada. Respondé en español rioplatense.";

        String prompt =
                "Dividí la siguiente tarea en exactamente " +
                        cantidad +
                        " micro-pasos concretos y específicos.\n\n" +
                        "TAREA: " +
                        tituloTareaPadre +
                        "\n\n" +
                        "Cada micro-paso debe describir una acción real " +
                        "que ayude a completar específicamente esta tarea.\n" +
                        "Los pasos deben estar ordenados de principio a fin.\n" +
                        "Cada paso debe poder realizarse por separado.\n" +
                        "Usá frases breves, claras y accionables.\n" +
                        "No escribas títulos, explicaciones ni introducciones.\n" +
                        "No uses frases genéricas como definir el objetivo, " +
                        "realizar el avance principal, revisar detalles, " +
                        "organizarse o marcar como completada.\n" +
                        "No agregues la palabra Paso ni números.\n" +
                        "Devolvé únicamente " +
                        cantidad +
                        " líneas, con un micro-paso por línea.";

        List<ClaudeRequest.Message> mensajes =
                new ArrayList<>();

        mensajes.add(
                new ClaudeRequest.Message(
                        "user",
                        prompt
                )
        );

        ClaudeRequest request =
                new ClaudeRequest(
                        MODELO,
                        700,
                        sistema,
                        mensajes
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

                        finalizarCarga();

                        if (!response.isSuccessful()) {

                            mostrarErrorClaude(
                                    response
                            );

                            return;
                        }

                        ClaudeResponse respuestaClaude =
                                response.body();

                        if (respuestaClaude == null) {

                            mostrarError(
                                    "Claude respondió sin contenido"
                            );

                            return;
                        }

                        String textoRespuesta =
                                respuestaClaude.obtenerTexto();

                        List<String> pasosGenerados =
                                procesarMicroPasos(
                                        textoRespuesta,
                                        cantidad
                                );

                        if (pasosGenerados.size()
                                != cantidad) {

                            mostrarError(
                                    "No se pudieron generar los " +
                                            cantidad +
                                            " pasos. Volvé a intentarlo."
                            );

                            return;
                        }

                        adapter.setPasos(
                                pasosGenerados
                        );

                        layoutListaPreliminar.setVisibility(
                                View.VISIBLE
                        );

                        btnAceptar.setEnabled(
                                true
                        );

                        Toast.makeText(
                                DesgloseTareaActivity.this,
                                "¡Pasos generados! Podés editarlos antes de guardar.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<ClaudeResponse> call,
                            Throwable throwable
                    ) {

                        finalizarCarga();

                        String detalle =
                                throwable.getMessage();

                        if (detalle == null
                                || detalle.trim().isEmpty()) {

                            detalle =
                                    "Error de conexión desconocido";
                        }

                        mostrarError(
                                "No se pudo conectar con Claude: " +
                                        detalle
                        );
                    }
                }
        );
    }

    private List<String> procesarMicroPasos(
            String respuesta,
            int cantidadEsperada
    ) {

        List<String> pasos =
                new ArrayList<>();

        if (respuesta == null
                || respuesta.trim().isEmpty()) {

            return pasos;
        }

        String[] lineas =
                respuesta.split(
                        "\\r?\\n"
                );

        for (String linea : lineas) {

            String paso =
                    limpiarMicroPaso(
                            linea
                    );

            if (paso.isEmpty()) {
                continue;
            }

            if (pasos.contains(paso)) {
                continue;
            }

            pasos.add(
                    paso
            );

            if (pasos.size()
                    == cantidadEsperada) {

                break;
            }
        }

        return pasos;
    }

    private String limpiarMicroPaso(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        String resultado =
                texto.trim();

        resultado =
                resultado.replaceFirst(
                        "^[-•*]+\\s*",
                        ""
                );

        resultado =
                resultado.replaceFirst(
                        "^\\d+\\s*[.)-]\\s*",
                        ""
                );

        resultado =
                resultado.replaceFirst(
                        "(?i)^paso\\s*\\d*\\s*[:.)-]?\\s*",
                        ""
                );

        resultado =
                resultado.replace(
                        "**",
                        ""
                );

        resultado =
                resultado.trim();

        if (resultado.length() > 180) {

            resultado =
                    resultado.substring(
                            0,
                            180
                    ).trim();
        }

        return resultado;
    }

    private void finalizarCarga() {

        generandoPasos = false;

        pbCargando.setVisibility(
                View.GONE
        );

        btnGenerar.setEnabled(
                true
        );

        btnAceptar.setEnabled(
                adapter != null
                        && adapter.getItemCount() > 0
        );
    }

    private void mostrarErrorClaude(
            Response<ClaudeResponse> response
    ) {

        String detalle =
                "";

        if (response.errorBody() != null) {

            try {

                detalle =
                        response.errorBody()
                                .string();

                if (detalle.length() > 300) {

                    detalle =
                            detalle.substring(
                                    0,
                                    300
                            );
                }

            } catch (IOException ignored) {

                detalle = "";
            }
        }

        mostrarError(
                "Claude no pudo generar los pasos. Código HTTP: " +
                        response.code() +
                        (
                                detalle.isEmpty()
                                        ? ""
                                        : "\n" + detalle
                        )
        );
    }

    private void mostrarError(
            String mensaje
    ) {

        Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_LONG
        ).show();
    }

    private boolean apiKeyNoConfigurada() {

        return BuildConfig.CLAUDE_API_KEY == null
                || BuildConfig.CLAUDE_API_KEY
                .trim()
                .isEmpty()
                || BuildConfig.CLAUDE_API_KEY
                .equals(
                        "API_KEY_NO_CONFIGURADA"
                );
    }

    private void guardarMicroPasosEnBaseDeDatos() {

        List<String> pasos =
                adapter.getPasos();

        if (pasos.isEmpty()) {

            Toast.makeText(
                    this,
                    "No hay pasos para guardar",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String fechaCreacion =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                ).format(
                        new Date()
                );

        int guardados = 0;

        for (String textoPaso : pasos) {

            String textoLimpio =
                    textoPaso.trim();

            if (!textoLimpio.isEmpty()) {

                Tarea microPaso =
                        new Tarea(
                                idUsuarioActual,
                                textoLimpio,
                                "Micro-paso generado con IA",
                                Tarea.ESTADO_PENDIENTE,
                                Tarea.PRIORIDAD_MEDIA,
                                fechaCreacion,
                                null,
                                null,
                                idTareaPadre
                        );

                long id =
                        tareaDao.insertar(
                                microPaso
                        );

                if (id > 0) {
                    guardados++;
                }
            }
        }

        if (guardados > 0) {

            recompensasDao.otorgarPuntos(
                    idUsuarioActual,
                    idTareaPadre,
                    10,
                    "DESGLOSE_IA",
                    "Desglose de tarea con IA",
                    "DESGLOSE_" + idTareaPadre,
                    fechaCreacion
            );

            tareaDao.sincronizarEstadoPadre(
                    idTareaPadre
            );
        }

        Toast.makeText(
                this,
                "Se guardaron " +
                        guardados +
                        " micro-pasos.",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}
