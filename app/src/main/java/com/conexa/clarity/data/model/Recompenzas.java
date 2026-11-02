package com.conexa.clarity.data.model;

public class Recompenzas {
    private int idRecompenza;
    private int idUsuario;
    private Integer idTarea;
    private int puntos;
    private String tipo;
    private String descripcion;
    private String fecha;

    public Recompenzas(int idRecompenza, int idUsuario, Integer idTarea,
                       int puntos, String tipo, String descripcion, String fecha){
        this.idRecompenza = idRecompenza;
        this.idUsuario = idUsuario;
        this.idTarea = idTarea;
        this.puntos = puntos;
        this.tipo =tipo;
        this.descripcion =descripcion;
        this.fecha =fecha;


    }

    public int getIdRecompenza() {
        return idRecompenza;
    }

    public void setIdRecompenza(int idRecompenza) {
        this.idRecompenza = idRecompenza;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdTarea() {
        return idTarea;
    }

    public void setIdTarea(Integer idTarea) {
        this.idTarea = idTarea;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
