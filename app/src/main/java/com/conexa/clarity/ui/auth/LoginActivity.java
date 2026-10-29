package com.conexa.clarity.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.util.SesionUsuario;
import com.conexa.clarity.R;
import com.conexa.clarity.data.local.UsuarioDao;
import com.conexa.clarity.data.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private UsuarioDao usuarioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usuarioDao = new UsuarioDao(this);

        // Si ya hay un usuario con sesión (y existe en la base), vamos directo al dashboard
        int idGuardado = SesionUsuario.obtenerId(this);
        if (idGuardado != SesionUsuario.SIN_SESION && usuarioDao.obtenerPorId(idGuardado) != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnIngresar).setOnClickListener(v -> iniciarSesion());
        findViewById(R.id.btnIrRegistro).setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void iniciarSesion() {
        // 1. Leer lo que escribió el usuario
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // 2. Validar que no haya campos vacíos
        if (email.isEmpty()) {
            etEmail.setError("Ingresá tu correo");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingresá tu contraseña");
            return;
        }

        // 3. Preguntarle a la base si el correo y la contraseña son correctos
        Usuario usuario = usuarioDao.autenticar(email, password);

        // 4. Ver qué pasó
        if (usuario == null) {
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "¡Hola, " + usuario.getNombre() + "!", Toast.LENGTH_SHORT).show();

            // Guardamos quién es y pasamos al dashboard
            SesionUsuario.guardar(this, usuario.getIdUsuario());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}