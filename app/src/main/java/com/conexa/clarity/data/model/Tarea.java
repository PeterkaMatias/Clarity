package com.conexa.clarity.data.model;

import java.io.Serializable;

public class Tarea implements Serializable {

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

    // Constructor completo para cuando leemos desde la base de datos
    public Tarea(int id, int idUsuario, String titulo, boolean completada, boolean creadaPorIA) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.completada = completada;
        this.creadaPorIA = creadaPorIA;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public boolean isCreadaPorIA() {
        return creadaPorIA;
    }

    public void setCreadaPorIA(boolean creadaPorIA) {
        this.creadaPorIA = creadaPorIA;
    }
}