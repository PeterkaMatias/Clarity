package com.conexa.clarity.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "clarity.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla Recursos según DER
        db.execSQL("CREATE TABLE Recursos (" +
                "IdRecursos INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Titulo TEXT NOT NULL, " +
                "Contenido TEXT NOT NULL, " +
                "Categoria TEXT NOT NULL" +
                ");");

        // Tabla Favoritos según DER
        db.execSQL("CREATE TABLE Favoritos (" +
                "IdFavorito INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IdUsuario INTEGER NOT NULL, " +
                "IdRecurso INTEGER NOT NULL, " +
                "FOREIGN KEY (IdRecurso) REFERENCES Recursos(IdRecursos) ON DELETE CASCADE" +
                ");");

        // Insertar datos de prueba iniciales para el RF7
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
    }

    private void insertarRecurso(SQLiteDatabase db, String titulo, String contenido, String categoria) {
        ContentValues cv = new ContentValues();
        cv.put("Titulo", titulo);
        cv.put("Contenido", contenido);
        cv.put("Categoria", categoria);
        db.insert("Recursos", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Favoritos");
        db.execSQL("DROP TABLE IF EXISTS Recursos");
        onCreate(db);
    }
}