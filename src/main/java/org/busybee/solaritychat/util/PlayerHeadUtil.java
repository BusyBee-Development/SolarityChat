package org.busybee.solaritychat.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerHeadUtil {

    private static final int HEAD_WIDTH = 8;
    private static final int HEAD_HEIGHT = 8;
    private static final String HEAD_PIXEL = "█";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static String getSkinUrl(Player player) {
        String urlString = PlaceholderAPI.setPlaceholders(player, "%player_skin_url%");
        if (urlString.isEmpty() || urlString.equals("%player_skin_url%")) {
            urlString = "https://minotar.net/skin/" + player.getUniqueId();
        }
        return urlString;
    }

    public static BufferedImage downloadSkin(String urlString) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode());
            }
            try (InputStream is = response.body()) {
                return ImageIO.read(is);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public static List<String> getHeadLinesFromSkin(BufferedImage skin) {
        if (skin == null) {
            return Collections.emptyList();
        }

        BufferedImage head = skin.getSubimage(8, 8, HEAD_WIDTH, HEAD_HEIGHT);
        BufferedImage helm = skin.getSubimage(40, 8, HEAD_WIDTH, HEAD_HEIGHT);
        BufferedImage combined = new BufferedImage(HEAD_WIDTH, HEAD_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = combined.createGraphics();
        g.drawImage(head, 0, 0, null);
        g.drawImage(helm, 0, 0, null);
        g.dispose();

        List<String> lines = new ArrayList<>();
        for (int y = 0; y < HEAD_HEIGHT; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < HEAD_WIDTH; x++) {
                Color color = new Color(combined.getRGB(x, y), true);
                if (color.getAlpha() > 0) {
                    line.append("<color:#").append(String.format("%06x", color.getRGB() & 0xFFFFFF)).append(">").append(HEAD_PIXEL);
                } else {
                    line.append(" ");
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }
}