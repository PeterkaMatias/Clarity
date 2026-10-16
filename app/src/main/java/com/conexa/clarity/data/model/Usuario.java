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

    public Usuario(int idUsuario, String nombre, String apellido, String email, String tipoPerfil) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.tipoPerfil = tipoPerfil;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getTipoPerfil() { return tipoPerfil; }

    public boolean esParental() { return PERFIL_PARENTAL.equals(tipoPerfil); }
}