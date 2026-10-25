package com.srms.config;

import com.srms.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages JDBC connections, schema auto-initialization, and connection health verification.
 */
public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private static volatile DatabaseConnection instance;
    private volatile boolean isConnected;
    private volatile String lastConnectionError = "MySQL connection has not been tested.";

    private DatabaseConnection() {
        initDataSource();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public synchronized void initDataSource() {
        DatabaseConfig cfg = DatabaseConfig.getInstance();
        try (Connection conn = openConnection(cfg)) {
            isConnected = true;
            lastConnectionError = "";
            log.info("Successfully connected to MySQL database: {}", cfg.getUrl());
            if (cfg.isAutoInitSchema()) autoInitSchema(conn);
        } catch (Exception e) {
            if (e instanceof SQLException sqlException) {
                isConnected = false;
                lastConnectionError = describe(sqlException);
                log.warn("MySQL connection failed for {}: {}", cfg.getUrl(), lastConnectionError);
            } else {
                log.error("MySQL schema initialization failed for {}: {}", cfg.getUrl(), e.getMessage(), e);
            }
        }
    }

    public boolean isAvailable() {
        if (isConnected) return true;
        try (Connection conn = openConnection(DatabaseConfig.getInstance())) {
            if (!conn.isValid(2)) return false;
            isConnected = true;
            lastConnectionError = "";
            return true;
        } catch (SQLException e) {
            isConnected = false;
            lastConnectionError = describe(e);
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection conn = openConnection(DatabaseConfig.getInstance());
            isConnected = true;
            lastConnectionError = "";
            return conn;
        } catch (SQLException e) {
            isConnected = false;
            lastConnectionError = describe(e);
            log.error("MySQL connection failed: {}", lastConnectionError);
            throw new DatabaseException("MySQL connection failed: " + lastConnectionError, e);
        }
    }

    public String getLastConnectionError() {
        return lastConnectionError;
    }

    private Connection openConnection(DatabaseConfig cfg) throws SQLException {
        try {
            Class.forName(cfg.getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver is unavailable: " + cfg.getDriverClassName(), e);
        }
        return DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
    }

    private String describe(SQLException e) {
        return "SQLState=" + e.getSQLState() + ", errorCode=" + e.getErrorCode() + ": " + e.getMessage();
    }

    private void autoInitSchema(Connection conn) {
        try {
            log.info("Verifying database schema tables...");
            executeSqlScript(conn, "/sql/schema.sql");
            // Check if users table is empty; if so, populate demo-data
            try (Statement st = conn.createStatement();
                 var rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    log.info("Database is empty. Seeding initial demo data...");
                    executeSqlScript(conn, "/sql/demo-data.sql");
                }
            }
        } catch (Exception e) {
            log.error("Schema initialization encountered an issue: {}", e.getMessage(), e);
        }
    }

    public void executeSqlScript(Connection conn, String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                log.warn("SQL resource script not found: {}", resourcePath);
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sql = new StringBuilder();
            String line;
            try (Statement stmt = conn.createStatement()) {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) {
                        continue;
                    }
                    sql.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String query = sql.toString().trim();
                        // Remove trailing semicolon for execute
                        query = query.substring(0, query.length() - 1);
                        if (!query.isEmpty() && !query.toUpperCase().startsWith("CREATE DATABASE") && !query.toUpperCase().startsWith("USE ")) {
                            try {
                                stmt.execute(query);
                            } catch (SQLException ex) {
                                log.error("SQL initialization statement failed (SQLState={}, errorCode={}): {}",
                                        ex.getSQLState(), ex.getErrorCode(), ex.getMessage(), ex);
                                throw new DatabaseException("SQL initialization failed: " + ex.getMessage(), ex);
                            }
                        }
                        sql.setLength(0);
                    }
                }
            }
            log.info("Executed SQL script: {}", resourcePath);
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to execute script {}: {}", resourcePath, e.getMessage(), e);
            throw new DatabaseException("Failed to execute SQL script " + resourcePath + ": " + e.getMessage(), e);
        }
    }

    public synchronized void close() {
        isConnected = false;
        log.info("Database connection state reset.");
    }
}
