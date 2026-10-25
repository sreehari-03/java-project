package com.srms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = setting(
            "SRMS_DB_URL",
            "jdbc:mysql://localhost:3306/srms_db?createDatabaseIfNotExist=true");
    private static final String USER = setting("SRMS_DB_USER", "root");
    private static final String PASSWORD = setting("SRMS_DB_PASSWORD", "");

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        try {
            initializeSchema(connection);
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS students (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        admission_no VARCHAR(64) NOT NULL UNIQUE,
                        full_name VARCHAR(150) NOT NULL,
                        email VARCHAR(254) NOT NULL,
                        class_name VARCHAR(64) NOT NULL,
                        school VARCHAR(150) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'Active'
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(64) NOT NULL UNIQUE,
                        password_hash VARCHAR(100) NOT NULL,
                        role VARCHAR(16) NOT NULL,
                        full_name VARCHAR(150) NOT NULL,
                        registration_no VARCHAR(64) NOT NULL,
                        student_id BIGINT NULL,
                        CONSTRAINT fk_accounts_student
                            FOREIGN KEY (student_id) REFERENCES students(id)
                    )
                    """);
        }
    }
}
