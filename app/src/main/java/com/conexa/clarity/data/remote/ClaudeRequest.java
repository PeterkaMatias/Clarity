package com.conexa.clarity.data.remote;

import java.util.List;

public class ClaudeRequest {

    private String model;
    private int max_tokens;
    private List<Message> messages;

    public ClaudeRequest(String model, int maxTokens, List<Message> messages) {
        this.model = model;
        this.max_tokens = maxTokens;
        this.messages = messages;
    }

    public static class Message {

        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
