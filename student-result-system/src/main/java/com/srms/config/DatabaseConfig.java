package com.srms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Handles database configuration loading and persistence.
 * Checks for external db.properties in the working directory first,
 * then falls back to internal classpath resources.
 */
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String EXTERNAL_CONFIG_FILE = "db.properties";
    private static final String CLASSPATH_CONFIG_FILE = "/db.properties";

    private static final DatabaseConfig INSTANCE = new DatabaseConfig();

    private final Properties properties = new Properties();

    private DatabaseConfig() {
        loadProperties();
    }

    public static DatabaseConfig getInstance() {
        return INSTANCE;
    }

    public synchronized void loadProperties() {
        // 1. Try external file in current directory
        File externalFile = new File(EXTERNAL_CONFIG_FILE);
        if (externalFile.exists() && externalFile.isFile()) {
            try (InputStream in = new FileInputStream(externalFile)) {
                properties.load(in);
                log.info("Loaded database configuration from external file: {}", externalFile.getAbsolutePath());
                return;
            } catch (IOException e) {
                log.warn("Failed to read external db.properties, falling back to classpath: {}", e.getMessage());
            }
        }

        // 2. Fall back to classpath resource
        try (InputStream in = getClass().getResourceAsStream(CLASSPATH_CONFIG_FILE)) {
            if (in != null) {
                properties.load(in);
                log.info("Loaded database configuration from classpath: {}", CLASSPATH_CONFIG_FILE);
            } else {
                log.warn("Classpath db.properties not found; using built-in defaults.");
                setDefaults();
            }
        } catch (IOException e) {
            log.error("Failed to load db.properties from classpath: {}", e.getMessage());
            setDefaults();
        }
    }

    private void setDefaults() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/srms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("db.pool.maxSize", "10");
        properties.setProperty("db.pool.minIdle", "2");
        properties.setProperty("db.pool.idleTimeout", "30000");
        properties.setProperty("db.pool.connectionTimeout", "10000");
        properties.setProperty("db.autoInitSchema", "true");
    }

    public String getUrl() {
        return properties.getProperty("db.url", "jdbc:mysql://localhost:3306/srms_db");
    }

    public String getUsername() {
        return properties.getProperty("db.username", "root");
    }

    public String getPassword() {
        return properties.getProperty("db.password", "");
    }

    public String getDriverClassName() {
        return properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public int getMaxPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.maxSize", "10"));
    }

    public int getMinIdle() {
        return Integer.parseInt(properties.getProperty("db.pool.minIdle", "2"));
    }

    public long getIdleTimeout() {
        return Long.parseLong(properties.getProperty("db.pool.idleTimeout", "30000"));
    }

    public long getConnectionTimeout() {
        return Long.parseLong(properties.getProperty("db.pool.connectionTimeout", "10000"));
    }

    public boolean isAutoInitSchema() {
        return Boolean.parseBoolean(properties.getProperty("db.autoInitSchema", "true"));
    }

    public synchronized void updateCredentials(String url, String username, String password) {
        properties.setProperty("db.url", url);
        properties.setProperty("db.username", username);
        properties.setProperty("db.password", password);
        saveExternal();
    }

    public synchronized void saveExternal() {
        try (OutputStream out = Files.newOutputStream(Path.of(EXTERNAL_CONFIG_FILE))) {
            properties.store(out, "SRMS Database Configuration");
            log.info("Saved external db.properties successfully.");
        } catch (IOException e) {
            log.error("Failed to save external db.properties: {}", e.getMessage());
        }
    }
}
