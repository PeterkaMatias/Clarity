package com.conexa.clarity.data.model;

public class Temporizador {
    private int idTemporizador;
    private int idUsuario;
    private Integer idTarea;
    private int duracion;
    private String fechaInicio;
    private String fechaFin;
    private String estado;

    public Temporizador(int idTemporizador, int idUsuario, Integer idTarea,
                        int duracion, String fechaInicio, String fechaFin, String estado){
        this.idTemporizador = idTemporizador;
        this.idUsuario = idUsuario;
        this.idTarea = idTarea;
        this.duracion = duracion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdTemporizador() {
        return idTemporizador;
    }

    public void setIdTemporizador(int idTemporizador) {
        this.idTemporizador = idTemporizador;
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

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
