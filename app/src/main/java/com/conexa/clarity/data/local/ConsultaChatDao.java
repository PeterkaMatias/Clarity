package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.ConsultaChat;

import java.util.ArrayList;
import java.util.List;

public class ConsultaChatDao {

    private final DatabaseHelper dbHelper;

    public ConsultaChatDao(
            Context context
    ) {
        dbHelper =
                DatabaseHelper.getInstance(
                        context
                );
    }

    public long guardarConsulta(
            int idUsuario,
            String consulta,
            String respuesta
    ) {

        if (idUsuario <= 0
                || consulta == null
                || consulta.trim().isEmpty()
                || respuesta == null
                || respuesta.trim().isEmpty()) {

            return -1L;
        }

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "IdUsuario",
                idUsuario
        );

        values.put(
                "Consulta",
                consulta.trim()
        );

        values.put(
                "Respuesta",
                respuesta.trim()
        );

        return db.insert(
                "ConsultasChat",
                null,
                values
        );
    }

    public List<ConsultaChat> obtenerHistorial(
            int idUsuario
    ) {

        List<ConsultaChat> historial =
                new ArrayList<>();

        if (idUsuario <= 0) {
            return historial;
        }

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        String consultaSql =
                "SELECT IdConsulta, IdUsuario, Consulta, " +
                        "Respuesta, Feedback, Fecha " +
                        "FROM ConsultasChat " +
                        "WHERE IdUsuario = ? " +
                        "ORDER BY IdConsulta DESC";

        try (
                Cursor cursor = db.rawQuery(
                        consultaSql,
                        new String[]{
                                String.valueOf(
                                        idUsuario
                                )
                        }
                )
        ) {

            while (cursor.moveToNext()) {

                int indiceFeedback =
                        cursor.getColumnIndexOrThrow(
                                "Feedback"
                        );

                Integer feedback =
                        cursor.isNull(indiceFeedback)
                                ? null
                                : cursor.getInt(
                                indiceFeedback
                        );

                ConsultaChat consultaChat =
                        new ConsultaChat(
                                cursor.getLong(
                                        cursor.getColumnIndexOrThrow(
                                                "IdConsulta"
                                        )
                                ),
                                cursor.getInt(
                                        cursor.getColumnIndexOrThrow(
                                                "IdUsuario"
                                        )
                                ),
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                                "Consulta"
                                        )
                                ),
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                                "Respuesta"
                                        )
                                ),
                                feedback,
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                                "Fecha"
                                        )
                                )
                        );

                historial.add(
                        consultaChat
                );
            }
        }

        return historial;
    }

    public boolean eliminarConsulta(
            long idConsulta
    ) {

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        int filas =
                db.delete(
                        "ConsultasChat",
                        "IdConsulta = ?",
                        new String[]{
                                String.valueOf(
                                        idConsulta
                                )
                        }
                );

        return filas > 0;
    }

    public void guardarFeedback(
            long idConsulta,
            boolean positivo
    ) {

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "Feedback",
                positivo ? 1 : 0
        );

        db.update(
                "ConsultasChat",
                values,
                "IdConsulta = ?",
                new String[]{
                        String.valueOf(
                                idConsulta
                        )
                }
        );
    }
}