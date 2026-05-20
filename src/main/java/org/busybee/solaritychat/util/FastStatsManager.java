package org.busybee.solaritychat.util;

import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import dev.faststats.core.data.Metric;
import org.busybee.solaritychat.SolarityChat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FastStatsManager {
    private final SolarityChat plugin;
    private final BukkitMetrics metrics;

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware()
            .anonymize("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "[uuid hidden]")
            .ignoreError(java.lang.reflect.InvocationTargetException.class);

    public FastStatsManager(SolarityChat plugin) {
        this.plugin = plugin;
        String token = loadToken();

        this.metrics = BukkitMetrics.factory()
                .token(token)
                .errorTracker(ERROR_TRACKER)
                .addMetric(Metric.number("channels_total", () -> {
                    return plugin.getChannelManager() != null ? plugin.getChannelManager().getAvailableChannels().size() : 0;
                }))
                .addMetric(Metric.number("tags_total", () -> {
                    if (plugin.getTagManager() == null || plugin.getTagManager().getTagsSection() == null) return 0;
                    return plugin.getTagManager().getTagsSection().getKeys(false).size();
                }))
                .addMetric(Metric.number("filters_total", () -> {
                    return plugin.getFilterManager() != null ? plugin.getFilterManager().getFiltersCount() : 0;
                }))
                .create(plugin);
    }

    private String loadToken() {
        Properties props = new Properties();
        try (InputStream is = plugin.getResource("faststats.properties")) {
            if (is != null) {
                props.load(is);
                return props.getProperty("token", "YOUR_TOKEN_HERE");
            }
        } catch (IOException ignored) {}
        return "YOUR_TOKEN_HERE";
    }

    public void onEnable() {
        metrics.ready();
        plugin.getLogger().info("FastStats metrics have been enabled!");
    }

    public void onDisable() {
        metrics.shutdown();
    }
}
