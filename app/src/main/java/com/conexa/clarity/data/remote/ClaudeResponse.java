package com.conexa.clarity.data.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClaudeResponse {

    private List<ContentBlock> content;

    @SerializedName("stop_reason")
    private String stopReason;

    public String obtenerTexto() {

        if (content == null
                || content.isEmpty()) {

            return "";
        }

        StringBuilder resultado =
                new StringBuilder();

        for (ContentBlock bloque : content) {

            if (bloque == null) {
                continue;
            }

            if ("text".equals(bloque.type)
                    && bloque.text != null
                    && !bloque.text.trim().isEmpty()) {

                if (resultado.length() > 0) {

                    resultado.append(
                            "\n"
                    );
                }

                resultado.append(
                        bloque.text.trim()
                );
            }
        }

        return resultado
                .toString()
                .trim();
    }

    public String getStopReason() {

        return stopReason;
    }

    public static class ContentBlock {

        private String type;
        private String text;

        public String getType() {

            return type;
        }

        public String getText() {

            return text;
        }
    }
}