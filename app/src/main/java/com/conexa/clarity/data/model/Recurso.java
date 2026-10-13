package com.conexa.clarity.data.model;

import java.io.Serializable;

public class Recurso implements Serializable {
    private int idRecurso;
    private String titulo;
    private String contenido;
    private String categoria;
    private int tiempoLecturaMin;
    private boolean esFavorito;

    public Recurso(int idRecurso, String titulo, String contenido, String categoria, int tiempoLecturaMin, boolean esFavorito) {
        this.idRecurso = idRecurso;
        this.titulo = titulo;
        this.contenido = contenido;
        this.categoria = categoria;
        this.tiempoLecturaMin = tiempoLecturaMin;
        this.esFavorito = esFavorito;
    }

    // Getters y Setters
    public int getIdRecurso() { return idRecurso; }
    public String getTitulo() { return titulo; }
    public String getContenido() { return contenido; }
    public String getCategoria() { return categoria; }
    public int getTiempoLecturaMin() { return tiempoLecturaMin; }
    public boolean isEsFavorito() { return esFavorito; }
    public void setEsFavorito(boolean esFavorito) { this.esFavorito = esFavorito; }
}