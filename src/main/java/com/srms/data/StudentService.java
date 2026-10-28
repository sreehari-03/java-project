package com.srms.data;

import com.srms.config.Database;
import com.srms.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class StudentService {

    private StudentService() {
    }

    public static Student findForAccount(String username) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            String studentIdColumn = studentIdColumn(connection);
            String sql = """
                    SELECT s.admission_no, s.full_name, s.class_name,
                           s.email, s.school, s.status
                    FROM accounts a
                    JOIN students s ON s.%s = a.student_id
                    WHERE a.username = ?
                    """.formatted(studentIdColumn);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next() ? readStudent(result) : null;
                }
            }
        }
    }

    private static String studentIdColumn(Connection connection) throws SQLException {
        try (ResultSet primaryKeys = connection.getMetaData()
                .getPrimaryKeys(connection.getCatalog(), null, "students")) {
            if (primaryKeys.next()) {
                String column = primaryKeys.getString("COLUMN_NAME");
                if ("student_id".equalsIgnoreCase(column) || "id".equalsIgnoreCase(column)) {
                    return column;
                }
            }
        }
        throw new SQLException("Could not determine the primary key column for students.");
    }

    public static List<Student> findAll() throws SQLException {
        try (Connection connection = Database.getConnection()) {
            String studentIdColumn = studentIdColumn(connection);
            String sql = """
                    SELECT DISTINCT s.admission_no, s.full_name, s.class_name,
                                    s.email, s.school, s.status
                    FROM students s
                    JOIN accounts a ON a.student_id = s.%s
                    WHERE LOWER(a.role) = 'student'
                    ORDER BY s.admission_no
                    """.formatted(studentIdColumn);
            List<Student> students = new ArrayList<>();

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    students.add(readStudent(result));
                }
            }
            return students;
        }
    }

    private static Student readStudent(ResultSet result) throws SQLException {
        Student student = new Student(
                result.getString("admission_no"),
                result.getString("full_name"),
                result.getString("class_name"),
                result.getString("email"),
                result.getString("school"));
        student.status = result.getString("status");
        return student;
    }
}
