package com.conexa.clarity.ui.tareas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class FormularioTareaActivity extends AppCompatActivity {
    private TextInputEditText etTitulo, etDescripcion;
    private ChipGroup chipGroupPrioridad;
    private Button btnFecha,  btnHora, btnGuardar, btnEliminar, btnDesglosarIA;
    private TextView tvFechaSeleccionada, tvTituloPantalla;
    private MaterialSwitch switchRecordatorio;

    private TareaDao tareaDao;
    private Tarea tareaActual; // null si estamos creando una nueva
    private int idUsuarioActual = 1;

    private String fechaSeleccionada= "";
    private String horaSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_tarea);

        tareaDao = new TareaDao(this);
        idUsuarioActual = getIntent().getIntExtra("id_usuario", 1);

        // Vincular vistas
        tvTituloPantalla     = findViewById(R.id.tvTituloPantalla);
        etTitulo             = findViewById(R.id.etTituloTarea);
        etDescripcion        = findViewById(R.id.etDescripcionTarea);
        chipGroupPrioridad   = findViewById(R.id.chipGroupFormPrioridad);
        btnFecha             = findViewById(R.id.btnSeleccionarFecha);
        btnHora              = findViewById(R.id.btnSeleccionarHora);
        btnGuardar           = findViewById(R.id.btnGuardarTarea);
        btnEliminar          = findViewById(R.id.btnEliminarTarea);
        btnDesglosarIA       = findViewById(R.id.btnDesglosarIA);
        tvFechaSeleccionada  = findViewById(R.id.tvFechaSeleccionada);
        switchRecordatorio   = findViewById(R.id.switchRecordatorio);

        // Detectar si recibimos una tarea para editar
        if (getIntent().hasExtra("tarea_editar")) {
            tareaActual = (Tarea) getIntent().getSerializableExtra("tarea_editar");
            cargarDatosTareaExistente();
        }

        configurarSelectoresFechaHora();
        configurarBotones();
    }
    private void cargarDatosTareaExistente(){
        tvTituloPantalla.setText("Editar Tarea");
        btnGuardar.setText("Actualizar Tarea");
        btnEliminar.setVisibility(View.VISIBLE);

        etTitulo.setText(tareaActual.getTitulo());
        etDescripcion.setText(tareaActual.getDescripcion());
        // Prioridad
        if(Tarea.PRIORIDAD_ALTA.equalsIgnoreCase(tareaActual.getPrioridad())) {
            chipGroupPrioridad.check(R.id.chipFormAlta);
        }else if (Tarea.PRIORIDAD_BAJA.equalsIgnoreCase(tareaActual.getPrioridad())) {
            chipGroupPrioridad.check(R.id.chipFormBaja);
        }else {
            chipGroupPrioridad.check(R.id.chipFormMedia);
        }

        // Fecha de vencimiento
        if (tareaActual.getFechaVencimiento() != null && !tareaActual.getFechaVencimiento().isEmpty()){
            String fv = tareaActual.getFechaVencimiento();
            tvFechaSeleccionada.setText(" Vence: " + fv);
            String[] partes = fv.split(" ");
            if(partes.length > 0) fechaSeleccionada = partes[0];
            if(partes.length > 1) horaSeleccionada = partes[1];

        }

    }

    private void configurarSelectoresFechaHora(){
        Calendar calendario = Calendar.getInstance();

        btnFecha.setOnClickListener(v -> {
            int anio = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(this,(view, year, month, dayOfMonth) -> {
                fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                actualizarTextoFecha();
            }, anio,mes,dia);
            dpd.show();
        });

        btnHora.setOnClickListener(v -> {
            int hora = calendario.get(Calendar.HOUR_OF_DAY);
            int min = calendario.get(Calendar.MINUTE);

            TimePickerDialog tpd = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                actualizarTextoFecha();
            }, hora,min,true);
            tpd.show();
        });
    }

    private void actualizarTextoFecha() {
        if(!fechaSeleccionada.isEmpty()){
            String resultado = fechaSeleccionada;
            if (!horaSeleccionada.isEmpty()) {
                resultado += " " +horaSeleccionada;
            }
            tvFechaSeleccionada.setText(" Vence: "+ resultado);
        }else {
            tvFechaSeleccionada.setText("Sin fecha limite");
        }
    }

    private void configurarBotones(){
        // Guardat
        btnGuardar.setOnClickListener(v -> guardarTarea());
        // Eliminar
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
        // boton destacado: Desglosar con IA
        btnDesglosarIA.setOnClickListener(v -> {
            String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim():"";
            if(titulo.isEmpty()){
                etTitulo.setError("Escribi un titulo primero para que la IA sepa que desglosar");
                etTitulo.requestFocus();
                return;
            }
            // Si la tarea aun no se guardo, la guardamos primero para tener su idTarea
            if (tareaActual == null){
                long idNueva = guardarTareaRetornandoId();
                if(idNueva <= 0) return; // Error al guardar
                tareaActual = new Tarea();
                tareaActual.setIdTarea((int) idNueva);
                tareaActual.setTitulo(titulo);
            }
            // Toast.makeText(this, "Abriendo desglose con IA (Modulo 3.3)...", Toast.LENGTH_SHORT).show();
            //Proximo paso (3.3)
            Intent intent = new Intent(this, DesgloseTareaActivity.class);
            intent.putExtra("id_tarea_padre", tareaActual.getIdTarea());
            intent.putExtra("titulo_tarea", tareaActual.getTitulo());
            intent.putExtra("id_usuario",idUsuarioActual);
            startActivity(intent);
        });
    }
    private void guardarTarea() {
        long id = guardarTareaRetornandoId();
        if (id > 0){
            finish(); ///cerrara cuando haya un titulo
        }

    }

    private long guardarTareaRetornandoId() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim(): "";
        String descripcion =etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        if (titulo.isEmpty()) {
            etTitulo.setError("El titulo es obligatorio");
            etTitulo.requestFocus();
            return -1;
        }

        String prioridad = Tarea.PRIORIDAD_MEDIA;
        int checkedId = chipGroupPrioridad.getCheckedChipId();
        if (checkedId == R.id.chipFormAlta) {
            prioridad = Tarea.PRIORIDAD_ALTA;

        } else if (checkedId == R.id.chipFormBaja){
            prioridad = Tarea.PRIORIDAD_BAJA;
        }
        String fechaVencimientoCompletada = "";
        if(!fechaSeleccionada.isEmpty()){
            fechaVencimientoCompletada = fechaSeleccionada + (horaSeleccionada.isEmpty() ? "" : " " + horaSeleccionada);

        }
        if (tareaActual == null){
            //MODO CREACION
            String fechaCreacion = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            Tarea nueva = new Tarea(
                    idUsuarioActual,
                    titulo,
                    descripcion,
                    Tarea.ESTADO_PENDIENTE,
                    prioridad,fechaCreacion,
                    null, // Fecha_finalizado
                    fechaVencimientoCompletada.isEmpty() ? null : fechaVencimientoCompletada,
                    0 // Tarea principal (sin padre)
            );
            long id = tareaDao.insertar(nueva);
            if (id > 0){
                Toast.makeText(this, "¡Tarea guardada con exito!", Toast.LENGTH_SHORT).show();
                return id;
            } else {
                Toast.makeText(this, "Error al guarda la tarea", Toast.LENGTH_SHORT).show();
                return -1;
            }
        } else {
            //MODO EDICION
            tareaActual.setTitulo(titulo);
            tareaActual.setDescripcion(descripcion);
            tareaActual.setPrioridad(prioridad);
            tareaActual.setFechaVencimiento(fechaVencimientoCompletada.isEmpty() ? null : fechaVencimientoCompletada);

            boolean exito = tareaDao.actualizar(tareaActual);
            if(exito) {
                Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
                return tareaActual.getIdTarea();
        }else {
                Toast.makeText(this, "Error al actualziar", Toast.LENGTH_SHORT).show();
                return -1;

            }
    }
}

private void confirmarEliminar() {
    if(tareaActual == null) return;
    new AlertDialog.Builder(this)
            .setTitle("¿Eliminar tarea?")
            .setMessage("Esta accion eliminara la tarea y todos sus micro-pasos.")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                boolean ok = tareaDao.eliminar(tareaActual.getIdTarea());
                if(ok){
                    Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(this,"No se pudo eliminar", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
