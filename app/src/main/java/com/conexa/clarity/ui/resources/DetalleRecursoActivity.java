package com.conexa.clarity.ui.resources;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.RecompensasDao;
import com.conexa.clarity.data.local.RecursoDao;
import com.conexa.clarity.data.model.Recurso;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalleRecursoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_recurso);

        TextView tvCategoria = findViewById(R.id.tvDetalleCategoria);
        TextView tvTitulo = findViewById(R.id.tvDetalleTitulo);
        TextView tvTiempo = findViewById(R.id.tvDetalleTiempo);
        TextView tvContenido = findViewById(R.id.tvDetalleContenido);
        MaterialButton btnMarcarLeido = findViewById(R.id.btnMarcarLeido);

        int idUsuarioActual = getIntent().getIntExtra("id_usuario", 1);
        RecursoDao recursoDao = new RecursoDao(this);
        RecompensasDao recompensasDao= new RecompensasDao(this);

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

            btnMarcarLeido.setOnClickListener(v -> {
                boolean nuevaLectura = recursoDao.registrarLectura(
                        idUsuarioActual,
                        recurso.getIdRecurso()
                );
                if (nuevaLectura) {

                    boolean puntosOtorgados = recompensasDao.otorgarPuntos(
                            idUsuarioActual,
                            null,
                            15,
                            "LEER_RECURSO",
                            "Lectura del recurso: " + recurso.getTitulo(),
                            "RECURSO_" + recurso.getIdRecurso(),
                            new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss",
                                    Locale.getDefault()
                            ).format(new Date())
                    );

                    if (puntosOtorgados) {
                        Toast.makeText(this,
                                "¡Ganaste 15 puntos por leer este recurso!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Lectura registrada. Los puntos ya habían sido otorgados.",
                                Toast.LENGTH_LONG).show();
                    }

                    btnMarcarLeido.setText("Leído");
                    btnMarcarLeido.setEnabled(false);

                } else {
                    Toast.makeText(this,
                            "Este recurso ya figura como leído.",
                            Toast.LENGTH_SHORT).show();

                    btnMarcarLeido.setText("Leído");
                    btnMarcarLeido.setEnabled(false);
                }
            });

        }
    }
}