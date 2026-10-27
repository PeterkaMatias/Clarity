package com.conexa.clarity.data.model;

import java.io.Serializable;

public class Usuario implements Serializable {

    public static final String PERFIL_GENERAL = "GENERAL";
    public static final String PERFIL_PARENTAL = "PARENTAL";

    private final int idUsuario;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final String tipoPerfil;
    private int puntosTotales;
    private int nivel;

    public Usuario(int idUsuario, String nombre, String apellido, String email, String tipoPerfil) {
        this(idUsuario, nombre, apellido, email, tipoPerfil, 0, 1);
    }

    public Usuario(int idUsuario, String nombre, String apellido, String email, String tipoPerfil, int puntosTotales, int nivel) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.tipoPerfil = tipoPerfil;
        this.puntosTotales = puntosTotales;
        this.nivel = nivel;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getTipoPerfil() { return tipoPerfil; }
    public int getPuntosTotales() { return puntosTotales; }
    public void setPuntosTotales(int puntosTotales) { this.puntosTotales = puntosTotales; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public boolean esParental() { return PERFIL_PARENTAL.equals(tipoPerfil); }
}