package com.srms.data;

import com.srms.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ResultService {

    private ResultService() {
    }

    public record SavedResult(String admissionNo, String term, int[] marks, String remark) {
    }

    public static List<SavedResult> findAll() throws SQLException {
        String sql = """
                SELECT admission_no, term, mathematics, science, english,
                       social_studies, computer_science, teacher_remarks
                FROM srms_portal_results
                """;
        List<SavedResult> savedResults = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                int[] marks = readMarks(result);
                savedResults.add(new SavedResult(
                        result.getString("admission_no"),
                        result.getString("term"),
                        marks,
                        result.getString("teacher_remarks")));
            }
        }
        return savedResults;
    }

    public static void save(String admissionNo, String term, int[] marks, String remark)
            throws SQLException {
        if (marks.length != 5) {
            throw new IllegalArgumentException("Exactly five subject marks are required.");
        }

        String sql = """
                INSERT INTO srms_portal_results
                    (admission_no, term, mathematics, science, english,
                     social_studies, computer_science, teacher_remarks)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    mathematics = VALUES(mathematics),
                    science = VALUES(science),
                    english = VALUES(english),
                    social_studies = VALUES(social_studies),
                    computer_science = VALUES(computer_science),
                    teacher_remarks = VALUES(teacher_remarks)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, admissionNo);
            statement.setString(2, term);
            for (int i = 0; i < marks.length; i++) {
                statement.setInt(i + 3, marks[i]);
            }
            statement.setString(8, remark);
            statement.executeUpdate();
        }
    }

    public static void saveTeacherFeedback(String admissionNo, String term, String remark)
            throws SQLException {
        String sql = """
                INSERT INTO srms_portal_results
                    (admission_no, term, teacher_remarks)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE teacher_remarks = VALUES(teacher_remarks)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, admissionNo);
            statement.setString(2, term);
            statement.setString(3, remark);
            statement.executeUpdate();
        }
    }

    private static int[] readMarks(ResultSet result) throws SQLException {
        String[] columns = {
            "mathematics", "science", "english", "social_studies", "computer_science"
        };
        int[] marks = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            marks[i] = result.getInt(columns[i]);
            if (result.wasNull()) {
                return null;
            }
        }
        return marks;
    }
}
