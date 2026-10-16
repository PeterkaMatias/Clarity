package com.conexa.clarity.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.UsuarioDao;
import com.conexa.clarity.data.model.Usuario;
import com.conexa.clarity.ui.resources.RecursosActivity;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etEmail, etPassword;
    private CheckBox cbParental;
    private UsuarioDao usuarioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        usuarioDao = new UsuarioDao(this);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbParental = findViewById(R.id.cbParental);

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrar());
    }

    private void registrar() {
        // 1. Leer lo que escribió el usuario
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // 2. Validar: si algo está mal, se muestra el error y no seguimos
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresá tu nombre");
            return;
        }
        if (apellido.isEmpty()) {
            etApellido.setError("Ingresá tu apellido");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresá un correo válido");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Usá al menos 6 caracteres");
            return;
        }

        // 3. Guardar en la base
        String perfil = cbParental.isChecked() ? Usuario.PERFIL_PARENTAL : Usuario.PERFIL_GENERAL;
        long id = usuarioDao.registrar(nombre, apellido, email, password, perfil);

        // 4. Ver qué pasó
        if (id == UsuarioDao.EMAIL_DUPLICADO) {
            etEmail.setError("Este correo ya está registrado");
        } else if (id == UsuarioDao.ERROR) {
            Toast.makeText(this, "No pudimos crear la cuenta. Probá de nuevo.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "¡Cuenta creada! Bienvenido/a, " + nombre, Toast.LENGTH_SHORT).show();

            // Pasamos a Recursos y le avisamos qué usuario es
            Intent intent = new Intent(this, RecursosActivity.class);
            intent.putExtra("id_usuario", (int) id);
            startActivity(intent);
            finish();
        }
    }
}