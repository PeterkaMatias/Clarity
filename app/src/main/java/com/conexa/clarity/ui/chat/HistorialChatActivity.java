package com.conexa.clarity.ui.chat;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.ConsultaChatDao;
import com.conexa.clarity.data.model.ConsultaChat;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistorialChatActivity
        extends AppCompatActivity
        implements HistorialChatAdapter.OnHistorialClickListener {

    private RecyclerView rvHistorialChat;
    private TextView tvSinHistorial;

    private ConsultaChatDao consultaChatDao;
    private HistorialChatAdapter adapter;

    private final List<ConsultaChat> historial =
            new ArrayList<>();

    private int idUsuario;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_historial_chat
        );

        idUsuario =
                getIntent().getIntExtra(
                        "id_usuario",
                        1
                );

        consultaChatDao =
                new ConsultaChatDao(this);

        inicializarVistas();
        configurarLista();
        cargarHistorial();
    }

    private void inicializarVistas() {

        rvHistorialChat =
                findViewById(
                        R.id.rvHistorialChat
                );

        tvSinHistorial =
                findViewById(
                        R.id.tvSinHistorial
                );

        findViewById(
                R.id.btnVolverHistorial
        ).setOnClickListener(
                view -> finish()
        );
    }

    private void configurarLista() {

        rvHistorialChat.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new HistorialChatAdapter(
                        historial,
                        this
                );

        rvHistorialChat.setAdapter(
                adapter
        );
    }

    private void cargarHistorial() {

        List<ConsultaChat> resultado =
                consultaChatDao.obtenerHistorial(
                        idUsuario
                );

        historial.clear();

        if (resultado != null) {

            historial.addAll(
                    resultado
            );
        }

        adapter.notifyDataSetChanged();

        actualizarEstadoVacio();
    }

    @Override
    public void onHistorialClick(
            ConsultaChat consulta
    ) {

        mostrarDetalleHistorial(
                consulta
        );
    }

    private void mostrarDetalleHistorial(
            ConsultaChat consulta
    ) {

        Dialog dialog =
                new Dialog(this);

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        dialog.setContentView(
                R.layout.dialog_detalle_historial
        );

        Window ventana =
                dialog.getWindow();

        if (ventana != null) {

            ventana.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );
        }

        TextView tvConsulta =
                dialog.findViewById(
                        R.id.tvDialogConsulta
                );

        TextView tvFecha =
                dialog.findViewById(
                        R.id.tvDialogFecha
                );

        TextView tvRespuesta =
                dialog.findViewById(
                        R.id.tvDialogRespuesta
                );

        MaterialButton btnCerrar =
                dialog.findViewById(
                        R.id.btnCerrarDetalleHistorial
                );

        tvConsulta.setText(
                consulta.getConsulta()
        );

        tvFecha.setText(
                consulta.getFecha()
        );

        tvRespuesta.setText(
                limpiarFormatoClaude(
                        consulta.getRespuesta()
                )
        );

        btnCerrar.setOnClickListener(
                view -> dialog.dismiss()
        );

        dialog.setOnShowListener(
                ignored -> {

                    Window window =
                            dialog.getWindow();

                    if (window == null) {
                        return;
                    }

                    int anchoPantalla =
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels;

                    int altoPantalla =
                            getResources()
                                    .getDisplayMetrics()
                                    .heightPixels;

                    int anchoDialog =
                            (int) (
                                    anchoPantalla * 0.90f
                            );

                    int altoDialog =
                            (int) (
                                    altoPantalla * 0.78f
                            );

                    window.setLayout(
                            anchoDialog,
                            altoDialog
                    );

                    window.setSoftInputMode(
                            WindowManager.LayoutParams
                                    .SOFT_INPUT_ADJUST_RESIZE
                    );
                }
        );

        dialog.show();
    }

    private String limpiarFormatoClaude(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace(
                        "**",
                        ""
                )
                .replace(
                        "__",
                        ""
                )
                .trim();
    }

    @Override
    public void onHistorialLongClick(
            ConsultaChat consulta,
            int posicion
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Eliminar del historial"
                )
                .setMessage(
                        "¿Querés eliminar esta consulta?"
                )
                .setPositiveButton(
                        "Eliminar",
                        (dialog, which) -> {

                            boolean eliminado =
                                    consultaChatDao
                                            .eliminarConsulta(
                                                    consulta
                                                            .getIdConsulta()
                                            );

                            if (eliminado) {

                                adapter.eliminarElemento(
                                        posicion
                                );

                                actualizarEstadoVacio();

                                Toast.makeText(
                                        this,
                                        "Consulta eliminada",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .show();
    }

    private void actualizarEstadoVacio() {

        if (historial.isEmpty()) {

            rvHistorialChat.setVisibility(
                    View.GONE
            );

            tvSinHistorial.setVisibility(
                    View.VISIBLE
            );

        } else {

            rvHistorialChat.setVisibility(
                    View.VISIBLE
            );

            tvSinHistorial.setVisibility(
                    View.GONE
            );
        }
    }
}