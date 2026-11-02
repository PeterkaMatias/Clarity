package com.conexa.clarity.data.model;

public class UsuarioLogros {
    private int idUsuarioLogro;
    private int idUsuario;
    private int idLogro;
    private String fechaObtenido;

    public UsuarioLogros(int idUsuarioLogro, int idUsuario, int idLogro, String fechaObtenido) {
        this.idUsuarioLogro = idUsuarioLogro;
        this.idUsuario = idUsuario;
        this.idLogro = idLogro;
        this.fechaObtenido = fechaObtenido;
    }

    public int getIdUsuarioLogro() {
        return idUsuarioLogro;
    }

    public void setIdUsuarioLogro(int idUsuarioLogro) {
        this.idUsuarioLogro = idUsuarioLogro;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdLogro() {
        return idLogro;
    }

    public void setIdLogro(int idLogro) {
        this.idLogro = idLogro;
    }

    public String getFechaObtenido() {
        return fechaObtenido;
    }

    public void setFechaObtenido(String fechaObtenido) {
        this.fechaObtenido = fechaObtenido;
    }
}
