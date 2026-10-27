package com.conexa.clarity.ui.tareas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DesgloseTareaActivity extends AppCompatActivity {

    private TextView tvTituloPadre;
    private ChipGroup chipGroupCantidad;
    private Button btnGenerar, btnAceptar, btnAgregarPaso;
    private ProgressBar pbCargando;
    private View layoutListaPreliminar;
    private RecyclerView rvPasos;

    private MicroPasosPreliminarAdapter adapter;
    private TareaDao tareaDao;

    private int idTareaPadre = 0;
    private String tituloTareaPadre = "";
    private int idUsuarioActual = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desglose_tarea);

        tareaDao = new TareaDao(this);

        // Obtener datos de la tarea recibida
        idTareaPadre = getIntent().getIntExtra("id_tarea_padre", 0);
        tituloTareaPadre = getIntent().getStringExtra("titulo_tarea");
        idUsuarioActual = getIntent().getIntExtra("id_usuario", 1);

        if (tituloTareaPadre == null) tituloTareaPadre = "Tarea sin titulo";

        // Vincular vistas
        tvTituloPadre = findViewById(R.id.tvTituloTareaPadre);
        chipGroupCantidad = findViewById(R.id.chipGroupCantidadPasos);
        btnGenerar = findViewById(R.id.btnGenerarPasos);
        btnAceptar = findViewById(R.id.btnAceptarPasos);
        btnAgregarPaso = findViewById(R.id.btnAgregarPasoManual);
        pbCargando = findViewById(R.id.pbCargandoIA);
        layoutListaPreliminar = findViewById(R.id.layoutListaPreliminar);
        rvPasos = findViewById(R.id.rvPasosPreliminares);

        tvTituloPadre.setText(tituloTareaPadre);

        // Configurar RecyclerView
        rvPasos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MicroPasosPreliminarAdapter();
        rvPasos.setAdapter(adapter);

        configurarEventos();


    }
    private void configurarEventos(){

        btnGenerar.setOnClickListener(v -> solicitarDesgloseIA());

        btnAgregarPaso.setOnClickListener(v -> {
            adapter.agregarPaso("Nuevo micro-paso");
            rvPasos.smoothScrollToPosition(adapter.getItemCount() -1);

        });

        btnAceptar.setOnClickListener(v -> guardarMicroPasosEnBaseDeDatos());
    }

    private void solicitarDesgloseIA(){
        int cantidad =(chipGroupCantidad.getCheckedChipId() == R.id.chip5Pasos) ? 5 : 3;

        pbCargando.setVisibility(View.VISIBLE);
        btnGenerar.setEnabled(false);

        //SIMULADOR INTELIGENTE (Permite hacer pruebas)
        // Cuando tenga listo el cliente HTTP de Claude, solo se va a reemplazar esta llamada.
        new Handler(Looper.getMainLooper()).postDelayed(() ->{
            pbCargando.setVisibility(View.GONE);
            btnGenerar.setEnabled(true);

            List<String> pasosGenerados = generarMicroPasosSugeridos(tituloTareaPadre, cantidad);
            adapter.setPasos(pasosGenerados);
            layoutListaPreliminar.setVisibility(View.VISIBLE);

            Toast.makeText(this, "¡Pasos generados! Podes editarlos antes de aceptar:", Toast.LENGTH_SHORT).show();
        }, 1200);
    }

    private List<String> generarMicroPasosSugeridos(String titulo, int cantidad) {
List<String> lista = new ArrayList<>();
lista.add("Paso 1: Reunir materiales y despejar el espacio para " + titulo);
lista.add("Paso 2: Definir el primer objetivo concreto en 10 minutos");
lista.add("Paso 3: Realizar el avance principal de  la tarea");

if(cantidad >= 5) {
    lista.add("Paso 4: Revisar detalles y chequear que no falta nada");
    lista.add("Paso 5: Guardar, ordenar y marcar como completada");

}
return lista;

    }

    private void guardarMicroPasosEnBaseDeDatos(){
        List<String> pasos = adapter.getPasos();
        if (pasos.isEmpty()) {
            Toast.makeText(this, "No hay pasos para guardar", Toast.LENGTH_SHORT).show();
            return;
        }
        String fechaCreacion = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        int guardados = 0;

        for( String textoPaso: pasos){
            String textoLimpio = textoPaso.trim();
            if (!textoLimpio.isEmpty()){
                Tarea microPaso = new Tarea(
                        idUsuarioActual,
                        textoLimpio,
                        "Micro-paso generado con IA",
                        Tarea.ESTADO_PENDIENTE,
                        Tarea.PRIORIDAD_MEDIA,
                        fechaCreacion,
                        null,
                        null,
                        idTareaPadre // <-- VINCULADO A LA TAREA ORIGINAL
                );
                long id = tareaDao.insertar(microPaso);
                if(id > 0) guardados++;
            }
        }
        Toast.makeText(this, "Se guardaron " + guardados + " micro-pasos.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
