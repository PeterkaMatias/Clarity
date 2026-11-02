package com.conexa.clarity.ui.chat;

public class Mensaje {

    private final String texto;
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

    public boolean esDelUsuario() {
        return delUsuario;
    }
}