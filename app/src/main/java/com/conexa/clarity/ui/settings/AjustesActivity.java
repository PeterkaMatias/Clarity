
package com.conexa.clarity.ui.settings;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.PreferenciaUsuarioDao;

public class AjustesActivity extends AppCompatActivity {

    private PreferenciaUsuarioDao dao;

    private RadioGroup rgTema;
    private RadioButton rbTemaClaro;
    private RadioButton rbTemaOscuro;

    private SwitchCompat swNotificaciones;
    private SwitchCompat swRecordatorios;
    private SwitchCompat swSonido;
    private SwitchCompat swVibracion;

    private Button btnGuardar;
    private Button btnRestaurar;

    // Valor temporal para pruebas.
    // Después debe reemplazarse por el ID del usuario autenticado.
    private final int idUsuarioActual = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        dao = new PreferenciaUsuarioDao(this);

        vincularControles();
        establecerValoresPredeterminados();
        cargarPreferencias();
        configurarBotones();
    }

    private void vincularControles() {

        rgTema = findViewById(R.id.rgTema);
        rbTemaClaro = findViewById(R.id.rbTemaClaro);
        rbTemaOscuro = findViewById(R.id.rbTemaOscuro);

        swNotificaciones = findViewById(R.id.swNotificaciones);
        swRecordatorios = findViewById(R.id.swRecordatorios);
        swSonido = findViewById(R.id.swSonido);
        swVibracion = findViewById(R.id.swVibracion);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnRestaurar = findViewById(R.id.btnRestaurar);
    }

    private void establecerValoresPredeterminados() {

        rbTemaClaro.setChecked(true);

        swNotificaciones.setChecked(true);
        swRecordatorios.setChecked(true);
        swSonido.setChecked(true);
        swVibracion.setChecked(true);
    }

    private void cargarPreferencias() {

        Cursor cursor = dao.obtenerPreferencias(idUsuarioActual);

        try {
            if (cursor.moveToFirst()) {

                boolean notificaciones =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "Notificaciones"
                                )
                        ) == 1;

                boolean recordatorios =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "Recordatorios"
                                )
                        ) == 1;

                boolean sonido =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "Sonido"
                                )
                        ) == 1;

                boolean vibracion =
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "Vibracion"
                                )
                        ) == 1;

                String tema = cursor.getString(
                        cursor.getColumnIndexOrThrow("Tema")
                );

                swNotificaciones.setChecked(notificaciones);
                swRecordatorios.setChecked(recordatorios);
                swSonido.setChecked(sonido);
                swVibracion.setChecked(vibracion);

                if ("OSCURO".equals(tema)) {
                    rbTemaOscuro.setChecked(true);
                } else {
                    rbTemaClaro.setChecked(true);
                }

            }
        } finally {
            cursor.close();
        }
    }

    private void configurarBotones() {

        btnGuardar.setOnClickListener(v -> guardarPreferencias());

        btnRestaurar.setOnClickListener(v -> {
            establecerValoresPredeterminados();
            guardarPreferencias();
        });
    }

    private void guardarPreferencias() {

        String tema;

        if (rbTemaOscuro.isChecked()) {
            tema = "OSCURO";
        } else {
            tema = "CLARO";
        }

        boolean guardado = dao.guardarPreferencias(
                idUsuarioActual,
                swNotificaciones.isChecked(),
                swRecordatorios.isChecked(),
                swSonido.isChecked(),
                swVibracion.isChecked(),
                tema
        );

        if (guardado) {

            aplicarTema(tema);

            Toast.makeText(
                    this,
                    "Preferencias guardadas",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();

        } else {

            Toast.makeText(
                    this,
                    "No se pudieron guardar las preferencias",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void aplicarTema(String tema) {

        if ("OSCURO".equals(tema)) {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );

        } else {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );
        }
    }
}
