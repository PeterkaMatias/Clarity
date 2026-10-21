package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class TareaDao {

    private final DatabaseHelper dbHelper;

    public TareaDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertarTarea(int idUsuario, String titulo) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("IdUsuario", idUsuario);
        values.put("Titulo", titulo);
        values.put("Completada", 0);
        values.put("CreadaPorIA", 1);

        return db.insert("Tareas", null, values);
    }
}
