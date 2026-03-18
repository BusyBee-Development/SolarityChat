package org.busybee.solaritychat.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligently merges configuration files, preserving user values while adding new fields.
 */
public class ConfigMerger {

    /**
     * Merges default configuration with existing configuration.
     * User values are preserved, and missing keys from defaults are added.
     *
     * @param existingConfig The current user configuration
     * @param defaultConfig  The default configuration from resources
     * @return Number of fields added
     */
    public static int mergeConfigs(FileConfiguration existingConfig, FileConfiguration defaultConfig) {
        int addedFields = 0;
        addedFields += mergeSection(existingConfig, defaultConfig, "");
        return addedFields;
    }

    /**
     * Recursively merges configuration sections.
     *
     * @param existing The existing configuration section
     * @param defaults The default configuration section
     * @param path     The current path in the configuration tree
     * @return Number of fields added in this section
     */
    private static int mergeSection(ConfigurationSection existing, ConfigurationSection defaults, String path) {
        int addedFields = 0;

        for (String key : defaults.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (!existing.contains(key)) {
                // Key doesn't exist in user config, add it from defaults
                Object defaultValue = defaults.get(key);
                existing.set(key, defaultValue);
                addedFields++;
            } else {
                // Key exists, check if it's a section that needs recursive merge
                if (defaults.get(key) instanceof ConfigurationSection) {
                    ConfigurationSection existingSection = existing.getConfigurationSection(key);
                    ConfigurationSection defaultSection = defaults.getConfigurationSection(key);

                    if (existingSection != null && defaultSection != null) {
                        addedFields += mergeSection(existingSection, defaultSection, fullPath);
                    }
                }
            }
        }

        return addedFields;
    }

    /**
     * Merges configs while attempting to preserve comments.
     *
     * @param existingFile The existing config file
     * @param defaultConfig The default configuration
     * @param outputFile   Where to write the merged result
     * @return Number of fields added
     * @throws IOException If file operations fail
     */
    public static int mergeWithComments(File existingFile, FileConfiguration defaultConfig, File outputFile) throws IOException {
        // Load existing config
        FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(existingFile);

        // Perform merge
        int addedFields = mergeConfigs(existingConfig, defaultConfig);

        // Read existing file with comments
        Map<String, List<String>> existingComments = extractComments(existingFile);

        // Read default comments
        InputStream defaultStream = ConfigMerger.class.getResourceAsStream("/" + existingFile.getName());
        Map<String, List<String>> defaultComments = new HashMap<>();
        if (defaultStream != null) {
            File tempDefault = File.createTempFile("default", ".yml");
            try (FileOutputStream fos = new FileOutputStream(tempDefault)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = defaultStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
            defaultComments = extractComments(tempDefault);
            tempDefault.delete();
            defaultStream.close();
        }

        // Write merged config with comments
        writeConfigWithComments(existingConfig, existingComments, defaultComments, outputFile);

        return addedFields;
    }

    /**
     * Extracts comments from a YAML file.
     *
     * @param file The YAML file
     * @return Map of paths to their comment lines
     * @throws IOException If reading fails
     */
    private static Map<String, List<String>> extractComments(File file) throws IOException {
        Map<String, List<String>> comments = new LinkedHashMap<>();
        List<String> currentComments = new ArrayList<>();
        String currentPath = "";
        Pattern keyPattern = Pattern.compile("^(\\s*)([^#\\s:][^:]*):.*$");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.startsWith("#") || trimmed.isEmpty()) {
                    // Comment or empty line
                    currentComments.add(line);
                } else {
                    // Config key
                    Matcher matcher = keyPattern.matcher(line);
                    if (matcher.matches()) {
                        String key = matcher.group(2).trim();
                        if (!currentComments.isEmpty()) {
                            comments.put(key, new ArrayList<>(currentComments));
                            currentComments.clear();
                        }
                    }
                }
            }
        }

        return comments;
    }

    /**
     * Writes configuration with preserved comments.
     *
     * @param config           The configuration to write
     * @param existingComments Comments from existing config
     * @param defaultComments  Comments from default config
     * @param outputFile       Where to write
     * @throws IOException If writing fails
     */
    private static void writeConfigWithComments(FileConfiguration config,
                                                  Map<String, List<String>> existingComments,
                                                  Map<String, List<String>> defaultComments,
                                                  File outputFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writeSection(config.getRoot(), writer, "", existingComments, defaultComments, 0);
        }
    }

    /**
     * Recursively writes a configuration section with comments.
     */
    private static void writeSection(ConfigurationSection section,
                                       BufferedWriter writer,
                                       String path,
                                       Map<String, List<String>> existingComments,
                                       Map<String, List<String>> defaultComments,
                                       int indent) throws IOException {
        String indentStr = "  ".repeat(indent);

        for (String key : section.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            // Write comments (prefer existing, fallback to default)
            List<String> comments = existingComments.getOrDefault(key, defaultComments.get(key));
            if (comments != null) {
                for (String comment : comments) {
                    writer.write(comment);
                    writer.newLine();
                }
            }

            Object value = section.get(key);

            if (value instanceof ConfigurationSection) {
                // Write section header
                writer.write(indentStr + key + ":");
                writer.newLine();

                // Recursively write section
                writeSection((ConfigurationSection) value, writer, fullPath, existingComments, defaultComments, indent + 1);
            } else {
                // Write key-value pair
                writer.write(indentStr + key + ": " + formatValue(value));
                writer.newLine();
            }
        }
    }

    /**
     * Formats a value for YAML output.
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.contains("\n") || str.contains("\"")) {
                return "\"" + str.replace("\"", "\\\"") + "\"";
            }
            return "\"" + str + "\"";
        } else if (value instanceof List) {
            return value.toString();
        } else {
            return value.toString();
        }
    }
}
