package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.conexa.clarity.data.model.Tarea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TareaDao {
    private final DatabaseHelper dbHelper;
    private final RecompensasDao recompensasDao;
    private final LogrosDao logrosDao;
    public TareaDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.recompensasDao = new RecompensasDao(context);
        this.logrosDao = new LogrosDao(context);
    }

    // INSERT
    public long insertar(Tarea tarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(tarea);

        long idTarea = db.insert("Tareas", null, cv);

        if(idTarea != -1 && !tarea.esMicroPaso()){
            recompensasDao.otorgarPuntos(
                    tarea.getIdUsuario(),
                    (int) idTarea,
                    5,
                    "CREAR_TAREA",
                    "Creacion de una tarea",
                    "TAREA_" + idTarea,
                    tarea.getFechaCreacion()
            );

        }

        return idTarea;
    }
    // SELECT
    /** Devuelve tareas principales del usuario ordenadas por fecha de vencimiento */
    public List<Tarea> obtenerPrincipales(int idUsuario){
        return consultar(
                "SELECT * FROM Tareas WHERE idUsuario = ? AND IdTareaPadre IS NULL ORDER BY Fecha_vencimiento ASC",
                new String[]{ String.valueOf(idUsuario) }
        );

    }

    /** Devuelve tareas principales filtradas por estado (PENDIENTE, EN_PROGRESO, COMPLETADA) */
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

    // UPDATE
    public boolean actualizar(Tarea tarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(tarea);
        int filas = db.update("Tareas", cv, "idTarea = ?",
                new String[]{ String.valueOf(tarea.getIdTarea()) });
        return filas > 0;
    }

    public boolean cambiarEstado(int idTarea, String nuevoEstado, String fechaFinalizado){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int idUsuario;
        boolean esMicroPaso;
        String estadoAnterior;

        try(Cursor c = db.rawQuery(
                "SELECT idUsuario, IdTareaPadre, Estado FROM Tareas WHERE idTarea = ?",
                new String[]{String.valueOf(idTarea)}
        )){
            if(!c.moveToFirst()){
                return false;
            }
            idUsuario = c.getInt(0);
            esMicroPaso = !c.isNull(1);
            estadoAnterior = c.getString(2);
        }
        ContentValues cv = new ContentValues();
        cv.put("Estado", nuevoEstado);
        if(fechaFinalizado != null){
            cv.put("Fecha_finalizado", fechaFinalizado);
        } else {
            cv.putNull("Fecha_finalizado");
        }
        int filas = db.update("Tareas", cv, "idTarea = ?",
                new String[]{ String.valueOf(idTarea) });

        if(filas > 0 &&
        Tarea.ESTADO_COMPLETADA.equals(nuevoEstado)&&
        !Tarea.ESTADO_COMPLETADA.equals(estadoAnterior)){
            int puntos = esMicroPaso ? 10 : 25;
            String tipo = esMicroPaso ? "COMPLETAR_MICROPASO" :  "COMPLETAR_TAREA";
            String descripcion = esMicroPaso
                    ? "Completar una microtarea"
                    : "Completar una tarea";

            String fecha = fechaFinalizado != null
                    ? fechaFinalizado
                    : new java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                    java.util.Locale.getDefault()
            ).format(new java.util.Date());
            recompensasDao.otorgarPuntos(
                    idUsuario,
                    idTarea,
                    puntos,
                    tipo,
                    descripcion,
                    "TAREA_" + idTarea,
                    fecha
            );
            if (esMicroPaso) {

                // LOGRO 3
                verificarDividirYConquistar(idUsuario, idTarea, fecha);

                // LOGRO 10
                verificarRachaImparable(idUsuario);

            } else {

                // LOGRO 1
                logrosDao.desbloquear(idUsuario, 1, fecha);

                // LOGRO 4
                verificarSinPostergar(idUsuario, idTarea, fecha);
            }
        }
        return filas > 0;
    }

    // DELETE
    public boolean eliminar(int idTarea) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int filas = db.delete("Tareas", "idTarea = ?",
                new String[]{ String.valueOf(idTarea) });
        return filas > 0;
    }
    // ===== Compatibilidad con el Dashboard (MainActivity) =====
    /** Usado por MainActivity: devuelve las tareas principales del usuario */
    public List<Tarea> obtenerTareasPorUsuario(int idUsuario) {
        return consultar(
                "SELECT * FROM Tareas " +
                        "WHERE idUsuario = ? " +
                        "AND IdPerfilMenor IS NULL " +
                        "AND IdTareaPadre IS NULL " +
                        "ORDER BY Fecha_vencimiento ASC",
                new String[]{String.valueOf(idUsuario)}
        );
    }

    /** Usado por MainActivity: marca una tarea como completada o pendiente */
    public boolean actualizarCompletada(int idTarea, boolean completada) {
        String fecha = completada
                ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date())
                : null;
        return cambiarEstado(idTarea, completada ? Tarea.ESTADO_COMPLETADA : Tarea.ESTADO_PENDIENTE, fecha);
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
        } else {
            cv.putNull("IdTareaPadre");
        }
        return cv;
    }

    public int filtrarTareaPorPeriodo( int idUsuario, String fechaDesde, String fechaHasta){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) FROM Tareas " +
                "WHERE idUsuario = ? " +
                "AND Estado = 'COMPLETADA' " +
                "AND IdTareaPadre IS NULL " +
                "AND Fecha_finalizado >= ? " +
                "AND Fecha_finalizado < ? ";

        try(Cursor c = db.rawQuery(sql, new String[]{
                String. valueOf(idUsuario),
                fechaDesde,
                fechaHasta
        })){
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public List <Integer> contarTareasPorFecha(int idUsuario, String fechaDesde, String fechaHasta){
        List<Integer> resultados = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT substr(Fecha_finalizado, 1, 10), COUNT(*) " +
                "FROM Tareas " +
                "WHERE idUsuario = ? " +
                "AND Estado = 'COMPLETADA' " +
                "AND IdTareaPadre IS NULL " +
                "AND Fecha_finalizado >= ? " +
                "AND Fecha_finalizado < ? " +
                "GROUP BY substr(Fecha_finalizado, 1, 10) " +
                "ORDER BY substr(Fecha_finalizado, 1, 10) ASC";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario),
                fechaDesde,
                fechaHasta
        })) {
            while (c.moveToNext()) {
                resultados.add(c.getInt(1));
            }
        }

        return resultados;

    }
    public Map<String, Integer> contarTareasPorHora(
            int idUsuario, String fechaDesde, String fechaHasta) {

        Map<String, Integer> resultados = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT substr(Fecha_finalizado, 12, 2), COUNT(*) " +
                "FROM Tareas " +
                "WHERE idUsuario = ? " +
                "AND Estado = 'COMPLETADA' " +
                "AND IdTareaPadre IS NULL " +
                "AND Fecha_finalizado >= ? " +
                "AND Fecha_finalizado < ? " +
                "GROUP BY substr(Fecha_finalizado, 12, 2)";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario), fechaDesde, fechaHasta
        })) {
            while (c.moveToNext()) {
                resultados.put(c.getString(0), c.getInt(1));
            }
        }

        return resultados;
    }
    public Map<String, Integer> contarTareasPorFechas(
            int idUsuario, String fechaDesde, String fechaHasta) {

        Map<String, Integer> resultados = new HashMap<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT substr(Fecha_finalizado, 1, 10), COUNT(*) " +
                "FROM Tareas " +
                "WHERE idUsuario = ? " +
                "AND Estado = 'COMPLETADA' " +
                "AND IdTareaPadre IS NULL " +
                "AND Fecha_finalizado >= ? " +
                "AND Fecha_finalizado < ? " +
                "GROUP BY substr(Fecha_finalizado, 1, 10)";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario),
                fechaDesde,
                fechaHasta
        })) {
            while (c.moveToNext()) {
                resultados.put(c.getString(0), c.getInt(1));
            }
        }

        return resultados;
    }
    public List<String> obtenerFechasCompletadas(int idUsuario) {
        List<String> fechas = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT substr(Fecha_finalizado, 1, 10) " +
                "FROM Tareas " +
                "WHERE idUsuario = ? " +
                "AND Estado = 'COMPLETADA' " +
                "AND IdTareaPadre IS NULL " +
                "AND Fecha_finalizado IS NOT NULL " +
                "ORDER BY substr(Fecha_finalizado, 1, 10) ASC";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario)
        })) {
            while (c.moveToNext()) {
                fechas.add(c.getString(0));
            }
        }

        return fechas;
    }


    // ===== MODO PARENTAL =====
    public long asignarTareaMenor(
            int idUsuario,
            int idPerfilMenor,
            String titulo,
            String descripcion,
            String prioridad
    ) {

        if (idUsuario <= 0 ||
                idPerfilMenor <= 0 ||
                titulo == null ||
                titulo.trim().isEmpty()) {
            return -1;
        }

        if (!"ALTA".equals(prioridad) &&
                !"MEDIA".equals(prioridad) &&
                !"BAJA".equals(prioridad)) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Verificar que el menor pertenezca al adulto
        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM PerfilMenor " +
                        "WHERE IdPerfilMenor = ? AND IdUsuario = ?",
                new String[]{
                        String.valueOf(idPerfilMenor),
                        String.valueOf(idUsuario)
                })) {

            if (!cursor.moveToFirst()) {
                return -1;
            }
        }

        ContentValues valores = new ContentValues();

        valores.put("idUsuario", idUsuario);
        valores.put("IdPerfilMenor", idPerfilMenor);
        valores.put("Titulo", titulo.trim());
        valores.put("Descripcion", descripcion == null ? "" : descripcion.trim());
        valores.put("Estado", "PENDIENTE");
        valores.put("Prioridad", prioridad);

        String fecha = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                java.util.Locale.getDefault()
        ).format(new java.util.Date());

        valores.put("Fecha_creacion", fecha);
        valores.putNull("Fecha_finalizado");
        valores.putNull("Fecha_vencimiento");
        valores.putNull("IdTareaPadre");

        return db.insert("Tareas", null, valores);
    }

    public List<Tarea> obtenerTareasDeMenor(
            int idUsuario,
            int idPerfilMenor
    ) {

        return consultar(
                "SELECT * FROM Tareas " +
                        "WHERE idUsuario = ? " +
                        "AND IdPerfilMenor = ? " +
                        "AND IdTareaPadre IS NULL " +
                        "ORDER BY idTarea DESC",
                new String[]{
                        String.valueOf(idUsuario),
                        String.valueOf(idPerfilMenor)
                }
        );
    }

    public boolean cambiarEstadoTareaMenor(
            int idUsuario,
            int idPerfilMenor,
            int idTarea,
            boolean completada
    ) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues valores = new ContentValues();

        valores.put("Estado", completada ? "COMPLETADA" : "PENDIENTE");

        if (completada) {

            String fecha = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    java.util.Locale.getDefault()
            ).format(new java.util.Date());

            valores.put("Fecha_finalizado", fecha);

        } else {
            valores.putNull("Fecha_finalizado");
        }

        int filas = db.update(
                "Tareas",
                valores,
                "idTarea = ? AND idUsuario = ? " +
                        "AND IdPerfilMenor = ? " +
                        "AND IdTareaPadre IS NULL",
                new String[]{
                        String.valueOf(idTarea),
                        String.valueOf(idUsuario),
                        String.valueOf(idPerfilMenor)
                }
        );

        return filas > 0;
    }

    public long asignarTareaMenorConFecha(
            int idUsuario,
            int idPerfilMenor,
            String titulo,
            String descripcion,
            String prioridad,
            String fechaVencimiento
    ) {
        if (idUsuario <= 0 || idPerfilMenor <= 0 ||
                titulo == null || titulo.trim().isEmpty() ||
                !prioridadValidaMenor(prioridad)) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM PerfilMenor " +
                        "WHERE IdPerfilMenor = ? AND IdUsuario = ?",
                new String[]{
                        String.valueOf(idPerfilMenor),
                        String.valueOf(idUsuario)
                })) {
            if (!c.moveToFirst()) return -1;
        }

        ContentValues cv = new ContentValues();
        cv.put("idUsuario", idUsuario);
        cv.put("IdPerfilMenor", idPerfilMenor);
        cv.put("Titulo", titulo.trim());
        cv.put("Descripcion", descripcion == null ? "" : descripcion.trim());
        cv.put("Prioridad", prioridad);
        cv.put("Estado", "PENDIENTE");

        String fechaActual = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                java.util.Locale.getDefault()
        ).format(new java.util.Date());

        cv.put("Fecha_creacion", fechaActual);
        cv.putNull("Fecha_finalizado");
        cv.putNull("IdTareaPadre");

        if (fechaVencimiento == null || fechaVencimiento.trim().isEmpty()) {
            cv.putNull("Fecha_vencimiento");
        } else {
            cv.put("Fecha_vencimiento", fechaVencimiento);
        }

        return db.insert("Tareas", null, cv);
    }

    public boolean editarTareaMenor(
            int idUsuario,
            int idPerfilMenor,
            int idTarea,
            String titulo,
            String descripcion,
            String prioridad,
            String fechaVencimiento
    ) {
        if (idUsuario <= 0 || idPerfilMenor <= 0 ||
                idTarea <= 0 ||
                titulo == null || titulo.trim().isEmpty() ||
                !prioridadValidaMenor(prioridad)) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("Titulo", titulo.trim());
        cv.put("Descripcion", descripcion == null ? "" : descripcion.trim());
        cv.put("Prioridad", prioridad);

        if (fechaVencimiento == null || fechaVencimiento.trim().isEmpty()) {
            cv.putNull("Fecha_vencimiento");
        } else {
            cv.put("Fecha_vencimiento", fechaVencimiento);
        }

        int filas = db.update(
                "Tareas",
                cv,
                "idTarea = ? AND idUsuario = ? " +
                        "AND IdPerfilMenor = ? AND IdTareaPadre IS NULL " +
                        "AND EXISTS (SELECT 1 FROM PerfilMenor p " +
                        "WHERE p.IdPerfilMenor = Tareas.IdPerfilMenor " +
                        "AND p.IdUsuario = Tareas.idUsuario)",
                new String[]{
                        String.valueOf(idTarea),
                        String.valueOf(idUsuario),
                        String.valueOf(idPerfilMenor)
                }
        );

        return filas > 0;
    }

    public boolean eliminarTareaMenor(
            int idUsuario,
            int idPerfilMenor,
            int idTarea
    ) {
        if (idUsuario <= 0 || idPerfilMenor <= 0 || idTarea <= 0) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas = db.delete(
                "Tareas",
                "idTarea = ? AND idUsuario = ? " +
                        "AND IdPerfilMenor = ? AND IdTareaPadre IS NULL " +
                        "AND EXISTS (SELECT 1 FROM PerfilMenor p " +
                        "WHERE p.IdPerfilMenor = Tareas.IdPerfilMenor " +
                        "AND p.IdUsuario = Tareas.idUsuario)",
                new String[]{
                        String.valueOf(idTarea),
                        String.valueOf(idUsuario),
                        String.valueOf(idPerfilMenor)
                }
        );

        return filas > 0;
    }

    private boolean prioridadValidaMenor(String prioridad) {
        return "ALTA".equals(prioridad) ||
                "MEDIA".equals(prioridad) ||
                "BAJA".equals(prioridad);
    }

    //LOGROS 1, 2, 3, 4
    public void verificarMenteDespejada(int idUsuario, String fecha) {

        if (fecha == null || fecha.length() < 10) {
            return;
        }

        String fechaDia = fecha.substring(0, 10);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) FROM Tareas " +
                "WHERE idUsuario = ? " +
                "AND IdTareaPadre IS NULL " +
                "AND substr(Fecha_creacion, 1, 10) = ?";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario),
                fechaDia
        })) {

            if (c.moveToFirst() && c.getInt(0) >= 3) {
                logrosDao.desbloquear(idUsuario, 2, fecha);
            }
        }
    }
    public void verificarDividirYConquistar(
            int idUsuario,
            int idMicroPaso,
            String fecha) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        int idTareaPadre = -1;

        String sqlPadre = "SELECT IdTareaPadre FROM Tareas " +
                "WHERE idTarea = ? AND idUsuario = ?";

        try (Cursor c = db.rawQuery(sqlPadre, new String[]{
                String.valueOf(idMicroPaso),
                String.valueOf(idUsuario)
        })) {
            if (c.moveToFirst() && !c.isNull(0)) {
                idTareaPadre = c.getInt(0);
            }
        }

        if (idTareaPadre == -1) {
            return;
        }

        String sqlTotal = "SELECT COUNT(*) FROM Tareas " +
                "WHERE idUsuario = ? AND IdTareaPadre = ?";

        String sqlPendientes = "SELECT COUNT(*) FROM Tareas " +
                "WHERE idUsuario = ? AND IdTareaPadre = ? " +
                "AND Estado != ?";

        int total;
        int pendientes;

        try (Cursor c = db.rawQuery(sqlTotal, new String[]{
                String.valueOf(idUsuario),
                String.valueOf(idTareaPadre)
        })) {
            c.moveToFirst();
            total = c.getInt(0);
        }

        try (Cursor c = db.rawQuery(sqlPendientes, new String[]{
                String.valueOf(idUsuario),
                String.valueOf(idTareaPadre),
                Tarea.ESTADO_COMPLETADA
        })) {
            c.moveToFirst();
            pendientes = c.getInt(0);
        }

        if (total > 0 && pendientes == 0) {
            logrosDao.desbloquear(idUsuario, 3, fecha);
        }
    }
    public void verificarSinPostergar(
            int idUsuario,
            int idTarea,
            String fechaFinalizado) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT Fecha_vencimiento FROM Tareas " +
                "WHERE idTarea = ? AND idUsuario = ? " +
                "AND IdTareaPadre IS NULL";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idTarea),
                String.valueOf(idUsuario)
        })) {

            if (c.moveToFirst() && !c.isNull(0)) {

                String vencimiento = c.getString(0);

                if (fechaFinalizado != null &&
                        fechaFinalizado.compareTo(vencimiento) < 0) {

                    logrosDao.desbloquear(
                            idUsuario,
                            4,
                            fechaFinalizado
                    );
                }
            }
        }
    }
    public void verificarArranqueRapido(int idUsuario, String fecha) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) FROM Temporizador " +
                "WHERE IdUsuario = ? " +
                "AND Duracion = 5 " +
                "AND Estado = 'Completado'";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario)
        })) {

            if (c.moveToFirst() && c.getInt(0) > 0) {
                logrosDao.desbloquear(idUsuario, 5, fecha);
            }
        }
    }
    public void verificarEnLaZona(int idUsuario, String fecha) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT COUNT(*) FROM Temporizador " +
                "WHERE IdUsuario = ? " +
                "AND Duracion = 25 " +
                "AND Estado = 'Completado'";

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario)
        })) {

            if (c.moveToFirst() && c.getInt(0) > 0) {
                logrosDao.desbloquear(idUsuario, 6, fecha);
            }
        }
    }
    // LOGRO 10: Racha Imparable
    public void verificarRachaImparable(int idUsuario) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql = "SELECT DISTINCT substr(Fecha_finalizado, 1, 10) " +
                "FROM Tareas " +
                "WHERE idUsuario = ? " +
                "AND IdTareaPadre IS NOT NULL " +
                "AND Estado = ? " +
                "AND Fecha_finalizado IS NOT NULL " +
                "ORDER BY substr(Fecha_finalizado, 1, 10) DESC";

        List<String> fechas = new ArrayList<>();

        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(idUsuario),
                Tarea.ESTADO_COMPLETADA
        })) {
            while (c.moveToNext()) {
                fechas.add(c.getString(0));
            }
        }

        int consecutivos = 0;
        java.time.LocalDate fechaAnterior = null;

        for (String fecha : fechas) {

            java.time.LocalDate fechaActual =
                    java.time.LocalDate.parse(fecha);

            if (fechaAnterior == null ||
                    fechaAnterior.minusDays(1).equals(fechaActual)) {
                consecutivos++;
            } else {
                consecutivos = 1;
            }

            if (consecutivos >= 3) {

                String fechaLogro = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

                logrosDao.desbloquear(idUsuario, 10, fechaLogro);
                return;
            }

            fechaAnterior = fechaActual;
        }
    }


}