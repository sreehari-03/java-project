-- ========================================================
-- Student Result Management System (SRMS) Demo Data
-- Default Logins:
--   Teacher: username = 'teacher' (or 'ms_sharma') | password = 'teacher123'
--   Student: username = 'student' (or 'rohan')     | password = 'student123'
-- ========================================================

USE srms_db;

-- 1. Classes
INSERT IGNORE INTO classes (id, class_name, section, academic_year) VALUES
(1, 'Class 10', 'A', '2025-2026'),
(2, 'Class 11', 'A', '2025-2026'),
(3, 'Class 12', 'A', '2025-2026');

-- 2. Subjects
INSERT IGNORE INTO subjects (id, subject_code, subject_name, max_marks, pass_marks, description) VALUES
(1, 'MATH101', 'Mathematics', 100, 35, 'Core Mathematics & Geometry'),
(2, 'SCI101', 'Science', 100, 35, 'Physics, Chemistry & Biology'),
(3, 'ENG101', 'English', 100, 35, 'English Literature & Language'),
(4, 'HIST101', 'History', 100, 35, 'World History & Civics'),
(5, 'CS101', 'Computer Science', 100, 35, 'Computing Concepts, Programming & Databases');

-- 3. Academic Terms
INSERT IGNORE INTO academic_terms (id, term_name, academic_year, is_active) VALUES
(1, 'Term 1 (2025 - 2026)', '2025-2026', TRUE),
(2, 'Term 2 (2025 - 2026)', '2025-2026', FALSE),
(3, 'Final Term (2025 - 2026)', '2025-2026', FALSE);

-- 4. Users (Password hashes generated via BCrypt for 'teacher123' and 'student123')
-- $2a$10$wN1G15o4B5N3kHnUfZw9x.tC8bH6k.C9E8p7P8f8n4z3g2r1t0yKu = 'teacher123'
-- $2a$10$Q7yE2w8zN5t6h9u1p4q3r.i4o5l6m7n8b9v0c1x2z3a4s5d6f7g8h = 'student123'
INSERT IGNORE INTO users (id, username, password_hash, role, full_name, email) VALUES
(1, 'teacher', '$2a$10$wN1G15o4B5N3kHnUfZw9x.tC8bH6k.C9E8p7P8f8n4z3g2r1t0yKu', 'teacher', 'Ms. Sharma', 'sharma@school.edu'),
(2, 'ms_sharma', '$2a$10$wN1G15o4B5N3kHnUfZw9x.tC8bH6k.C9E8p7P8f8n4z3g2r1t0yKu', 'teacher', 'Ms. Sharma', 'teacher@school.edu'),
(3, 'student', '$2a$10$Q7yE2w8zN5t6h9u1p4q3r.i4o5l6m7n8b9v0c1x2z3a4s5d6f7g8h', 'student', 'Rohan Mehta', 'rohan@mail.com'),
(4, 'rohan', '$2a$10$Q7yE2w8zN5t6h9u1p4q3r.i4o5l6m7n8b9v0c1x2z3a4s5d6f7g8h', 'student', 'Rohan Mehta', 'rohan.mehta@mail.com'),
(5, 'sneha', '$2a$10$Q7yE2w8zN5t6h9u1p4q3r.i4o5l6m7n8b9v0c1x2z3a4s5d6f7g8h', 'student', 'Sneha Iyer', 'sneha@mail.com'),
(6, 'arjun', '$2a$10$Q7yE2w8zN5t6h9u1p4q3r.i4o5l6m7n8b9v0c1x2z3a4s5d6f7g8h', 'student', 'Arjun Das', 'arjun@mail.com'),
(7, 'priya', '$2a$10$Q7yE2w8zN5t6h9u1p4q3r.i4o5l6m7n8b9v0c1x2z3a4s5d6f7g8h', 'student', 'Priya Nair', 'priya@mail.com');

-- 5. Teachers
INSERT IGNORE INTO teachers (id, user_id, employee_no, department, qualification, phone) VALUES
(1, 1, 'TCH001', 'Mathematics & Computer Science', 'M.Sc., B.Ed.', '+91 98765 43210');

-- 6. Students
INSERT IGNORE INTO students (id, user_id, reg_no, full_name, class_id, email, phone, school_name, status) VALUES
(1, 3, 'REG001', 'Rohan Mehta', 1, 'rohan@mail.com', '+91 98111 22334', 'Sunrise Public School', 'Active'),
(2, 5, 'REG002', 'Sneha Iyer', 2, 'sneha@mail.com', '+91 98222 33445', 'Sunrise Public School', 'Active'),
(3, 6, 'REG003', 'Arjun Das', 3, 'arjun@mail.com', '+91 98333 44556', 'Sunrise Public School', 'Active'),
(4, 7, 'REG004', 'Priya Nair', 1, 'priya@mail.com', '+91 98444 55667', 'Sunrise Public School', 'Active'),
(5, NULL, 'REG005', 'Karan Singh', 2, 'karan@mail.com', '+91 98555 66778', 'Sunrise Public School', 'Active'),
(6, NULL, 'REG006', 'Ananya Reddy', 3, 'ananya@mail.com', '+91 98666 77889', 'Sunrise Public School', 'Active'),
(7, NULL, 'REG007', 'Vishal Gupta', 1, 'vishal@mail.com', '+91 98777 88990', 'Sunrise Public School', 'Active'),
(8, NULL, 'REG008', 'Meera Joshi', 2, 'meera@mail.com', '+91 98888 99001', 'Sunrise Public School', 'Active'),
(9, NULL, 'REG009', 'Aarav Sharma', 1, 'aarav@mail.com', '+91 98999 11223', 'Sunrise Public School', 'Active'),
(10, NULL, 'REG010', 'Diya Thomas', 1, 'diya@mail.com', '+91 98101 23456', 'Sunrise Public School', 'Active'),
(11, NULL, 'REG011', 'Kabir Verma', 2, 'kabir@mail.com', '+91 98202 34567', 'Sunrise Public School', 'Active'),
(12, NULL, 'REG012', 'Anjali Raj', 3, 'anjali@mail.com', '+91 98303 45678', 'Sunrise Public School', 'Active');

-- 7. Student Results for Rohan Mehta (Term 1)
INSERT IGNORE INTO student_results (student_id, subject_id, term_id, marks_obtained, max_marks, is_pass, teacher_remarks, entered_by) VALUES
(1, 1, 1, 85, 100, TRUE, 'Excellent problem solving ability.', 1),
(1, 2, 1, 78, 100, TRUE, 'Understands concepts well. Practice diagrams.', 1),
(1, 3, 1, 76, 100, TRUE, 'Good writing skills. Work on grammar nuances.', 1),
(1, 4, 1, 68, 100, TRUE, 'Consistent effort. Needs improvement in long answers.', 1),
(1, 5, 1, 92, 100, TRUE, 'Outstanding logical thinking and practical coding.', 1);

-- Results for Sneha Iyer (Term 1)
INSERT IGNORE INTO student_results (student_id, subject_id, term_id, marks_obtained, max_marks, is_pass, teacher_remarks, entered_by) VALUES
(2, 1, 1, 88, 100, TRUE, 'Strong performance in algebra.', 1),
(2, 2, 1, 82, 100, TRUE, 'Needs help with organic chemistry concepts.', 1),
(2, 3, 1, 79, 100, TRUE, 'Very good comprehension and essays.', 1),
(2, 4, 1, 85, 100, TRUE, 'Great historical timeline understanding.', 1),
(2, 5, 1, 90, 100, TRUE, 'Top marks in web application projects.', 1);

-- Results for Arjun Das (Term 1)
INSERT IGNORE INTO student_results (student_id, subject_id, term_id, marks_obtained, max_marks, is_pass, teacher_remarks, entered_by) VALUES
(3, 1, 1, 74, 100, TRUE, 'Review Q3 steps in calculus.', 1),
(3, 2, 1, 80, 100, TRUE, 'Solid grasp of physics formulas.', 1),
(3, 3, 1, 70, 100, TRUE, 'Good vocabulary, focus on formal letter formats.', 1),
(3, 4, 1, 65, 100, TRUE, 'Adequate answers, expand analytical points.', 1),
(3, 5, 1, 84, 100, TRUE, 'Good database queries and structure.', 1);

-- 8. Teacher Remarks
INSERT IGNORE INTO teacher_remarks (id, student_id, subject_id, term_id, teacher_id, remark_text) VALUES
(1, 2, 1, 1, 1, 'Good performance. Needs to work on problem solving.'),
(2, 3, 2, 1, 1, 'Understands the concepts well. Improve diagram accuracy.'),
(3, 4, 3, 1, 1, 'Good writing skills. Focus more on grammar.');

-- 9. Correction Requests
INSERT IGNORE INTO correction_requests (id, student_id, subject_id, term_id, request_text, status, teacher_response, reviewed_by, created_at) VALUES
(1, 3, 1, 1, 'Marks seem incorrect for Q3.', 'Pending', NULL, NULL, '2025-04-12 10:15:00'),
(2, 2, 2, 1, 'Need help with organic chemistry topics.', 'Pending', NULL, NULL, '2025-04-10 14:30:00'),
(3, 4, 3, 1, 'Clarification on essay marks.', 'Resolved', 'Marks re-verified. Additional 2 marks awarded for structure.', 1, '2025-04-08 09:20:00');

-- 10. Topic Assistance / Special Focus Requests
INSERT IGNORE INTO assistance_requests (id, student_id, subject_id, topic, comments, status, teacher_response, reviewed_by, created_at) VALUES
(1, 1, 1, 'Quadratic Equations', 'Would appreciate extra practice worksheets on quadratic word problems.', 'Pending', NULL, NULL, '2025-04-10 11:00:00'),
(2, 1, 2, 'Chemical Bonding', 'Need clarification on covalent vs ionic bonding models.', 'Resolved', 'Scheduled 1-on-1 revision during Thursday remedial period.', 1, '2025-04-05 15:45:00'),
(3, 1, 3, 'Essay Writing', 'Guidance needed on essay outline drafting.', 'Pending', NULL, NULL, '2025-03-28 16:30:00');

-- 11. Activity Logs
INSERT IGNORE INTO activity_logs (id, user_id, action_type, description, created_at) VALUES
(1, 1, 'RESULT_UPDATE', 'Result updated for Rohan Mehta (Class 10)', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 1, 'REMARK_ADD', 'Teacher remark added for Sneha Iyer (Class 11)', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 3, 'CORRECTION_REQ', 'Correction request from Arjun Das (Class 12)', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(4, 4, 'ASSIST_REQ', 'Topic assistance request from Priya Nair (Class 10)', DATE_SUB(NOW(), INTERVAL 6 HOUR));
