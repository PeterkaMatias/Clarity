
package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PreferenciaUsuarioDao {

    private final DatabaseHelper dbHelper;

    public PreferenciaUsuarioDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean guardarPreferencias(
            int idUsuario,
            boolean notificaciones,
            boolean recordatorios,
            boolean sonido,
            boolean vibracion,
            String tema,
            boolean modoParental
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();

        valores.put("Notificaciones", notificaciones ? 1 : 0);
        valores.put("Recordatorios", recordatorios ? 1 : 0);
        valores.put("Sonido", sonido ? 1 : 0);
        valores.put("Vibracion", vibracion ? 1 : 0);
        valores.put("Tema", tema);
        valores.put("ModoParental", modoParental ? 1 : 0);

        int filasActualizadas = db.update(
                "PreferenciaUsuario",
                valores,
                "IdUsuario = ?",
                new String[]{String.valueOf(idUsuario)}
        );

        if (filasActualizadas > 0) {
            return true;
        }

        // Si todavía no existen, crear el registro
        valores.put("IdUsuario", idUsuario);

        long resultado = db.insert(
                "PreferenciaUsuario",
                null,
                valores
        );

        return resultado != -1;
    }

    public Cursor obtenerPreferencias(int idUsuario) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(
                "PreferenciaUsuario",
                null,
                "IdUsuario = ?",
                new String[]{String.valueOf(idUsuario)},
                null,
                null,
                null
        );
    }

    public boolean estaModoParentalActivo(int idUsuario) {

        try (Cursor cursor = obtenerPreferencias(idUsuario)) {

            if (cursor.moveToFirst()) {

                int indice = cursor.getColumnIndex("ModoParental");

                if (indice >= 0) {
                    return cursor.getInt(indice) == 1;
                }
            }
        }

        return false;
    }

    public boolean actualizarModoParental(
            int idUsuario,
            boolean modoParental
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("ModoParental", modoParental ? 1 : 0);

        int filasActualizadas = db.update(
                "PreferenciaUsuario",
                valores,
                "IdUsuario = ?",
                new String[]{String.valueOf(idUsuario)}
        );

        if (filasActualizadas > 0) {
            return true;
        }

        // Crear las preferencias con sus valores por defecto
        valores.put("IdUsuario", idUsuario);

        long resultado = db.insert(
                "PreferenciaUsuario",
                null,
                valores
        );

        return resultado != -1;
    }
}
