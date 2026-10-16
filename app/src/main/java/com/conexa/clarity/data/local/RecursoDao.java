package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Recurso;

import java.util.ArrayList;
import java.util.List;

public class RecursoDao {
    private final DatabaseHelper dbHelper;

    public RecursoDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    public List<Recurso> obtenerRecursos(int idUsuario, String categoriaFiltro, String queryTexto) {
        List<Recurso> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder query = new StringBuilder(
                "SELECT r.IdRecursos, r.Titulo, r.Contenido, r.Categoria, r.TiempoLecturaMin, " +
                        "CASE WHEN f.IdFavorito IS NOT NULL THEN 1 ELSE 0 END AS EsFavorito " +
                        "FROM Recursos r " +
                        "LEFT JOIN Favoritos f ON r.IdRecursos = f.IdRecurso AND f.IdUsuario = ? " +
                        "WHERE 1=1 "
        );

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(idUsuario));

        if (categoriaFiltro != null && !categoriaFiltro.equalsIgnoreCase("TODAS")) {
            query.append("AND r.Categoria = ? ");
            args.add(categoriaFiltro);
        }

        if (queryTexto != null && !queryTexto.trim().isEmpty()) {
            query.append("AND (r.Titulo LIKE ? OR r.Contenido LIKE ?) ");
            String wildcard = "%" + queryTexto.trim() + "%";
            args.add(wildcard);
            args.add(wildcard);
        }

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("IdRecursos"));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow("Titulo"));
                String contenido = cursor.getString(cursor.getColumnIndexOrThrow("Contenido"));
                String categoria = cursor.getString(cursor.getColumnIndexOrThrow("Categoria"));
                int tiempoLectura = cursor.getInt(cursor.getColumnIndexOrThrow("TiempoLecturaMin"));
                boolean esFav = cursor.getInt(cursor.getColumnIndexOrThrow("EsFavorito")) == 1;

                lista.add(new Recurso(id, titulo, contenido, categoria, tiempoLectura, esFav));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // Alternar favorito (guardar o remover)
    public boolean alternarFavorito(int idUsuario, int idRecurso, boolean marcar) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (marcar) {
            ContentValues values = new ContentValues();
            values.put("IdUsuario", idUsuario);
            values.put("IdRecurso", idRecurso);
            return db.insert("Favoritos", null, values) != -1;
        } else {
            return db.delete("Favoritos", "IdUsuario = ? AND IdRecurso = ?",
                    new String[]{String.valueOf(idUsuario), String.valueOf(idRecurso)}) > 0;
        }
    }
}