package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Usuario;

import java.util.Locale;

public class UsuarioDao {

    // Códigos que devuelve registrar() cuando algo sale mal
    public static final long ERROR = -1;
    public static final long EMAIL_DUPLICADO = -2;

    private final DatabaseHelper dbHelper;

    public UsuarioDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // ¿Ya hay alguien registrado con este correo?
    public boolean emailExiste(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT 1 FROM Usuarios WHERE Email = ? LIMIT 1",
                new String[]{email.trim()})) {
            return c.moveToFirst();
        }
    }

    // Crea el usuario. Devuelve su id, o ERROR / EMAIL_DUPLICADO
    public long registrar(String nombre, String apellido, String email,
                          String password, String tipoPerfil) {
        if (emailExiste(email)) return EMAIL_DUPLICADO;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long idUsuario = ERROR;

        try {
            db.beginTransaction();
            ContentValues usuario = new ContentValues();

            usuario.put("Nombre", nombre.trim());
            usuario.put("Apellido", apellido.trim());
            usuario.put("Email", email.trim().toLowerCase(Locale.ROOT));
            usuario.put("Password", password);
            usuario.put("TipoPerfil", tipoPerfil);

            idUsuario = db.insertOrThrow("Usuarios", null, usuario);

            boolean modoParental = Usuario.PERFIL_PARENTAL.equals(tipoPerfil);

            // Crear preferencias del usuario
            ContentValues preferencias = new ContentValues();

            preferencias.put("IdUsuario", idUsuario);
            preferencias.put("ModoParental", modoParental ? 1 : 0);

            db.insertOrThrow("PreferenciaUsuario", null, preferencias);

            db.setTransactionSuccessful();

        } catch (SQLiteConstraintException e) {
            return EMAIL_DUPLICADO;

        } catch (RuntimeException e) {
            return ERROR;

        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
        return idUsuario;
    }


    // Devuelve el usuario si correo y contraseña son correctos; si no, null
    public Usuario autenticar(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT IdUsuario, Nombre, Apellido, Email, Password, TipoPerfil " +
                        "FROM Usuarios WHERE Email = ?",
                new String[]{email.trim()})) {

            if (!c.moveToFirst()) return null; // no existe ese correo

            String passwordGuardada = c.getString(c.getColumnIndexOrThrow("Password"));
            if (!passwordGuardada.equals(password)) return null; // clave incorrecta

            return mapear(c);
        }
    }

    // Busca un usuario por su id
    public Usuario obtenerPorId(long idUsuario) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT IdUsuario, Nombre, Apellido, Email, Password, TipoPerfil " +
                        "FROM Usuarios WHERE IdUsuario = ?",
                new String[]{String.valueOf(idUsuario)})) {
            return c.moveToFirst() ? mapear(c) : null;
        }
    }

    // Convierte una fila de la base en un objeto Usuario
    private Usuario mapear(Cursor c) {
        int puntosIndex = c.getColumnIndex("PuntosTotales");
        int nivelIndex = c.getColumnIndex("Nivel");

        int puntos = (puntosIndex != -1) ? c.getInt(puntosIndex) : 0;
        int nivel = (nivelIndex != -1) ? c.getInt(nivelIndex) : 1;

        return new Usuario(
                c.getInt(c.getColumnIndexOrThrow("IdUsuario")),
                c.getString(c.getColumnIndexOrThrow("Nombre")),
                c.getString(c.getColumnIndexOrThrow("Apellido")),
                c.getString(c.getColumnIndexOrThrow("Email")),
                c.getString(c.getColumnIndexOrThrow("TipoPerfil")),
                puntos,
                nivel);
    }
}