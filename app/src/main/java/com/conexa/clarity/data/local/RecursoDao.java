package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;

import com.conexa.clarity.data.model.Recurso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecursoDao {
    private final DatabaseHelper dbHelper;
    private final LogrosDao logrosDao;

    public RecursoDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.logrosDao = new LogrosDao(context);
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

            long resultado = db.insert("Favoritos", null, values);

            if (resultado != -1) {
                verificarEspecialistaTDAH(idUsuario);
            }

            return resultado != -1;

        } else {
            return db.delete("Favoritos", "IdUsuario = ? AND IdRecurso = ?",
                    new String[]{String.valueOf(idUsuario), String.valueOf(idRecurso)}) > 0;
        }
    }
    public boolean registrarLectura(int idUsuario, int idRecurso){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("IdUsuario", idUsuario);
        valores.put("IdRecurso", idRecurso);
        valores.put(
                "Fecha",
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date())
        );
        long resultado = db.insertWithOnConflict(
                "RecursosLeidos",
                null,
                valores,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        if (resultado != -1) {
            verificarEspecialistaTDAH(idUsuario);
        }

        return resultado != -1;
    }
    // LOGRO 9: Especialista en TDAH
    public void verificarEspecialistaTDAH(int idUsuario) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT COUNT(DISTINCT f.IdRecurso) " +
                "FROM Favoritos f " +
                "INNER JOIN RecursosLeidos r " +
                "ON f.IdRecurso = r.IdRecurso " +
                "AND f.IdUsuario = r.IdUsuario " +
                "WHERE f.IdUsuario = ?";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario)
        })) {

            if (c.moveToFirst() && c.getInt(0) >= 3) {

                String fecha = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

                logrosDao.desbloquear(idUsuario, 9, fecha);
            }
        }
    }
}