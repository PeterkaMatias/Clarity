package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Tarea;

import java.util.ArrayList;
import java.util.List;

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

    public List<Tarea> obtenerTareasPorUsuario(int idUsuario) {
        List<Tarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Leemos las columnas que definió tu compañero
        Cursor cursor = db.query(
                "Tareas",
                null,
                "IdUsuario = ?",
                new String[]{String.valueOf(idUsuario)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("IdTareas"));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow("Titulo"));
                boolean completada = cursor.getInt(cursor.getColumnIndexOrThrow("Completada")) == 1;
                boolean creadaPorIA = cursor.getInt(cursor.getColumnIndexOrThrow("CreadaPorIA")) == 1;

                lista.add(new Tarea(id, idUsuario, titulo, completada, creadaPorIA));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return lista;
    }

    public int actualizarCompletada(int idTarea, boolean completada) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Completada", completada ? 1 : 0);

        return db.update("Tareas", values, "IdTareas = ?", new String[]{String.valueOf(idTarea)});
    }
}