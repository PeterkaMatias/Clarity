package com.conexa.clarity.data.remote;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ClaudeRequest {

    private final String model;

    @SerializedName("max_tokens")
    private final int maxTokens;

    private final String system;

    private final List<Message> messages;

    public ClaudeRequest(
            String model,
            int maxTokens,
            String system,
            List<Message> messages
    ) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.system = system;

        this.messages = new ArrayList<>(
                messages
        );
    }

    public static class Message {

        private final String role;
        private final String content;

        public Message(
                String role,
                String content
        ) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}