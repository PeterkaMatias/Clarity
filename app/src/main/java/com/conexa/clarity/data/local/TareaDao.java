package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Tarea;
import java.util.ArrayList;
import java.util.List;

public class TareaDao {
    private final DatabaseHelper dbHelper;
    public TareaDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }
    // INSERT
    public long insertar(Tarea tarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(tarea);
        return db.insert("Tareas", null, cv);
    }
    // SELECT
    /** Devuelve tareas principales del usuario ordenadas por fecha de vencimiento */
    public List<Tarea> obtenerPrincipales(int idUsuario){
        return consultar(
                "SELECT * FROM Tareas WHERE idUsuario = ? AND IdTareaPadre IS NULL ORDER BY Fecha_vencimiento ASC",
                new String[]{ String.valueOf(idUsuario) }
        );

    }

    /** Devuelve tareas pirncipales filtradas por estado(PENDIENTE, EN_PROGRESO, COMPLETADA) */
    public List<Tarea> obtenerPorEstado(int idUsuario, String estado){
        return consultar(
                "SELECT * FROM Tareas WHERE idUsuario = ? AND Estado = ? AND IdTareaPadre IS NULL ORDER BY Fecha_vencimiento ASC",
                new String[] { String.valueOf(idUsuario), estado }
        );

    }
    /** Devuelve los micro-pasos (subtareas) de una tarea padre */
    public List<Tarea> obtenerSubtareas(int idTareaPadre){
        return consultar(
                "SELECT * FROM Tareas WHERE IdTareaPadre = ? ORDER BY idTarea ASC",
                new String[]{ String.valueOf(idTareaPadre)}
        );
    }

    /** Cuenta subtareas completadas de una tarea padre (para calcular progreso) */
    public int contarSubtareasCompletadas(int idTareaPadre){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM Tareas WHERE IdTareaPadre = ? AND Estado = 'COMPLETADA'",
                new String[]{ String.valueOf(idTareaPadre) })) {
    return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** Cuenta total de subtareas de una tarea padre */
    public int contarSubtareas(int idTareaPadre) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery( "SELECT COUNT(*) FROM Tareas WHERE IdTareaPadre = ?",
                new String[]{ String.valueOf(idTareaPadre) })) {
            return c.moveToFirst() ? c.getInt(0) : 0;
    }
}
//UPDATE

    public boolean actualizar(Tarea tarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(tarea);
        int filas = db.update("Tareas", cv, "idTarea = ?",
                new String[]{ String.valueOf(tarea.getIdTarea()) });
return filas > 0;
    }
public boolean cambiarEstado(int idTarea, String nuevoEstado, String fechaFinalizado){
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put("Estado", nuevoEstado);
    if(fechaFinalizado != null){
        cv.put("Fecha_finalizado", fechaFinalizado);
    } else {
        cv.putNull("Fecha_finalizado");
    }
    int filas = db.update("Tareas", cv, "idTarea = ?",
            new String[]{ String.valueOf(idTarea) });
    return filas > 0;

}
//DELETE
    public boolean eliminar(int idTarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int filas = db.delete("Tareas", "idTarea = ?",
                new String[]{ String.valueOf(idTarea) });
        return filas > 0;
    }
// HELPERS PRIVADOS
    private List<Tarea> consultar(String sql, String[] args) {
        List<Tarea> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, args)){
            while (c.moveToNext()) {
                lista.add(mapear(c));
            }
        }
        return lista;

    }

    private Tarea mapear(Cursor c){
        Tarea t = new Tarea();
        t.setIdTarea(c.getInt(c.getColumnIndexOrThrow("idTarea")));
        t.setIdUsuario(c.getInt(c.getColumnIndexOrThrow("idUsuario")));
        t.setTitulo(c.getString(c.getColumnIndexOrThrow("Titulo")));
        t.setDescripcion(c.getString(c.getColumnIndexOrThrow("Descripcion")));
        t.setEstado(c.getString(c.getColumnIndexOrThrow("Estado")));
        t.setPrioridad(c.getString(c.getColumnIndexOrThrow("Prioridad")));
        t.setFechaCreacion(c.getString(c.getColumnIndexOrThrow("Fecha_creacion")));
        t.setFechaFinalizado(c.getString(c.getColumnIndexOrThrow("Fecha_finalizado")));
        t.setFechaVencimiento(c.getString(c.getColumnIndexOrThrow("Fecha_vencimiento")));
        int padre = c.getColumnIndex("IdTareaPadre");
        t.setIdTareaPadre(c.isNull(padre) ? 0 : c.getInt(padre));
        return t;

    }
private ContentValues toContentValues(Tarea t){
        ContentValues cv = new ContentValues();
        cv.put("idUsuario", t.getIdUsuario());
        cv.put("Titulo", t.getTitulo());
        cv.put("Descripcion", t.getDescripcion());
        cv.put("Estado" , t.getEstado());
        cv.put("Prioridad", t.getPrioridad());
        cv.put("Fecha_creacion", t.getFechaCreacion());
        cv.put("Fecha_finalizado", t.getFechaFinalizado());
        cv.put("Fecha_vencimiento", t.getFechaVencimiento());
        if (t.esMicroPaso()){
            cv.put("IdTareaPadre", t.getIdTareaPadre());
        }else {
            cv.putNull("IdTareaPadre");
    }

return cv;
    }
}