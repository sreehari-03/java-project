package com.srms.service;

import com.srms.config.DatabaseConnection;
import com.srms.dao.*;
import com.srms.exception.DatabaseException;
import com.srms.exception.ValidationException;
import com.srms.model.*;
import com.srms.util.BCryptUtil;
import com.srms.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service layer for Student management operations.
 */
public class StudentService {
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ReferenceDataDAO refDAO = new ReferenceDataDAO();
    private final ActivityLogDAO activityDAO = new ActivityLogDAO();

    /**
     * Returns all students, joined with class names.
     */
    public List<Student> getAllStudents() {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return getInMemoryStudents();
        }
        return studentDAO.findAll();
    }

    public Optional<Student> findByRegNo(String regNo) {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return getInMemoryStudents().stream().filter(s -> s.reg.equalsIgnoreCase(regNo)).findFirst();
        }
        return studentDAO.findByRegNo(regNo);
    }

    public Optional<Student> findByUserId(int userId) {
        if (!DatabaseConnection.getInstance().isAvailable()) return Optional.empty();
        return studentDAO.findByUserId(userId);
    }

    /**
     * Validates and creates a new student, optionally creating a linked user account.
     */
    public Student createStudent(String regNo, String fullName, String cls, String email, String phone, String school,
                                  String username, String password) {
        if (!ValidationUtil.isValidRegNo(regNo)) throw new ValidationException("Invalid registration number. Use 3-20 alphanumeric characters.");
        if (!ValidationUtil.isValidName(fullName)) throw new ValidationException("Name must be 2-100 characters.");
        if (!ValidationUtil.isValidEmail(email)) throw new ValidationException("Invalid email address.");

        if (!DatabaseConnection.getInstance().isAvailable()) {
            throw new DatabaseException("Cannot add student because MySQL is unavailable. Check that XAMPP MySQL is running and verify db.url, db.username, and db.password in db.properties. "
                    + DatabaseConnection.getInstance().getLastConnectionError());
        }

        if (studentDAO.existsByRegNo(regNo)) throw new ValidationException("Registration number '" + regNo + "' is already used.");
        if (studentDAO.existsByEmail(email)) throw new ValidationException("Email '" + email + "' is already registered.");

        // Find matching class_id
        int classId = resolveClassId(cls);

        Student s = new Student();
        s.setReg(regNo);
        s.setName(fullName);
        s.setCls(cls);
        s.setClassId(classId);
        s.setEmail(email);
        s.setPhone(phone);
        s.setSchool(school != null && !school.isBlank() ? school : "Sunrise Public School");
        s.setStatus("Active");

        // Optionally create user account
        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            if (userDAO.existsByUsername(username)) throw new ValidationException("Username '" + username + "' is already taken.");
            User u = new User();
            u.setUsername(username);
            u.setPasswordHash(BCryptUtil.hash(password));
            u.setRole("student");
            u.setFullName(fullName);
            u.setEmail(email);
            int uid = userDAO.insert(u);
            s.setUserId(uid);
        }

        int id = studentDAO.insert(s);
        s.setId(id);
        activityDAO.log(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null,
                "STUDENT_ADD", "New student registered: " + fullName + " (" + regNo + ")");
        return s;
    }

    public boolean updateStudent(Student s) {
        if (!DatabaseConnection.getInstance().isAvailable()) return true; // Optimistic
        int classId = resolveClassId(s.getCls());
        s.setClassId(classId);
        boolean ok = studentDAO.update(s);
        if (ok) {
            activityDAO.log(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null,
                    "STUDENT_UPDATE", "Updated student: " + s.getName() + " (" + s.getReg() + ")");
        }
        return ok;
    }

    public boolean deactivateStudent(int studentId, String name) {
        if (!DatabaseConnection.getInstance().isAvailable()) return true;
        boolean ok = studentDAO.updateStatus(studentId, "Inactive");
        if (ok) activityDAO.log(null, "STUDENT_DEACTIVATE", "Deactivated student: " + name);
        return ok;
    }

    public boolean deleteStudent(int studentId, String name) {
        if (!DatabaseConnection.getInstance().isAvailable()) return true;
        boolean ok = studentDAO.delete(studentId);
        if (ok) activityDAO.log(null, "STUDENT_DELETE", "Deleted student: " + name);
        return ok;
    }

    public int getTotalStudentCount() {
        if (!DatabaseConnection.getInstance().isAvailable()) return getInMemoryStudents().size();
        return studentDAO.countAll();
    }

    public List<String[]> getCountByClass() {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            Map<String, Integer> map = new LinkedHashMap<>();
            for (Student s : getInMemoryStudents()) {
                map.merge(s.cls, 1, Integer::sum);
            }
            List<String[]> rows = new ArrayList<>();
            map.forEach((k, v) -> rows.add(new String[]{k, String.valueOf(v)}));
            return rows;
        }
        return studentDAO.countByClass();
    }

    public List<ClassRoom> getAllClasses() {
        return refDAO.findAllClasses();
    }

    private int resolveClassId(String displayName) {
        List<ClassRoom> classes = refDAO.findAllClasses();
        return classes.stream()
                .filter(c -> c.toString().equalsIgnoreCase(displayName) || c.getClassName().equalsIgnoreCase(displayName))
                .findFirst()
                .map(ClassRoom::getId)
                .orElse(1); // Default to first class if not found
    }

    // Offline in-memory fallback data
    private List<Student> getInMemoryStudents() {
        String sc = "Sunrise Public School";
        return Arrays.asList(
                new Student("REG001", "Rohan Mehta", "Class 10 (A)", "rohan@mail.com", sc),
                new Student("REG002", "Sneha Iyer", "Class 11 (A)", "sneha@mail.com", sc),
                new Student("REG003", "Arjun Das", "Class 12 (A)", "arjun@mail.com", sc),
                new Student("REG004", "Priya Nair", "Class 10 (A)", "priya@mail.com", sc),
                new Student("REG005", "Karan Singh", "Class 11 (A)", "karan@mail.com", sc),
                new Student("REG006", "Ananya Reddy", "Class 12 (A)", "ananya@mail.com", sc),
                new Student("REG007", "Vishal Gupta", "Class 10 (A)", "vishal@mail.com", sc),
                new Student("REG008", "Meera Joshi", "Class 11 (A)", "meera@mail.com", sc)
        );
    }
}
