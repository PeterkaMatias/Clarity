package com.conexa.clarity.data.model;

public class Tarea {

    private int id;
    private int idUsuario;
    private String titulo;
    private boolean completada;
    private boolean creadaPorIA;

    public Tarea(int idUsuario, String titulo) {
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.completada = false;
        this.creadaPorIA = true;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public boolean isCompletada() {
        return completada;
    }
}
