package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Recompensas;

import java.util.ArrayList;
import java.util.List;

public class RecompensasDao {
    private final DatabaseHelper dbHelper;

    public RecompensasDao(Context context){
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long agregar(Recompensas recompensa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("IdUsuario", recompensa.getIdUsuario());

        if (recompensa.getIdTarea() != null) {
            cv.put("IdTarea", recompensa.getIdTarea());
        } else {
            cv.putNull("IdTarea");
        }

        cv.put("Referencia", recompensa.getReferencia());
        cv.put("Puntos", recompensa.getPuntos());
        cv.put("Tipo", recompensa.getTipo());
        cv.put("Descripcion", recompensa.getDescripcion());
        cv.put("Fecha", recompensa.getFecha());

        return db.insert("Recompensas", null, cv);
    }
    public boolean modificar(Recompensas recompensa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("IdUsuario", recompensa.getIdUsuario());

        if (recompensa.getIdTarea() != null) {
            cv.put("IdTarea", recompensa.getIdTarea());
        } else {
            cv.putNull("IdTarea");
        }

        cv.put("Referencia", recompensa.getReferencia());
        cv.put("Puntos", recompensa.getPuntos());
        cv.put("Tipo", recompensa.getTipo());
        cv.put("Descripcion", recompensa.getDescripcion());
        cv.put("Fecha", recompensa.getFecha());

        int filas = db.update(
                "Recompensas",
                cv,
                "IdRecompensa = ?",
                new String[]{String.valueOf(recompensa.getIdRecompensa())}
        );

        return filas > 0;
    }
    public boolean eliminar(int idRecompensa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                "Recompensas",
                "IdRecompensa = ?",
                new String[]{String.valueOf(idRecompensa)}
        );

        return filas > 0;
    }
    public List<Recompensas> obtenerPorUsuario(int idUsuario) {
        List<Recompensas> lista = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM Recompensas " +
                "WHERE IdUsuario = ? ORDER BY Fecha DESC";

        try (Cursor c = db.rawQuery(
                sql,
                new String[]{String.valueOf(idUsuario)}
        )) {
            while (c.moveToNext()) {
                int indiceTarea = c.getColumnIndexOrThrow("IdTarea");
                String referencia = c.getString(
                        c.getColumnIndexOrThrow("Referencia")
                );

                Recompensas recompensa = new Recompensas(
                        c.getInt(c.getColumnIndexOrThrow("IdRecompensa")),
                        c.getInt(c.getColumnIndexOrThrow("IdUsuario")),
                        c.isNull(indiceTarea) ? null : c.getInt(indiceTarea),
                        c.getInt(c.getColumnIndexOrThrow("Puntos")),
                        c.getString(c.getColumnIndexOrThrow("Tipo")),
                        c.getString(c.getColumnIndexOrThrow("Descripcion")),
                        c.getString(c.getColumnIndexOrThrow("Fecha"))
                );
                recompensa.setReferencia(referencia);
                lista.add(recompensa);
            }
        }

        return lista;
    }
    public boolean otorgarPuntos(
            int idUsuario,
            Integer idTarea,
            int puntos,
            String tipo,
            String descripcion,
            String referencia,
            String fecha) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            // Comprobar si esta acción ya fue recompensada.
            Cursor cursor = db.rawQuery(
                    "SELECT 1 FROM Recompensas " +
                            "WHERE IdUsuario = ? AND Tipo = ? AND Referencia = ?",
                    new String[]{
                            String.valueOf(idUsuario),
                            tipo,
                            referencia
                    }
            );

            boolean existe = cursor.moveToFirst();
            cursor.close();

            if (existe) {
                return false;
            }

            // Registrar la recompensa.
            ContentValues cv = new ContentValues();
            cv.put("IdUsuario", idUsuario);

            if (idTarea != null) {
                cv.put("IdTarea", idTarea);
            } else {
                cv.putNull("IdTarea");
            }

            cv.put("Puntos", puntos);
            cv.put("Tipo", tipo);
            cv.put("Descripcion", descripcion);
            cv.put("Referencia", referencia);
            cv.put("Fecha", fecha);

            long resultado = db.insert("Recompensas", null, cv);

            if (resultado == -1) {
                return false;
            }

            // Actualizar los puntos totales del usuario.
            db.execSQL(
                    "UPDATE Usuarios SET PuntosTotales = PuntosTotales + ? " +
                            "WHERE IdUsuario = ?",
                    new Object[]{puntos, idUsuario}
            );

            // Obtener los puntos acumulados.
            Cursor consultaPuntos = db.rawQuery(
                    "SELECT PuntosTotales FROM Usuarios WHERE IdUsuario = ?",
                    new String[]{String.valueOf(idUsuario)}
            );

            int puntosTotales = 0;

            if (consultaPuntos.moveToFirst()) {
                puntosTotales = consultaPuntos.getInt(0);
            }

            consultaPuntos.close();

            // Calcular el nivel.
            int nivel;

            if (puntosTotales >= 1000) {
                nivel = 5;
            } else if (puntosTotales >= 601) {
                nivel = 4;
            } else if (puntosTotales >= 301) {
                nivel = 3;
            } else if (puntosTotales >= 101) {
                nivel = 2;
            } else {
                nivel = 1;
            }

            db.execSQL(
                    "UPDATE Usuarios SET Nivel = ? WHERE IdUsuario = ?",
                    new Object[]{nivel, idUsuario}
            );

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }
    public int obtenerPuntosTotales(int idUsuario) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT PuntosTotales FROM Usuarios WHERE IdUsuario = ?",
                new String[]{String.valueOf(idUsuario)}
        );

        int puntos = 0;

        if (cursor.moveToFirst()) {
            puntos = cursor.getInt(0);
        }

        cursor.close();
        return puntos;
    }
}
