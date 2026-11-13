package com.conexa.clarity.data.model;

public class Recompensas {
    private int idRecompensa;
    private int idUsuario;
    private Integer idTarea;
    private int puntos;
    private String tipo;
    private String descripcion;
    private String fecha;
    private String referencia;

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Recompensas(int idRecompensa, int idUsuario, Integer idTarea,
                       int puntos, String tipo, String descripcion, String fecha){
        this.idRecompensa = idRecompensa;
        this.idUsuario = idUsuario;
        this.idTarea = idTarea;
        this.puntos = puntos;
        this.tipo =tipo;
        this.descripcion =descripcion;
        this.fecha =fecha;


    }

    public int getIdRecompensa() {
        return idRecompensa;
    }

    public void setIdRecompensa(int idRecompensa) {
        this.idRecompensa = idRecompensa;
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
