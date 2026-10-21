package com.conexa.clarity.data.remote;

import java.util.List;

public class ClaudeResponse {

    public List<Content> content;

    public static class Content {
        public String type;
        public String text;
    }

    public String obtenerTexto() {

        if (content == null || content.isEmpty()) {
            return "";
        }

        return content.get(0).text;
    }
}