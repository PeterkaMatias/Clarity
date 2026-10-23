
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
            String tema
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();

        valores.put("IdUsuario", idUsuario);
        valores.put("Notificaciones", notificaciones ? 1 : 0);
        valores.put("Recordatorios", recordatorios ? 1 : 0);
        valores.put("Sonido", sonido ? 1 : 0);
        valores.put("Vibracion", vibracion ? 1 : 0);
        valores.put("Tema", tema);

        long resultado = db.insertWithOnConflict(
                "PreferenciaUsuario",
                null,
                valores,
                SQLiteDatabase.CONFLICT_REPLACE
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
}
