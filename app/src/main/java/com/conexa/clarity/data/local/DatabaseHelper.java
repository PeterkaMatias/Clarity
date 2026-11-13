package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "clarity.db";
    private static final int DATABASE_VERSION = 10;

    private static DatabaseHelper instance;

    // Todos piden la base con este método, así hay una sola para toda la app
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Usuarios
        db.execSQL("CREATE TABLE Usuarios (" +
                "IdUsuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Nombre TEXT NOT NULL, " +
                "Apellido TEXT NOT NULL, " +
                "Email TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "Password TEXT NOT NULL, " +
                "TipoPerfil TEXT NOT NULL DEFAULT 'GENERAL' CHECK (TipoPerfil IN ('GENERAL','PARENTAL')), " +
                "PuntosTotales INTEGER NOT NULL DEFAULT 0, " +
                "FechaRegistro TEXT NOT NULL DEFAULT (datetime('now','localtime')), " +
                "Nivel INTEGER NOT NULL DEFAULT 1" +
                ");");

        // Perfil del (menor administrado desde el modo parental)
        db.execSQL("CREATE TABLE PerfilMenor (" +
                "IdPerfilMenor INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "Nombre TEXT NOT NULL, " +
                "Edad INTEGER, " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE" +
                ");");

        // Tareas (versión completa de Rodrigo con Estado, Prioridad, Fechas y micro-pasos)

// Tareas (versión completa de Rodrigo con Estado, Prioridad, Fechas y micro-pasos)
        db.execSQL("CREATE TABLE Tareas(" +
                "idTarea INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idUsuario INTEGER NOT NULL, " +
                "IdPerfilMenor INTEGER, " +
                "Titulo TEXT NOT NULL, " +
                "Descripcion TEXT, " +
                "Estado TEXT NOT NULL DEFAULT 'PENDIENTE' " +
                "CHECK (Estado IN ('PENDIENTE','EN_PROGRESO','COMPLETADA')), " +
                "Prioridad TEXT NOT NULL DEFAULT 'MEDIA' " +
                "CHECK (Prioridad IN ('ALTA','MEDIA','BAJA')), " +
                "Fecha_creacion TEXT, " +
                "Fecha_finalizado TEXT, " +
                "Fecha_vencimiento TEXT, " +
                "IdTareaPadre INTEGER, " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdTareaPadre) REFERENCES Tareas(IdTarea) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdPerfilMenor) REFERENCES PerfilMenor(IdPerfilMenor) ON DELETE CASCADE" +
                ");");

        //Temporizador
        db.execSQL("CREATE TABLE Temporizador(" +
                "IdTemporizador INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdTarea INTEGER, " +
                "Duracion INTEGER NOT NULL, " +
                "Fecha_inicio TEXT NOT NULL, " +
                "Fecha_fin TEXT NOT NULL, "+
                "Estado TEXT NOT NULL," +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdTarea) REFERENCES Tareas(IdTarea) ON DELETE SET NULL" +
                ");");
        //RECOMPENSAS
        db.execSQL("CREATE TABLE Recompensas(" +
                "IdRecompensa INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "IdUsuario INTEGER NOT NULL, " +
                "IdTarea INTEGER, " +
                "Referencia TEXT, " +
                "Puntos INTEGER NOT NULL, " +
                "Tipo TEXT NOT NULL, " +
                "Descripcion TEXT NOT NULL, " +
                "Fecha TEXT NOT NULL, " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdTarea) REFERENCES Tareas(IdTarea) ON DELETE SET NULL" +
                ");");
        //LOGROS
        db.execSQL("CREATE TABLE Logros(" +
                "IdLogro INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Nombre TEXT NOT NULL, " +
                "Descripcion TEXT, " +
                "Requisito TEXT" +
                ");");
        //USUARIOLOGROS
        db.execSQL("CREATE TABLE UsuarioLogros(" +
                "IdUsuarioLogro INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdLogro INTEGER NOT NULL, " +
                "Fecha_obtenido TEXT NOT NULL, " +
                "UNIQUE (IdUsuario, IdLogro), " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdLogro) REFERENCES Logros(IdLogro) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE RecursosLeidos (" +
                "IdLectura INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdRecurso INTEGER NOT NULL, " +
                "Fecha TEXT NOT NULL, " +
                "UNIQUE (IdUsuario, IdRecurso), " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdRecurso) REFERENCES Recursos(IdRecursos) ON DELETE CASCADE" +
                ");");

        // Consultas con el Chat
        db.execSQL("CREATE TABLE ConsultasChat (" +
                "IdConsulta INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "Consulta TEXT NOT NULL, " +
                "Respuesta TEXT, " +
                "Feedback INTEGER, " +
                "Fecha TEXT NOT NULL DEFAULT (datetime('now','localtime')), " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE" +
                ");");

        // Recursos (ahora guarda el tiempo de lectura)
        db.execSQL("CREATE TABLE Recursos (" +
                "IdRecursos INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Titulo TEXT NOT NULL, " +
                "Contenido TEXT NOT NULL, " +
                "Categoria TEXT NOT NULL, " +
                "TiempoLecturaMin INTEGER NOT NULL DEFAULT 2" +
                ");");

        // Favoritos (ahora apunta a Usuarios)
        db.execSQL("CREATE TABLE Favoritos (" +
                "IdFavorito INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdRecurso INTEGER NOT NULL, " +
                "UNIQUE (IdUsuario, IdRecurso), " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdRecurso) REFERENCES Recursos(IdRecursos) ON DELETE CASCADE" +
                ");");

        // Ajustes Preferencias de usuario
        db.execSQL("CREATE TABLE PreferenciaUsuario (" +
                "IdPreferencia INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL UNIQUE, " +
                "Notificaciones INTEGER NOT NULL DEFAULT 1, " +
                "Recordatorios INTEGER NOT NULL DEFAULT 1, " +
                "Sonido INTEGER NOT NULL DEFAULT 1, " +
                "Vibracion INTEGER NOT NULL DEFAULT 1, " +
                "ModoParental INTEGER NOT NULL DEFAULT 0 " +
                "CHECK (ModoParental IN (0, 1)), " +
                "Tema TEXT NOT NULL DEFAULT 'CLARO' " +
                "CHECK (Tema IN ('CLARO', 'OSCURO')), " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE" +
                ");");

        insertarDatosIniciales(db);
    }

    private void insertarDatosIniciales(SQLiteDatabase db) {
        insertarRecurso(db, "La regla de los 2 minutos",
                "Si una tarea toma menos de dos minutos en completarse, hacela en el momento. Esto reduce la acumulación de micro-pendientes que sobrecargan la memoria de trabajo.",
                "Disfunción Ejecutiva");

        insertarRecurso(db, "Manejo de la Ceguera del Tiempo",
                "Las personas con TDAH perciben el tiempo en dos estados: 'ahora' y 'no ahora'. Utilizar temporizadores visuales con barras progresivas ayuda a materializar el paso del tiempo.",
                "Ceguera del Tiempo");

        insertarRecurso(db, "Técnica Pomodoro Adaptada",
                "El Pomodoro tradicional de 25/5 puede ser frustrante. Probá bloques más cortos de 10 o 15 minutos de foco con 3 minutos de descanso para vencer la resistencia inicial.",
                "Técnicas de Estudio");

        insertarRecurso(db, "Guía para Padres: Rutinas Visuales",
                "Reemplazar órdenes verbales repetitivas por tableros con íconos o colores reduce la fricción y fomenta la autonomía en niños con TDAH.",
                "Guía para Padres");

        insertarLogros(db);
    }
    private void insertarLogros(SQLiteDatabase db) {

        insertarLogro(db, 1, "Primer Paso",
                "Completá tu primera tarea.",
                "PRIMERA_TAREA");

        insertarLogro(db, 2, "Mente Despejada",
                "Registrá al menos 3 tareas en un mismo día.",
                "TRES_TAREAS_DIA");

        insertarLogro(db, 3, "Dividir y Conquistar",
                "Usá el desglose y completá todos los micro-pasos.",
                "DESGLOSE_COMPLETO");

        insertarLogro(db, 4, "Sin Postergar",
                "Completá una tarea antes de su vencimiento.",
                "ANTES_VENCIMIENTO");

        insertarLogro(db, 5, "Arranque Rápido",
                "Completá tu primer temporizador de 5 minutos.",
                "PRIMER_TEMPORIZADOR_5");

        insertarLogro(db, 6, "En la Zona",
                "Completá un bloque de concentración de 25 minutos sin pausas.",
                "TEMPORIZADOR_25");

        insertarLogro(db, 7, "Hora Dorada",
                "Acumulá 60 minutos de concentración.",
                "60_MINUTOS_FOCO");

        insertarLogro(db, 8, "Amigo de Lumi",
                "Realizá tu primera consulta a Lumi.",
                "PRIMERA_CONSULTA_LUMI");

        insertarLogro(db, 9, "Especialista en TDAH",
                "Leé y guardá 3 recursos diferentes.",
                "TRES_RECURSOS_LEIDOS_Y_GUARDADOS");

        insertarLogro(db, 10, "Racha Imparable",
                "Completá al menos un micro-paso durante 3 días consecutivos.",
                "TRES_DIAS_MICROPASOS");
    }

    private void insertarLogro(SQLiteDatabase db, int id, String nombre,
                               String descripcion, String requisito) {

        ContentValues cv = new ContentValues();
        cv.put("IdLogro", id);
        cv.put("Nombre", nombre);
        cv.put("Descripcion", descripcion);
        cv.put("Requisito", requisito);

        db.insertWithOnConflict(
                "Logros",
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    private void insertarRecurso(SQLiteDatabase db, String titulo, String contenido, String categoria) {
        ContentValues cv = new ContentValues();
        cv.put("Titulo", titulo);
        cv.put("Contenido", contenido);
        cv.put("Categoria", categoria);
        db.insert("Recursos", null, cv);
    }

    //Sirve mientras seguimos haciendo cambios, despues hay que reemplazarlo
    /*@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS UsuarioLogros");
        db.execSQL("DROP TABLE IF EXISTS Recompensas");
        db.execSQL("DROP TABLE IF EXISTS Temporizador");
        db.execSQL("DROP TABLE IF EXISTS Favoritos");
        db.execSQL("DROP TABLE IF EXISTS PreferenciaUsuario");
        db.execSQL("DROP TABLE IF EXISTS ConsultasChat");

        // Estrategia segura: recrear todas las tablas
        db.execSQL("DROP TABLE IF EXISTS Tareas");
        db.execSQL("DROP TABLE IF EXISTS PerfilMenor");
        db.execSQL("DROP TABLE IF EXISTS Logros");
        db.execSQL("DROP TABLE IF EXISTS Recursos");
        db.execSQL("DROP TABLE IF EXISTS Usuarios");
        onCreate(db);
    }*/
    // Actualiza la base de datos cuando se agregan o modifican tablas y columnas,
    // conservando los datos que ya tiene el usuario.
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Crear Recompensas si todavía no existe.
        db.execSQL("CREATE TABLE IF NOT EXISTS Recompensas (" +
                "IdRecompensa INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdTarea INTEGER, " +
                "Referencia TEXT, " +
                "Puntos INTEGER NOT NULL, " +
                "Tipo TEXT NOT NULL, " +
                "Descripcion TEXT NOT NULL, " +
                "Fecha TEXT NOT NULL, " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdTarea) REFERENCES Tareas(IdTarea) ON DELETE SET NULL" +
                ")");

        if (!existeColumna(db, "Tareas", "IdPerfilMenor")) {
            db.execSQL("ALTER TABLE Tareas ADD COLUMN IdPerfilMenor INTEGER");
        }

        // Agregar Referencia solamente si todavía no existe.
        if (!existeColumna(db, "Recompensas", "Referencia")) {
            db.execSQL("ALTER TABLE Recompensas ADD COLUMN Referencia TEXT");
        }

        // Crear Logros si todavía no existe.
        db.execSQL("CREATE TABLE IF NOT EXISTS Logros (" +
                "IdLogro INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Nombre TEXT NOT NULL, " +
                "Descripcion TEXT, " +
                "Requisito TEXT" +
                ")");

        // Crear UsuarioLogros si todavía no existe.
        db.execSQL("CREATE TABLE IF NOT EXISTS UsuarioLogros (" +
                "IdUsuarioLogro INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdLogro INTEGER NOT NULL, " +
                "Fecha_obtenido TEXT NOT NULL, " +
                "UNIQUE (IdUsuario, IdLogro), " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdLogro) REFERENCES Logros(IdLogro) ON DELETE CASCADE" +
                ")");

        // Evitar recompensas duplicadas.
        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_recompensas_unicas " +
                        "ON Recompensas(IdUsuario, Tipo, Referencia)"
        );

        // Eliminar posibles logros duplicados.
        db.execSQL(
                "DELETE FROM UsuarioLogros " +
                        "WHERE IdUsuarioLogro NOT IN (" +
                        "SELECT MIN(IdUsuarioLogro) FROM UsuarioLogros " +
                        "GROUP BY IdUsuario, IdLogro)"
        );

        db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_logros_unicos " +
                        "ON UsuarioLogros(IdUsuario, IdLogro)"
        );

        // Crear RecursosLeidos si todavía no existe.
        db.execSQL("CREATE TABLE IF NOT EXISTS RecursosLeidos (" +
                "IdLectura INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdRecurso INTEGER NOT NULL, " +
                "Fecha TEXT NOT NULL, " +
                "UNIQUE (IdUsuario, IdRecurso), " +
                "FOREIGN KEY (IdUsuario) REFERENCES Usuarios(IdUsuario) ON DELETE CASCADE, " +
                "FOREIGN KEY (IdRecurso) REFERENCES Recursos(IdRecursos) ON DELETE CASCADE" +
                ")");

        insertarLogros(db);
    }private boolean existeColumna(
            SQLiteDatabase db, String tabla, String columna) {

        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tabla + ")", null)) {

            while (cursor.moveToNext()) {
                String nombreColumna = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                if (nombreColumna.equalsIgnoreCase(columna)) {
                    return true;
                }
            }
        }

        return false;
    }

}