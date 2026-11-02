package com.conexa.clarity.data.model;

public class Logros {
    private int idLogro;
    private String nombre;
    private String descripcion;
    private String requisito;

    public Logros(int idLogro, String nombre, String descripcion, String requisito) {
        this.idLogro = idLogro;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.requisito = requisito;
    }

    public int getIdLogro() {
        return idLogro;
    }

    public void setIdLogro(int idLogro) {
        this.idLogro = idLogro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRequisito() {
        return requisito;
    }

    public void setRequisito(String requisito) {
        this.requisito = requisito;
    }
}
