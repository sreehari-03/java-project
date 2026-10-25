package com.srms.dao;

import com.srms.config.DatabaseConnection;
import com.srms.exception.DatabaseException;
import com.srms.model.Subject;
import com.srms.model.AcademicTerm;
import com.srms.model.ClassRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * DAO for static reference data: subjects, terms, classes.
 */
public class ReferenceDataDAO {
    private static final Logger log = LoggerFactory.getLogger(ReferenceDataDAO.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    // ---- SUBJECTS ----

    public List<Subject> findAllSubjects() {
        if (!db.isAvailable()) return fallbackSubjects();
        final String sql = "SELECT id, subject_code, subject_name, max_marks, pass_marks, description FROM subjects ORDER BY subject_name";
        List<Subject> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("id"),
                        rs.getString("subject_code"),
                        rs.getString("subject_name"),
                        rs.getInt("max_marks"),
                        rs.getInt("pass_marks"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            log.error("findAllSubjects error: {}", e.getMessage());
            return fallbackSubjects();
        }
        return list.isEmpty() ? fallbackSubjects() : list;
    }

    public Optional<Subject> findSubjectById(int id) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = "SELECT id, subject_code, subject_name, max_marks, pass_marks, description FROM subjects WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Subject(
                            rs.getInt("id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            rs.getInt("max_marks"),
                            rs.getInt("pass_marks"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("findSubjectById error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Subject> findSubjectByName(String name) {
        if (!db.isAvailable()) {
            return fallbackSubjects().stream().filter(s -> s.getSubjectName().equalsIgnoreCase(name)).findFirst();
        }
        final String sql = "SELECT id, subject_code, subject_name, max_marks, pass_marks, description FROM subjects WHERE subject_name = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Subject(
                            rs.getInt("id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            rs.getInt("max_marks"),
                            rs.getInt("pass_marks"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("findSubjectByName error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // ---- ACADEMIC TERMS ----

    public List<AcademicTerm> findAllTerms() {
        if (!db.isAvailable()) return fallbackTerms();
        final String sql = "SELECT id, term_name, academic_year, is_active FROM academic_terms ORDER BY id";
        List<AcademicTerm> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AcademicTerm(
                        rs.getInt("id"),
                        rs.getString("term_name"),
                        rs.getString("academic_year"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
            log.error("findAllTerms error: {}", e.getMessage());
            return fallbackTerms();
        }
        return list.isEmpty() ? fallbackTerms() : list;
    }

    public Optional<AcademicTerm> findTermById(int id) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = "SELECT id, term_name, academic_year, is_active FROM academic_terms WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AcademicTerm(
                            rs.getInt("id"),
                            rs.getString("term_name"),
                            rs.getString("academic_year"),
                            rs.getBoolean("is_active")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("findTermById error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<AcademicTerm> findTermByName(String name) {
        if (!db.isAvailable()) {
            return fallbackTerms().stream().filter(t -> t.getTermName().equalsIgnoreCase(name)).findFirst();
        }
        final String sql = "SELECT id, term_name, academic_year, is_active FROM academic_terms WHERE term_name = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AcademicTerm(
                            rs.getInt("id"),
                            rs.getString("term_name"),
                            rs.getString("academic_year"),
                            rs.getBoolean("is_active")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("findTermByName error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // ---- CLASSES ----

    public List<ClassRoom> findAllClasses() {
        if (!db.isAvailable()) return fallbackClasses();
        final String sql = "SELECT id, class_name, section, academic_year FROM classes ORDER BY class_name, section";
        List<ClassRoom> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ClassRoom(rs.getInt("id"), rs.getString("class_name"),
                        rs.getString("section"), rs.getString("academic_year")));
            }
        } catch (SQLException e) {
            log.error("findAllClasses error: {}", e.getMessage());
            return fallbackClasses();
        }
        return list.isEmpty() ? fallbackClasses() : list;
    }

    public Optional<ClassRoom> findClassById(int id) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = "SELECT id, class_name, section, academic_year FROM classes WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ClassRoom(rs.getInt("id"), rs.getString("class_name"),
                            rs.getString("section"), rs.getString("academic_year")));
                }
            }
        } catch (SQLException e) {
            log.error("findClassById error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    // ---- Fallbacks for offline mode ----

    private List<Subject> fallbackSubjects() {
        return List.of(
                new Subject(1, "MATH101", "Mathematics", 100, 35, null),
                new Subject(2, "SCI101", "Science", 100, 35, null),
                new Subject(3, "ENG101", "English", 100, 35, null),
                new Subject(4, "HIST101", "History", 100, 35, null),
                new Subject(5, "CS101", "Computer Science", 100, 35, null)
        );
    }

    private List<AcademicTerm> fallbackTerms() {
        return List.of(
                new AcademicTerm(1, "Term 1 (2025 - 2026)", "2025-2026", true),
                new AcademicTerm(2, "Term 2 (2025 - 2026)", "2025-2026", false),
                new AcademicTerm(3, "Final Term (2025 - 2026)", "2025-2026", false)
        );
    }

    private List<ClassRoom> fallbackClasses() {
        return List.of(
                new ClassRoom(1, "Class 10", "A", "2025-2026"),
                new ClassRoom(2, "Class 11", "A", "2025-2026"),
                new ClassRoom(3, "Class 12", "A", "2025-2026")
        );
    }
}
