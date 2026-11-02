package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.UsuarioLogros;

import java.util.ArrayList;
import java.util.List;

public class UsuarioLogrosDao {
    private final DatabaseHelper dbHelper;
    public UsuarioLogrosDao(Context context){
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    public long agregar(UsuarioLogros usuarioLogro) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("IdUsuario", usuarioLogro.getIdUsuario());
        cv.put("IdLogro", usuarioLogro.getIdLogro());
        cv.put("Fecha_obtenido", usuarioLogro.getFechaObtenido());

        return db.insert("UsuarioLogros", null, cv);
    }
    public boolean modificar(UsuarioLogros usuarioLogro) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("IdUsuario", usuarioLogro.getIdUsuario());
        cv.put("IdLogro", usuarioLogro.getIdLogro());
        cv.put("Fecha_obtenido", usuarioLogro.getFechaObtenido());

        int filas = db.update(
                "UsuarioLogros",
                cv,
                "IdUsuarioLogro = ?",
                new String[]{String.valueOf(usuarioLogro.getIdUsuarioLogro())}
        );

        return filas > 0;
    }
    public boolean eliminar(int idUsuarioLogro) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                "UsuarioLogros",
                "IdUsuarioLogro = ?",
                new String[]{String.valueOf(idUsuarioLogro)}
        );

        return filas > 0;
    }
    public List<UsuarioLogros> obtenerPorUsuario(int idUsuario) {
        List<UsuarioLogros> lista = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM UsuarioLogros " +
                "WHERE IdUsuario = ? " +
                "ORDER BY Fecha_obtenido DESC";

        try (Cursor c = db.rawQuery(
                sql,
                new String[]{String.valueOf(idUsuario)}
        )) {
            while (c.moveToNext()) {
                UsuarioLogros usuarioLogro = new UsuarioLogros(
                        c.getInt(c.getColumnIndexOrThrow("IdUsuarioLogro")),
                        c.getInt(c.getColumnIndexOrThrow("IdUsuario")),
                        c.getInt(c.getColumnIndexOrThrow("IdLogro")),
                        c.getString(c.getColumnIndexOrThrow("Fecha_obtenido"))
                );

                lista.add(usuarioLogro);
            }
        }

        return lista;
    }

}
