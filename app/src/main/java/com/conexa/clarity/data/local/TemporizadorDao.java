package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Temporizador;

import java.util.ArrayList;
import java.util.List;

public class TemporizadorDao {
    private final DatabaseHelper dbHelper;
    public TemporizadorDao(Context context){
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    public long agregar(Temporizador temporizador){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("IdUsuario", temporizador.getIdUsuario());

        if(temporizador.getIdTarea() != null){
            cv.put("IdTarea", temporizador.getIdTarea());
        }else{
            cv.putNull("IdTarea");
        }

        cv.put("Duracion", temporizador.getDuracion());
        cv.put("Fecha_inicio", temporizador.getFechaInicio());
        cv.put("Fecha_fin", temporizador.getFechaFin());
        cv.put("Estado", temporizador.getEstado());

        return db.insert("Temporizador", null, cv);


    }
    public boolean modificar(Temporizador temporizador){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("IdUsuario", temporizador.getIdUsuario());

        if(temporizador.getIdTarea() != null){
            cv.put("IdTarea", temporizador.getIdTarea());
        }else{
            cv.putNull("IdTarea");
        }

        cv.put("Duracion", temporizador.getDuracion());
        cv.put("Fecha_inicio", temporizador.getFechaInicio());
        cv.put("Fecha_fin", temporizador.getFechaFin());
        cv.put("Estado", temporizador.getEstado());

        int filas = db.update(
                "Temporizador",
                cv,
                "IdTemporizador = ?",
                new String[]{String.valueOf(temporizador.getIdTemporizador())}
        );
        return filas > 0;
    }
    public boolean eliminar(int idTemporizador){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                "Temporizador",
                "idTemporizador = ?",
                new String[]{String.valueOf(idTemporizador)}
        );
        return filas > 0 ;
    }
    public List<Temporizador> obtenerPorUsuario(int idUsuario) {
        List<Temporizador> lista = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM Temporizador " +
                "WHERE IdUsuario = ? " +
                "ORDER BY Fecha_inicio DESC";

        try (Cursor c = db.rawQuery(
                sql,
                new String[]{String.valueOf(idUsuario)}
        )) {
            while (c.moveToNext()) {
                Temporizador t = new Temporizador(
                        c.getInt(c.getColumnIndexOrThrow("IdTemporizador")),
                        c.getInt(c.getColumnIndexOrThrow("IdUsuario")),
                        c.isNull(c.getColumnIndexOrThrow("IdTarea"))
                                ? null
                                : c.getInt(c.getColumnIndexOrThrow("IdTarea")),
                        c.getInt(c.getColumnIndexOrThrow("Duracion")),
                        c.getString(c.getColumnIndexOrThrow("Fecha_inicio")),
                        c.getString(c.getColumnIndexOrThrow("Fecha_fin")),
                        c.getString(c.getColumnIndexOrThrow("Estado"))
                );

                lista.add(t);
            }
        }

        return lista;
    }
}
