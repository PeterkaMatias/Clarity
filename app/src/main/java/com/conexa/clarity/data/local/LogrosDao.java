package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Logros;

import java.util.ArrayList;
import java.util.List;

public class LogrosDao {
    private final DatabaseHelper dbHelper;
    public LogrosDao(Context context){
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long agregar(Logros logro) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("Nombre", logro.getNombre());
        cv.put("Descripcion", logro.getDescripcion());
        cv.put("Requisito", logro.getRequisito());

        return db.insert("Logros", null, cv);
    }
    public boolean modificar(Logros logro) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("Nombre", logro.getNombre());
        cv.put("Descripcion", logro.getDescripcion());
        cv.put("Requisito", logro.getRequisito());

        int filas = db.update(
                "Logros",
                cv,
                "IdLogro = ?",
                new String[]{String.valueOf(logro.getIdLogro())}
        );

        return filas > 0;
    }
    public boolean eliminar(int idLogro) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                "Logros",
                "IdLogro = ?",
                new String[]{String.valueOf(idLogro)}
        );

        return filas > 0;
    }
    public List<Logros> obtenerTodos() {
        List<Logros> lista = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT * FROM Logros ORDER BY IdLogro ASC";

        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                Logros logro = new Logros(
                        c.getInt(c.getColumnIndexOrThrow("IdLogro")),
                        c.getString(c.getColumnIndexOrThrow("Nombre")),
                        c.getString(c.getColumnIndexOrThrow("Descripcion")),
                        c.getString(c.getColumnIndexOrThrow("Requisito"))
                );

                lista.add(logro);
            }
        }

        return lista;
    }
    public List<Integer> obtenerLogrosDesbloqueados(int idUsuario) {
        List<Integer> lista = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT IdLogro FROM UsuarioLogros WHERE IdUsuario = ?";

        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(idUsuario)})) {
            while (c.moveToNext()) {
                lista.add(c.getInt(0));
            }
        }

        return lista;
    }
    public boolean desbloquear(int idUsuario, int idLogro, String fecha) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("IdUsuario", idUsuario);
        cv.put("IdLogro", idLogro);
        cv.put("Fecha_obtenido", fecha);

        long resultado = db.insertWithOnConflict(
                "UsuarioLogros",
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        return resultado != -1;
    }
}
