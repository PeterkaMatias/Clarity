
package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PerfilMenorDao {

    private final DatabaseHelper dbHelper;

    public PerfilMenorDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Registrar un menor asociado al usuario adulto
    public long registrarMenor(
            int idUsuario,
            String nombre,
            int edad
    ) {

        if (idUsuario <= 0 ||
                nombre == null ||
                nombre.trim().isEmpty() ||
                edad < 0 ||
                edad > 17) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("IdUsuario", idUsuario);
        valores.put("Nombre", nombre.trim());
        valores.put("Edad", edad);

        return db.insert(
                "PerfilMenor",
                null,
                valores
        );
    }

    // Consultar perfiles del usuario autenticado
    public Cursor obtenerMenores(int idUsuario) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(
                "PerfilMenor",
                null,
                "IdUsuario = ?",
                new String[]{String.valueOf(idUsuario)},
                null,
                null,
                "Nombre ASC"
        );
    }

    // Comprobar que el perfil pertenece al usuario
    public boolean perteneceAlUsuario(
            int idPerfilMenor,
            int idUsuario
    ) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM PerfilMenor " +
                        "WHERE IdPerfilMenor = ? " +
                        "AND IdUsuario = ? LIMIT 1",
                new String[]{
                        String.valueOf(idPerfilMenor),
                        String.valueOf(idUsuario)
                })) {

            return cursor.moveToFirst();
        }
    }

    // Editar nombre y edad del menor
    public boolean actualizarMenor(
            int idPerfilMenor,
            int idUsuario,
            String nombre,
            int edad
    ) {

        if (idPerfilMenor <= 0 ||
                idUsuario <= 0 ||
                nombre == null ||
                nombre.trim().isEmpty() ||
                edad < 0 ||
                edad > 17) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("Nombre", nombre.trim());
        valores.put("Edad", edad);

        int filas = db.update(
                "PerfilMenor",
                valores,
                "IdPerfilMenor = ? AND IdUsuario = ?",
                new String[]{
                        String.valueOf(idPerfilMenor),
                        String.valueOf(idUsuario)
                }
        );

        return filas > 0;
    }

    // Comprobar si el menor tiene tareas asociadas
    public boolean tieneTareas(
            int idPerfilMenor,
            int idUsuario
    ) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM Tareas " +
                        "WHERE IdPerfilMenor = ? " +
                        "AND idUsuario = ? LIMIT 1",
                new String[]{
                        String.valueOf(idPerfilMenor),
                        String.valueOf(idUsuario)
                })) {

            return cursor.moveToFirst();
        }
    }

    // Eliminar un perfil sin tareas asociadas
    public boolean eliminarMenor(
            int idPerfilMenor,
            int idUsuario
    ) {

        if (idPerfilMenor <= 0 || idUsuario <= 0) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // No eliminar perfiles con tareas asociadas
        if (tieneTareas(idPerfilMenor, idUsuario)) {
            return false;
        }

        int filas = db.delete(
                "PerfilMenor",
                "IdPerfilMenor = ? AND IdUsuario = ?",
                new String[]{
                        String.valueOf(idPerfilMenor),
                        String.valueOf(idUsuario)
                }
        );

        return filas > 0;
    }
}
