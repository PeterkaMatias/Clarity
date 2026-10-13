package com.conexa.clarity.ui.resources;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.conexa.clarity.R;
import com.conexa.clarity.data.model.Recurso;

public class DetalleRecursoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_recurso);

        TextView tvCategoria = findViewById(R.id.tvDetalleCategoria);
        TextView tvTitulo = findViewById(R.id.tvDetalleTitulo);
        TextView tvTiempo = findViewById(R.id.tvDetalleTiempo);
        TextView tvContenido = findViewById(R.id.tvDetalleContenido);

        Recurso recurso;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            recurso = getIntent().getSerializableExtra("recurso_data", Recurso.class);
        } else {
            recurso = (Recurso) getIntent().getSerializableExtra("recurso_data");
        }

        if (recurso != null) {
            tvCategoria.setText(recurso.getCategoria());
            tvTitulo.setText(recurso.getTitulo());
            tvTiempo.setText("⏱ Lectura de " + recurso.getTiempoLecturaMin() + " min");
            tvContenido.setText(recurso.getContenido());
        }
    }
}