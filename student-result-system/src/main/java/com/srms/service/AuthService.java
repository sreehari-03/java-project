package com.srms.service;

import com.srms.config.DatabaseConnection;
import com.srms.dao.StudentDAO;
import com.srms.dao.UserDAO;
import com.srms.exception.AuthenticationException;
import com.srms.model.Student;
import com.srms.model.User;
import com.srms.util.BCryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Handles user authentication and session management.
 * Works transparently whether MySQL is connected or not (in-memory fallback).
 */
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    // Session state
    private static User currentUser;
    private static Student currentStudent;

    /**
     * Authenticates username + plaintext password.
     * Returns the authenticated user or throws AuthenticationException.
     */
    public User login(String username, String plainPassword, String requiredRole) {
        if (username == null || username.isBlank()) {
            throw new AuthenticationException("Username is required.");
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new AuthenticationException("Password is required.");
        }

        if (DatabaseConnection.getInstance().isAvailable()) {
            return loginFromDatabase(username, plainPassword, requiredRole);
        } else {
            return loginFromMemory(username, plainPassword, requiredRole);
        }
    }

    private User loginFromDatabase(String username, String plainPassword, String requiredRole) {
        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid username or password.");
        }
        User user = userOpt.get();
        if (!BCryptUtil.verify(plainPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }
        if (requiredRole != null && !requiredRole.equalsIgnoreCase(user.getRole())) {
            throw new AuthenticationException("This account is a " + user.getRole() + " account. Please use the correct login tab.");
        }
        // Upgrade plaintext hash if needed
        if (!user.getPasswordHash().startsWith("$2")) {
            try {
                userDAO.updatePasswordHash(user.getId(), BCryptUtil.hash(plainPassword));
            } catch (Exception ignored) {}
        }
        currentUser = user;
        // If student, fetch their student record
        if ("student".equals(user.getRole())) {
            currentStudent = studentDAO.findByUserId(user.getId()).orElse(null);
        }
        log.info("User '{}' logged in as '{}'", user.getUsername(), user.getRole());
        return user;
    }

    private User loginFromMemory(String username, String plainPassword, String requiredRole) {
        // Offline fallback credentials
        if ("teacher".equalsIgnoreCase(username) && "teacher123".equals(plainPassword)) {
            if (requiredRole != null && !"teacher".equalsIgnoreCase(requiredRole)) {
                throw new AuthenticationException("This is a teacher account. Please switch to the Teacher tab.");
            }
            User u = new User(1, "teacher", BCryptUtil.hash("teacher123"), "teacher", "Ms. Sharma", "sharma@school.edu");
            currentUser = u;
            return u;
        }
        if ("student".equalsIgnoreCase(username) && "student123".equals(plainPassword)) {
            if (requiredRole != null && !"student".equalsIgnoreCase(requiredRole)) {
                throw new AuthenticationException("This is a student account. Please switch to the Student tab.");
            }
            User u = new User(3, "student", BCryptUtil.hash("student123"), "student", "Rohan Mehta", "rohan@mail.com");
            currentUser = u;
            return u;
        }
        throw new AuthenticationException("Invalid username or password. (Database offline — only demo logins accepted)");
    }

    public void logout() {
        currentUser = null;
        currentStudent = null;
        log.info("User logged out.");
    }

    public static User getCurrentUser() { return currentUser; }
    public static Student getCurrentStudent() { return currentStudent; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static void setCurrentStudent(Student s) { currentStudent = s; }
    public static boolean isLoggedIn() { return currentUser != null; }
}
