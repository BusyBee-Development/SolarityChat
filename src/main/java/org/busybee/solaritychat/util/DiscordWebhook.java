package org.busybee.solaritychat.util;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiscordWebhook {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String url) {
        this.url = url;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public void setTts(boolean tts) {
        this.tts = tts;
    }
    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() throws IOException {
        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        StringBuilder json = new StringBuilder();
        json.append("{");

        if (content != null) {
            json.append("\"content\":\"").append(escapeJson(content)).append("\",");
        }
        if (username != null) {
            json.append("\"username\":\"").append(escapeJson(username)).append("\",");
        }
        if (avatarUrl != null) {
            json.append("\"avatar_url\":\"").append(escapeJson(avatarUrl)).append("\",");
        }
        json.append("\"tts\":").append(tts);

        if (!embeds.isEmpty()) {
            json.append(",\"embeds\":[");
            for (int i = 0; i < embeds.size(); i++) {
                json.append(embeds.get(i).toJson());
                if (i != embeds.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
        }

        json.append("}");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.url))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "SolarityChat")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    public static class EmbedObject {
        private String title;
        private String description;
        private String url;
        private Color color;
        private String timestamp;

        public EmbedObject setTitle(String title) {
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(String description) {
            this.description = description;
            return this;
        }

        public EmbedObject setUrl(String url) {
            this.url = url;
            return this;
        }

        public EmbedObject setColor(Color color) {
            this.color = color;
            return this;
        }

        public EmbedObject setTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public EmbedObject setCurrentTimestamp() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            this.timestamp = sdf.format(new Date());
            return this;
        }

        String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");

            if (title != null) {
                json.append("\"title\":\"").append(escapeJson(title)).append("\",");
            }
            if (description != null) {
                json.append("\"description\":\"").append(escapeJson(description)).append("\",");
            }
            if (url != null) {
                json.append("\"url\":\"").append(escapeJson(url)).append("\",");
            }
            if (color != null) {
                json.append("\"color\":").append(color.getRGB() & 0xFFFFFF).append(",");
            }
            if (timestamp != null) {
                json.append("\"timestamp\":\"").append(timestamp).append("\",");
            }

            if (json.charAt(json.length() - 1) == ',') {
                json.setLength(json.length() - 1);
            }

            json.append("}");
            return json.toString();
        }

        private String escapeJson(String text) {
            return text.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t");
        }
    }
}
