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

        cv.put("Puntos", recompensa.getPuntos());
        cv.put("Tipo", recompensa.getTipo());
        cv.put("Descripcion", recompensa.getDescripcion());
        cv.put("Fecha", recompensa.getFecha());

        return db.insert("Recompenzas", null, cv);
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

        cv.put("Puntos", recompensa.getPuntos());
        cv.put("Tipo", recompensa.getTipo());
        cv.put("Descripcion", recompensa.getDescripcion());
        cv.put("Fecha", recompensa.getFecha());

        int filas = db.update(
                "Recompenzas",
                cv,
                "IdRecompenza = ?",
                new String[]{String.valueOf(recompensa.getIdRecompenza())}
        );

        return filas > 0;
    }
    public boolean eliminar(int idRecompenza) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                "Recompenzas",
                "IdRecompenza = ?",
                new String[]{String.valueOf(idRecompenza)}
        );

        return filas > 0;
    }
    public List<Recompensas> obtenerPorUsuario(int idUsuario) {
        List<Recompensas> lista = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM Recompenzas " +
                "WHERE IdUsuario = ? ORDER BY Fecha DESC";

        try (Cursor c = db.rawQuery(
                sql,
                new String[]{String.valueOf(idUsuario)}
        )) {
            while (c.moveToNext()) {
                int indiceTarea = c.getColumnIndexOrThrow("IdTarea");

                Recompensas recompensa = new Recompensas(
                        c.getInt(c.getColumnIndexOrThrow("IdRecompenza")),
                        c.getInt(c.getColumnIndexOrThrow("IdUsuario")),
                        c.isNull(indiceTarea) ? null : c.getInt(indiceTarea),
                        c.getInt(c.getColumnIndexOrThrow("Puntos")),
                        c.getString(c.getColumnIndexOrThrow("Tipo")),
                        c.getString(c.getColumnIndexOrThrow("Descripcion")),
                        c.getString(c.getColumnIndexOrThrow("Fecha"))
                );

                lista.add(recompensa);
            }
        }

        return lista;
    }
}
