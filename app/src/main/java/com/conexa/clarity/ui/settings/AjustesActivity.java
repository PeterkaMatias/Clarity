
package com.conexa.clarity.ui.settings;

import android.content.Intent;
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
import com.conexa.clarity.util.SesionUsuario;
import com.conexa.clarity.ui.auth.LoginActivity;
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
    private SwitchCompat swModoParental;

    private Button btnGuardar;
    private Button btnRestaurar;
    private Button btnCerrarSesion;
    private int idUsuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        // Obtener el ID del usuario autenticado
        idUsuarioActual = SesionUsuario.obtenerId(this);

        // Verificar que exista una sesión activa
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
        swModoParental = findViewById(R.id.swModoParental);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnRestaurar = findViewById(R.id.btnRestaurar);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
    }

    private void establecerValoresPredeterminados() {

        rbTemaClaro.setChecked(true);

        swNotificaciones.setChecked(true);
        swRecordatorios.setChecked(true);
        swSonido.setChecked(true);
        swVibracion.setChecked(true);
    }

    private void cargarPreferencias() {

        try (Cursor cursor = dao.obtenerPreferencias(idUsuarioActual)) {

            if (cursor.moveToFirst()) {

                boolean notificaciones = cursor.getInt(
                        cursor.getColumnIndexOrThrow("Notificaciones")
                ) == 1;

                boolean recordatorios = cursor.getInt(
                        cursor.getColumnIndexOrThrow("Recordatorios")
                ) == 1;

                boolean sonido = cursor.getInt(
                        cursor.getColumnIndexOrThrow("Sonido")
                ) == 1;

                boolean vibracion = cursor.getInt(
                        cursor.getColumnIndexOrThrow("Vibracion")
                ) == 1;

                boolean modoParental = cursor.getInt(
                        cursor.getColumnIndexOrThrow("ModoParental")
                ) == 1;

                String tema = cursor.getString(cursor.getColumnIndexOrThrow("Tema"));

                swNotificaciones.setChecked(notificaciones);
                swRecordatorios.setChecked(recordatorios);
                swSonido.setChecked(sonido);
                swVibracion.setChecked(vibracion);
                swModoParental.setChecked(modoParental);

                if ("OSCURO".equals(tema)) {
                    rbTemaOscuro.setChecked(true);
                } else {
                    rbTemaClaro.setChecked(true);
                }
            }
        }
    }

    private void configurarBotones() {

        btnGuardar.setOnClickListener(v -> guardarPreferencias());

        btnRestaurar.setOnClickListener(v -> {
            establecerValoresPredeterminados();
            guardarPreferencias();
        });

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion(){
        // 1. Limpiar la sesion en el celular
        SesionUsuario.cerrar(this);
        Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show();
        // 2. Redirigir a LoginActivity y limpiar el historial de navegacion
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void guardarPreferencias() {

        String tema = rbTemaOscuro.isChecked() ? "OSCURO" : "CLARO";

        boolean modoParental = swModoParental.isChecked();

        boolean guardado = dao.guardarPreferencias(
                idUsuarioActual,
                swNotificaciones.isChecked(),
                swRecordatorios.isChecked(),
                swSonido.isChecked(),
                swVibracion.isChecked(),
                tema,
                modoParental
        );

        if (guardado) {

            Toast.makeText(
                    this,
                    "Preferencias guardadas",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);

            aplicarTema(tema);

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
