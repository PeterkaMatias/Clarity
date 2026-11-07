
package com.conexa.clarity.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.PerfilMenorDao;
import com.conexa.clarity.data.local.PreferenciaUsuarioDao;
import com.conexa.clarity.ui.auth.LoginActivity;
import com.conexa.clarity.util.SesionUsuario;

public class PerfilesMenoresActivity extends AppCompatActivity {

    private EditText etNombreMenor;
    private EditText etEdadMenor;

    private LinearLayout contenedorMenores;
    private TextView tvSinMenores;

    private PerfilMenorDao perfilMenorDao;
    private PreferenciaUsuarioDao preferenciaDao;

    private int idUsuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        idUsuarioActual = SesionUsuario.obtenerId(this);

        if (idUsuarioActual == SesionUsuario.SIN_SESION) {

            Intent intent = new Intent(
                    this,
                    LoginActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
            return;
        }

        perfilMenorDao = new PerfilMenorDao(this);
        preferenciaDao = new PreferenciaUsuarioDao(this);

        if (!modoParentalDisponible()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_perfiles_menores);

        etNombreMenor = findViewById(R.id.etNombreMenor);
        etEdadMenor = findViewById(R.id.etEdadMenor);

        contenedorMenores = findViewById(
                R.id.contenedorMenores
        );

        tvSinMenores = findViewById(
                R.id.tvSinMenores
        );

        Button btnAgregarMenor = findViewById(
                R.id.btnAgregarMenor
        );

        btnAgregarMenor.setOnClickListener(
                v -> registrarMenor()
        );

        cargarMenores();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (preferenciaDao != null &&
                !modoParentalDisponible()) {
            finish();
        }
    }

    private boolean modoParentalDisponible() {

        boolean activo =
                preferenciaDao.estaModoParentalActivo(
                        idUsuarioActual
                );

        if (!activo) {
            Toast.makeText(
                    this,
                    "Activá el Modo Parental para continuar",
                    Toast.LENGTH_SHORT
            ).show();
        }

        return activo;
    }

    private void registrarMenor() {

        String nombre = etNombreMenor
                .getText().toString().trim();

        String edadTexto = etEdadMenor
                .getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombreMenor.setError(
                    "Ingresá el nombre del menor"
            );
            return;
        }

        if (edadTexto.isEmpty()) {
            etEdadMenor.setError("Ingresá la edad");
            return;
        }

        int edad;

        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            etEdadMenor.setError("Edad inválida");
            return;
        }

        if (edad < 0 || edad > 17) {
            etEdadMenor.setError(
                    "Ingresá una edad entre 0 y 17"
            );
            return;
        }

        if (!modoParentalDisponible()) {
            finish();
            return;
        }

        long resultado = perfilMenorDao.registrarMenor(
                idUsuarioActual,
                nombre,
                edad
        );

        if (resultado == -1) {
            Toast.makeText(
                    this,
                    "No se pudo registrar el perfil",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        Toast.makeText(
                this,
                "Perfil registrado correctamente",
                Toast.LENGTH_SHORT
        ).show();

        etNombreMenor.setText("");
        etEdadMenor.setText("");

        cargarMenores();
    }

    private void cargarMenores() {

        contenedorMenores.removeAllViews();

        int cantidad = 0;

        try (Cursor cursor =
                     perfilMenorDao.obtenerMenores(
                             idUsuarioActual
                     )) {

            int indiceId = cursor.getColumnIndexOrThrow(
                    "IdPerfilMenor"
            );

            int indiceNombre = cursor.getColumnIndexOrThrow(
                    "Nombre"
            );

            int indiceEdad = cursor.getColumnIndexOrThrow(
                    "Edad"
            );

            while (cursor.moveToNext()) {

                int idPerfilMenor = cursor.getInt(indiceId);
                String nombre = cursor.getString(indiceNombre);
                int edad = cursor.getInt(indiceEdad);

                // Contenedor de cada perfil
                LinearLayout fila = new LinearLayout(this);
                fila.setOrientation(LinearLayout.VERTICAL);

                int padding = dp(12);
                fila.setPadding(
                        padding,
                        padding,
                        padding,
                        padding
                );

                LinearLayout.LayoutParams parametrosFila =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                parametrosFila.bottomMargin = dp(12);
                fila.setLayoutParams(parametrosFila);

                fila.setBackgroundColor(
                        ContextCompat.getColor(
                                this,
                                R.color.blanco_superficie
                        )
                );

                // Nombre y edad
                TextView tvPerfil = new TextView(this);

                tvPerfil.setText(
                        nombre + " | " + edad + " años"
                );

                tvPerfil.setTextSize(16);
                tvPerfil.setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.gris_texto
                        )
                );

                fila.addView(tvPerfil);

                // Contenedor horizontal de botones
                LinearLayout botones = new LinearLayout(this);
                botones.setOrientation(LinearLayout.HORIZONTAL);

                // Botón Editar
                Button btnEditar = new Button(this);
                btnEditar.setText("Editar");

                // Botón Eliminar
                Button btnEliminar = new Button(this);
                btnEliminar.setText("Eliminar");

                botones.addView(
                        btnEditar,
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        )
                );

                botones.addView(
                        btnEliminar,
                        new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        )
                );

                fila.addView(botones);
                contenedorMenores.addView(fila);

                btnEditar.setOnClickListener(v ->
                        mostrarDialogoEditar(
                                idPerfilMenor,
                                nombre,
                                edad
                        )
                );

                btnEliminar.setOnClickListener(v ->
                        confirmarEliminar(
                                idPerfilMenor,
                                nombre
                        )
                );

                cantidad++;
            }
        }

        tvSinMenores.setVisibility(
                cantidad == 0 ? View.VISIBLE : View.GONE
        );
    }

    private void mostrarDialogoEditar(
            int idPerfilMenor,
            String nombreActual,
            int edadActual
    ) {

        if (!modoParentalDisponible()) {
            finish();
            return;
        }

        LinearLayout formulario = new LinearLayout(this);
        formulario.setOrientation(LinearLayout.VERTICAL);
        formulario.setPadding(
                dp(20),
                dp(12),
                dp(20),
                dp(4)
        );

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre del menor");
        inputNombre.setSingleLine(true);
        inputNombre.setText(nombreActual);

        EditText inputEdad = new EditText(this);
        inputEdad.setHint("Edad");
        inputEdad.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );
        inputEdad.setText(String.valueOf(edadActual));

        formulario.addView(inputNombre);
        formulario.addView(inputEdad);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("Editar perfil del menor")
                .setView(formulario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialogo.setOnShowListener(d -> {

            dialogo.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String nombre = inputNombre
                        .getText().toString().trim();

                String edadTexto = inputEdad
                        .getText().toString().trim();

                if (nombre.isEmpty()) {
                    inputNombre.setError(
                            "Ingresá un nombre"
                    );
                    return;
                }

                int edad;

                try {
                    edad = Integer.parseInt(edadTexto);
                } catch (NumberFormatException e) {
                    inputEdad.setError(
                            "Ingresá una edad válida"
                    );
                    return;
                }

                if (edad < 0 || edad > 17) {
                    inputEdad.setError(
                            "La edad debe estar entre 0 y 17"
                    );
                    return;
                }

                if (!modoParentalDisponible()) {
                    dialogo.dismiss();
                    finish();
                    return;
                }

                boolean actualizado =
                        perfilMenorDao.actualizarMenor(
                                idPerfilMenor,
                                idUsuarioActual,
                                nombre,
                                edad
                        );

                if (actualizado) {

                    Toast.makeText(
                            this,
                            "Perfil actualizado correctamente",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialogo.dismiss();
                    cargarMenores();

                } else {

                    Toast.makeText(
                            this,
                            "No se pudo actualizar el perfil",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        });

        dialogo.show();
    }

    private void confirmarEliminar(
            int idPerfilMenor,
            String nombre
    ) {

        if (!modoParentalDisponible()) {
            finish();
            return;
        }

        if (perfilMenorDao.tieneTareas(
                idPerfilMenor,
                idUsuarioActual
        )) {

            new AlertDialog.Builder(this)
                    .setTitle("No se puede eliminar")
                    .setMessage(
                            "El perfil de " + nombre +
                                    " tiene tareas asociadas. " +
                                    "Primero será necesario gestionar " +
                                    "esas tareas."
                    )
                    .setPositiveButton("Entendido", null)
                    .show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar perfil")
                .setMessage(
                        "¿Querés eliminar el perfil de " +
                                nombre + "? Esta acción no se puede deshacer."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(
                        "Eliminar",
                        (dialog, which) -> {

                            if (!modoParentalDisponible()) {
                                finish();
                                return;
                            }

                            boolean eliminado =
                                    perfilMenorDao.eliminarMenor(
                                            idPerfilMenor,
                                            idUsuarioActual
                                    );

                            if (eliminado) {

                                Toast.makeText(
                                        this,
                                        "Perfil eliminado correctamente",
                                        Toast.LENGTH_SHORT
                                ).show();

                                cargarMenores();

                            } else {

                                Toast.makeText(
                                        this,
                                        "No se pudo eliminar el perfil. " +
                                                "Verificá si tiene tareas asociadas.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                )
                .show();
    }

    private int dp(int valor) {
        return Math.round(
                valor * getResources()
                        .getDisplayMetrics().density
        );
    }
}
