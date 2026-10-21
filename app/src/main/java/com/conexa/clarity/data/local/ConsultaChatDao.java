package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ConsultaChatDao {

    private final DatabaseHelper dbHelper;

    public ConsultaChatDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long guardarConsulta(
            int idUsuario,
            String consulta,
            String respuesta) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("IdUsuario", idUsuario);
        values.put("Consulta", consulta);
        values.put("Respuesta", respuesta);

        return db.insert(
                "ConsultasChat",
                null,
                values
        );
    }

    public void guardarFeedback(long idConsulta, boolean positivo) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("Feedback", positivo ? 1 : 0);

        db.update(
                "ConsultasChat",
                values,
                "IdConsulta = ?",
                new String[]{String.valueOf(idConsulta)}
        );
    }
}