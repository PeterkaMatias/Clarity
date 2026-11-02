package com.conexa.clarity.ui.chat;

import com.conexa.clarity.data.remote.ClaudeRequest;

import java.util.ArrayList;
import java.util.List;

public final class ConversationMemory {

    private static final int MAX_MENSAJES = 6;

    private static final List<ClaudeRequest.Message> mensajes =
            new ArrayList<>();

    private ConversationMemory() {
        // Evita crear instancias de esta clase.
    }

    public static synchronized void agregarMensajeUsuario(
            String texto
    ) {

        agregarMensaje(
                new ClaudeRequest.Message(
                        "user",
                        texto
                )
        );
    }

    public static synchronized void agregarMensajeLumi(
            String texto
    ) {

        agregarMensaje(
                new ClaudeRequest.Message(
                        "assistant",
                        texto
                )
        );
    }

    private static void agregarMensaje(
            ClaudeRequest.Message mensaje
    ) {

        mensajes.add(mensaje);

        while (mensajes.size() > MAX_MENSAJES) {
            mensajes.remove(0);
        }
    }

    public static synchronized List<ClaudeRequest.Message>
    obtenerMensajes() {

        return new ArrayList<>(
                mensajes
        );
    }

    public static synchronized void borrarMemoria() {

        mensajes.clear();
    }

    public static synchronized int cantidadMensajes() {

        return mensajes.size();
    }
}