package com.conexa.clarity.ui.chat;

public class Mensaje {

    private String texto;
    private final boolean delUsuario;

    public Mensaje(
            String texto,
            boolean delUsuario
    ) {
        this.texto = texto;
        this.delUsuario = delUsuario;
    }

    public String getTexto() {

        return texto;
    }

    public void setTexto(
            String texto
    ) {

        this.texto = texto;
    }

    public boolean esDelUsuario() {

        return delUsuario;
    }
}