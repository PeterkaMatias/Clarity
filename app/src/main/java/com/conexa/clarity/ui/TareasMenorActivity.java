
package com.conexa.clarity.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.PerfilMenorDao;
import com.conexa.clarity.data.local.PreferenciaUsuarioDao;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;
import com.conexa.clarity.ui.auth.LoginActivity;
import com.conexa.clarity.util.SesionUsuario;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TareasMenorActivity extends AppCompatActivity {

    private int idUsuarioActual;
    private int idPerfilMenor;
    private String nombreMenor;

    private PerfilMenorDao perfilMenorDao;
    private PreferenciaUsuarioDao preferenciaDao;
    private TareaDao tareaDao;

    private TextView tvTituloMenor;
    private TextView tvSinTareasMenor;
    private TextView tvResumenTareasMenor;

    private EditText etTituloTareaMenor;
    private EditText etDescripcionTareaMenor;

    private Spinner spinnerPrioridadMenor;
    private LinearLayout contenedorTareasMenor;

    private Button btnFechaVencimiento;
    private String fechaSeleccionada = null;

    private final String[] prioridades = {
            "MEDIA", "ALTA", "BAJA"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        idUsuarioActual = SesionUsuario.obtenerId(this);

        if (idUsuarioActual == SesionUsuario.SIN_SESION) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
            return;
        }

        idPerfilMenor = getIntent().getIntExtra("id_perfil_menor", -1);
        nombreMenor = getIntent().getStringExtra("nombre_menor");

        perfilMenorDao = new PerfilMenorDao(this);
        preferenciaDao = new PreferenciaUsuarioDao(this);
        tareaDao = new TareaDao(this);

        if (!accesoPermitido()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_tareas_menor);

        tvTituloMenor = findViewById(R.id.tvTituloMenor);
        tvSinTareasMenor = findViewById(R.id.tvSinTareasMenor);
        tvResumenTareasMenor = findViewById(R.id.tvResumenTareasMenor);

        etTituloTareaMenor = findViewById(R.id.etTituloTareaMenor);
        etDescripcionTareaMenor = findViewById(R.id.etDescripcionTareaMenor);
        spinnerPrioridadMenor = findViewById(R.id.spinnerPrioridadMenor);
        contenedorTareasMenor = findViewById(R.id.contenedorTareasMenor);

        Button btnAsignar = findViewById(R.id.btnAsignarTareaMenor);

        tvTituloMenor.setText("Tareas de " + (nombreMenor == null ? "menor" : nombreMenor));

        configurarSpinner(spinnerPrioridadMenor);

        // Selector de fecha agregado dinámicamente
        btnFechaVencimiento = new Button(this);
        btnFechaVencimiento.setText("Fecha de vencimiento (opcional)");

        LinearLayout contenedorFormulario = (LinearLayout) btnAsignar.getParent();

        int posicion = contenedorFormulario.indexOfChild(btnAsignar);

        contenedorFormulario.addView(
                btnFechaVencimiento,
                posicion
        );

        btnFechaVencimiento.setOnClickListener(
                v -> mostrarSelectorFecha(
                        fechaSeleccionada,
                        fecha -> {
                            fechaSeleccionada = fecha;
                            btnFechaVencimiento.setText(
                                    "Vence: " + fecha
                            );
                        }
                )
        );

        btnFechaVencimiento.setOnLongClickListener(v -> {
            fechaSeleccionada = null;
            btnFechaVencimiento.setText("Fecha de vencimiento (opcional)");
            return true;
        });

        btnAsignar.setOnClickListener(v -> asignarTarea());

        cargarTareas();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (perfilMenorDao != null && !accesoPermitido()) {
            finish();
        }
    }

    private boolean accesoPermitido() {

        boolean modoActivo =
                preferenciaDao.estaModoParentalActivo(
                        idUsuarioActual
                );

        boolean pertenece =
                perfilMenorDao.perteneceAlUsuario(
                        idPerfilMenor,
                        idUsuarioActual
                );

        if (!modoActivo || !pertenece) {
            Toast.makeText(
                    this,
                    "No tenés acceso a este perfil",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true;
    }

    private void configurarSpinner(Spinner spinner) {

        ArrayAdapter<String> adaptador =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        prioridades
                );

        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adaptador);
    }

    private void mostrarSelectorFecha(
            String fechaActual,
            java.util.function.Consumer<String> callback
    ) {

        Calendar calendario = Calendar.getInstance();

        if (fechaActual != null && fechaActual.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] partes = fechaActual.split("-");
            calendario.set(
                    Integer.parseInt(partes[0]),
                    Integer.parseInt(partes[1]) - 1,
                    Integer.parseInt(partes[2])
            );
        }

        DatePickerDialog dialogo = new DatePickerDialog(
                this,
                (view, anio, mes, dia) -> {
                    String fecha = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            anio, mes + 1, dia
                    );
                    callback.accept(fecha);
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        dialogo.show();
    }

    private void asignarTarea() {

        if (!accesoPermitido()) {
            finish();
            return;
        }

        String titulo = etTituloTareaMenor.getText().toString().trim();

        String descripcion = etDescripcionTareaMenor.getText().toString().trim();

        String prioridad = spinnerPrioridadMenor.getSelectedItem().toString();

        if (titulo.isEmpty()) {
            etTituloTareaMenor.setError("Ingresá un título");
            return;
        }

        long resultado = tareaDao.asignarTareaMenorConFecha(
                idUsuarioActual,
                idPerfilMenor,
                titulo,
                descripcion,
                prioridad,
                fechaSeleccionada
        );

        if (resultado == -1) {
            Toast.makeText(
                    this,
                    "No se pudo asignar la tarea",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        Toast.makeText(
                this,
                "Tarea asignada correctamente",
                Toast.LENGTH_SHORT
        ).show();

        etTituloTareaMenor.setText("");
        etDescripcionTareaMenor.setText("");
        spinnerPrioridadMenor.setSelection(0);

        fechaSeleccionada = null;
        btnFechaVencimiento.setText("Fecha de vencimiento (opcional)");

        cargarTareas();
    }

    private void cargarTareas() {

        contenedorTareasMenor.removeAllViews();

        List<Tarea> tareas = tareaDao.obtenerTareasDeMenor(
                idUsuarioActual,
                idPerfilMenor
        );

        tvSinTareasMenor.setVisibility(tareas.isEmpty() ? View.VISIBLE : View.GONE);

        int total = tareas.size();
        int completadas = 0;
        int pendientes = 0;
        int enProgreso = 0;

        for (Tarea tarea : tareas) {

            String estado = tarea.getEstado();

            if ("COMPLETADA".equals(estado)) {
                completadas++;
            } else if ("EN_PROGRESO".equals(estado)) {
                enProgreso++;
            } else {
                pendientes++;
            }

            crearTarjetaTarea(tarea);
        }

        int porcentaje = total == 0 ? 0 : Math.round(completadas * 100f / total);

        tvResumenTareasMenor.setText(
                "Total: " + total +
                        "\nPendientes: " + pendientes +
                        " | En progreso: " + enProgreso +
                        "\nCompletadas: " + completadas +
                        "\nProgreso: " + porcentaje + "%"
        );
    }


    private void crearTarjetaTarea(Tarea tarea) {

        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);

        int espacio = dp(12);
        tarjeta.setPadding(espacio, espacio, espacio, espacio);

        tarjeta.setBackgroundColor(
                ContextCompat.getColor(this, R.color.blanco_superficie)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(12);
        tarjeta.setLayoutParams(params);

        // Encabezado: título e íconos
        LinearLayout encabezado = new LinearLayout(this);
        encabezado.setOrientation(LinearLayout.HORIZONTAL);
        encabezado.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        TextView tvTitulo = new TextView(this);
        tvTitulo.setText(tarea.getTitulo());
        tvTitulo.setTextSize(17);
        tvTitulo.setTextColor(
                ContextCompat.getColor(this, R.color.gris_texto)
        );

        encabezado.addView(
                tvTitulo,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        // Ícono editar
        ImageButton btnEditar = new ImageButton(this);
        btnEditar.setImageResource(R.drawable.ic_editar);
        btnEditar.setContentDescription("Editar tarea");
        btnEditar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.TRANSPARENT
                )
        );
        btnEditar.setImageTintList(
                ContextCompat.getColorStateList(this, R.color.verde_principal)
        );

        // Ícono eliminar
        ImageButton btnEliminar = new ImageButton(this);
        btnEliminar.setImageResource(R.drawable.ic_eliminar);
        btnEliminar.setContentDescription("Eliminar tarea");
        btnEliminar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.TRANSPARENT
                )
        );
        btnEliminar.setImageTintList(
                ContextCompat.getColorStateList(this, R.color.gris_texto)
        );

        encabezado.addView(
                btnEditar,
                new LinearLayout.LayoutParams(dp(48), dp(48))
        );

        encabezado.addView(
                btnEliminar,
                new LinearLayout.LayoutParams(dp(48), dp(48))
        );

        tarjeta.addView(encabezado);

        // Detalles de la tarea
        TextView tvDetalle = new TextView(this);

        String detalle =
                "Prioridad: " + tarea.getPrioridad() +
                        "\nEstado: " + tarea.getEstado();

        if (tarea.getFechaVencimiento() != null && !tarea.getFechaVencimiento().isEmpty()) {
            detalle += "\nVence: " +
                    tarea.getFechaVencimiento();
        }

        if (tarea.getDescripcion() != null && !tarea.getDescripcion().trim().isEmpty()) {
            detalle += "\n" + tarea.getDescripcion();
        }

        tvDetalle.setText(detalle);
        tvDetalle.setTextSize(14);
        tvDetalle.setTextColor(ContextCompat.getColor(this, R.color.gris_texto));

        tvDetalle.setPadding(0, dp(8), 0, dp(8));
        tarjeta.addView(tvDetalle);

        // Botón de estado
        boolean completada = "COMPLETADA".equals(tarea.getEstado());

        Button btnEstado = new Button(this);
        btnEstado.setText(completada ? "Marcar como pendiente" : "Marcar como completada");

        tarjeta.addView(btnEstado);

        // Eventos
        btnEditar.setOnClickListener(v -> mostrarDialogoEditar(tarea));

        btnEliminar.setOnClickListener(v -> confirmarEliminar(tarea));

        btnEstado.setOnClickListener(v -> {

            if (!accesoPermitido()) {
                finish();
                return;
            }

            boolean actualizado =
                    tareaDao.cambiarEstadoTareaMenor(
                            idUsuarioActual,
                            idPerfilMenor,
                            tarea.getIdTarea(),
                            !completada
                    );

            if (actualizado) {
                cargarTareas();
            } else {
                mostrarMensaje("No se pudo actualizar la tarea");
            }
        });

        contenedorTareasMenor.addView(tarjeta);
    }


    private void mostrarDialogoEditar(Tarea tarea) {

        if (!accesoPermitido()) {
            finish();
            return;
        }

        LinearLayout formulario = new LinearLayout(this);
        formulario.setOrientation(LinearLayout.VERTICAL);
        formulario.setPadding(dp(20), dp(12), dp(20), dp(4));

        EditText inputTitulo = new EditText(this);
        inputTitulo.setHint("Título");
        inputTitulo.setSingleLine(true);
        inputTitulo.setText(tarea.getTitulo());

        EditText inputDescripcion = new EditText(this);
        inputDescripcion.setHint("Descripción");
        inputDescripcion.setMinLines(2);
        inputDescripcion.setText(tarea.getDescripcion());

        Spinner inputPrioridad = new Spinner(this);
        configurarSpinner(inputPrioridad);

        for (int i = 0; i < prioridades.length; i++) {
            if (prioridades[i].equals(tarea.getPrioridad())) {
                inputPrioridad.setSelection(i);
                break;
            }
        }

        final String[] fecha = {
                tarea.getFechaVencimiento()
        };

        Button btnFecha = new Button(this);
        btnFecha.setText(
                fecha[0] == null || fecha[0].isEmpty()
                        ? "Elegir vencimiento"
                        : "Vence: " + fecha[0]
        );

        btnFecha.setOnClickListener(v ->
                mostrarSelectorFecha(
                        fecha[0],
                        seleccion -> {
                            fecha[0] = seleccion;
                            btnFecha.setText("Vence: " + seleccion);
                        }
                )
        );

        Button btnQuitarFecha = new Button(this);
        btnQuitarFecha.setText("Quitar vencimiento");
        btnQuitarFecha.setOnClickListener(v -> {
            fecha[0] = null;
            btnFecha.setText("Elegir vencimiento");
        });

        formulario.addView(inputTitulo);
        formulario.addView(inputDescripcion);
        formulario.addView(inputPrioridad);
        formulario.addView(btnFecha);
        formulario.addView(btnQuitarFecha);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Editar tarea")
                .setView(formulario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialogo.setOnShowListener(d -> {

            dialogo.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String titulo = inputTitulo.getText().toString().trim();

                if (titulo.isEmpty()) {
                    inputTitulo.setError("Ingresá un título");
                    return;
                }

                if (!accesoPermitido()) {
                    dialogo.dismiss();
                    finish();
                    return;
                }

                boolean actualizado =
                        tareaDao.editarTareaMenor(
                                idUsuarioActual,
                                idPerfilMenor,
                                tarea.getIdTarea(), titulo,
                                inputDescripcion.getText().toString().trim(),
                                inputPrioridad.getSelectedItem().toString(),
                                fecha[0]
                        );

                if (actualizado) {
                    dialogo.dismiss();
                    mostrarMensaje("Tarea actualizada correctamente");
                    cargarTareas();
                } else {
                    mostrarMensaje("No se pudo actualizar la tarea");
                }
            });
        });

        dialogo.show();
    }

    private void confirmarEliminar(Tarea tarea) {

        if (!accesoPermitido()) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar tarea")
                .setMessage("¿Querés eliminar \"" + tarea.getTitulo() + "\"? Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar",
                        (dialog, which) -> {

                            if (!accesoPermitido()) {
                                finish();
                                return;
                            }

                            boolean eliminado = tareaDao.eliminarTareaMenor(
                                            idUsuarioActual,
                                            idPerfilMenor,
                                            tarea.getIdTarea()
                                    );

                            if (eliminado) {
                                mostrarMensaje("Tarea eliminada correctamente");
                                cargarTareas();
                            } else {
                                mostrarMensaje("No se pudo eliminar la tarea");
                            }
                        }
                )
                .show();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
