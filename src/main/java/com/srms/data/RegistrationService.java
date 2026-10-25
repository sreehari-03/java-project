package com.srms.data;

import com.srms.config.Database;
import com.srms.model.Account;
import com.srms.model.Student;
import com.srms.security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class RegistrationService {

    private RegistrationService() {
    }

    public static void register(Account account, Student student)
            throws SQLException {

        String studentSql = """
                INSERT INTO students
                    (admission_no, full_name, email, class_name, school, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String accountSql = """
                INSERT INTO accounts
                    (username, password_hash, role, full_name, registration_no, student_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Long studentId = null;

                if (student != null) {
                    try (PreparedStatement statement
                            = connection.prepareStatement(
                                    studentSql,
                                    PreparedStatement.RETURN_GENERATED_KEYS)) {

                        statement.setString(1, student.reg);
                        statement.setString(2, student.name);
                        statement.setString(3, student.email);
                        statement.setString(4, student.cls);
                        statement.setString(5, student.school);
                        statement.setString(6, student.status);
                        statement.executeUpdate();

                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (keys.next()) {
                                studentId = keys.getLong(1);
                            } else {
                                throw new SQLException(
                                        "Could not retrieve the new student ID.");
                            }
                        }
                    }
                }

                try (PreparedStatement statement
                        = connection.prepareStatement(accountSql)) {

                    statement.setString(1, account.username());
                    statement.setString(2,
                            PasswordUtil.hash(account.password()));
                    statement.setString(3,
                            account.role().toUpperCase());
                    statement.setString(4, account.name());
                    statement.setString(5, account.reg());

                    if (studentId == null) {
                        statement.setNull(6, java.sql.Types.BIGINT);
                    } else {
                        statement.setLong(6, studentId);
                    }

                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
