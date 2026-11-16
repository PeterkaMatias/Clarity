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
    private final Context context;

    public LogrosDao(
            Context context
    ) {
        this.context =
                context.getApplicationContext();

        this.dbHelper =
                DatabaseHelper.getInstance(
                        context
                );
    }

    public long agregar(
            Logros logro
    ) {

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        ContentValues cv =
                new ContentValues();

        cv.put(
                "Nombre",
                logro.getNombre()
        );

        cv.put(
                "Descripcion",
                logro.getDescripcion()
        );

        cv.put(
                "Requisito",
                logro.getRequisito()
        );

        return db.insert(
                "Logros",
                null,
                cv
        );
    }

    public boolean modificar(
            Logros logro
    ) {

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        ContentValues cv =
                new ContentValues();

        cv.put(
                "Nombre",
                logro.getNombre()
        );

        cv.put(
                "Descripcion",
                logro.getDescripcion()
        );

        cv.put(
                "Requisito",
                logro.getRequisito()
        );

        int filas =
                db.update(
                        "Logros",
                        cv,
                        "IdLogro = ?",
                        new String[]{
                                String.valueOf(
                                        logro.getIdLogro()
                                )
                        }
                );

        return filas > 0;
    }

    public boolean eliminar(
            int idLogro
    ) {

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        int filas =
                db.delete(
                        "Logros",
                        "IdLogro = ?",
                        new String[]{
                                String.valueOf(
                                        idLogro
                                )
                        }
                );

        return filas > 0;
    }

    public List<Logros> obtenerTodos() {

        List<Logros> lista =
                new ArrayList<>();

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        String sql =
                "SELECT * FROM Logros " +
                        "ORDER BY IdLogro ASC";

        try (
                Cursor cursor =
                        db.rawQuery(
                                sql,
                                null
                        )
        ) {

            while (cursor.moveToNext()) {

                Logros logro =
                        new Logros(
                                cursor.getInt(
                                        cursor.getColumnIndexOrThrow(
                                                "IdLogro"
                                        )
                                ),
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                                "Nombre"
                                        )
                                ),
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                                "Descripcion"
                                        )
                                ),
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                                "Requisito"
                                        )
                                )
                        );

                lista.add(
                        logro
                );
            }
        }

        return lista;
    }

    public List<Integer> obtenerLogrosDesbloqueados(
            int idUsuario
    ) {

        List<Integer> lista =
                new ArrayList<>();

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        String sql =
                "SELECT DISTINCT IdLogro " +
                        "FROM UsuarioLogros " +
                        "WHERE IdUsuario = ?";

        try (
                Cursor cursor =
                        db.rawQuery(
                                sql,
                                new String[]{
                                        String.valueOf(
                                                idUsuario
                                        )
                                }
                        )
        ) {

            while (cursor.moveToNext()) {

                lista.add(
                        cursor.getInt(0)
                );
            }
        }

        return lista;
    }

    public int contarLogrosDisponibles() {

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        String sql =
                "SELECT COUNT(*) " +
                        "FROM Logros";

        try (
                Cursor cursor =
                        db.rawQuery(
                                sql,
                                null
                        )
        ) {

            if (cursor.moveToFirst()) {

                return cursor.getInt(0);
            }
        }

        return 0;
    }

    public int contarLogrosObtenidos(
            int idUsuario
    ) {

        if (idUsuario <= 0) {
            return 0;
        }

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        String sql =
                "SELECT COUNT(DISTINCT IdLogro) " +
                        "FROM UsuarioLogros " +
                        "WHERE IdUsuario = ?";

        try (
                Cursor cursor =
                        db.rawQuery(
                                sql,
                                new String[]{
                                        String.valueOf(
                                                idUsuario
                                        )
                                }
                        )
        ) {

            if (cursor.moveToFirst()) {

                return cursor.getInt(0);
            }
        }

        return 0;
    }

    public int calcularPorcentajeLogros(
            int idUsuario
    ) {

        int totalLogros =
                contarLogrosDisponibles();

        int logrosObtenidos =
                contarLogrosObtenidos(
                        idUsuario
                );

        if (totalLogros <= 0) {
            return 0;
        }

        int porcentaje =
                logrosObtenidos
                        * 100
                        / totalLogros;

        if (porcentaje < 0) {
            return 0;
        }

        if (porcentaje > 100) {
            return 100;
        }

        return porcentaje;
    }

    public boolean desbloquear(
            int idUsuario,
            int idLogro,
            String fecha
    ) {

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        ContentValues cv =
                new ContentValues();

        cv.put(
                "IdUsuario",
                idUsuario
        );

        cv.put(
                "IdLogro",
                idLogro
        );

        cv.put(
                "Fecha_obtenido",
                fecha
        );

        long resultado =
                db.insertWithOnConflict(
                        "UsuarioLogros",
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                );

        boolean nuevoDesbloqueo =
                resultado != -1L;

        if (nuevoDesbloqueo) {

            new android.os.Handler(
                    android.os.Looper.getMainLooper()
            ).post(
                    () ->
                            android.widget.Toast.makeText(
                                    context,
                                    "🏆 ¡Nuevo logro desbloqueado!",
                                    android.widget.Toast.LENGTH_LONG
                            ).show()
            );
        }

        return nuevoDesbloqueo;
    }
}