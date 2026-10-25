package com.srms.model;

import java.time.LocalDateTime;

public class Student {
    private int id;
    private Integer userId;
    public String reg;
    public String name;
    public String cls;
    private int classId;
    public String email;
    private String phone;
    public String school = "Sunrise Public School";
    public String status = "Active";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Student() {}

    public Student(String reg, String name, String cls, String email, String school) {
        this.reg = reg;
        this.name = name;
        this.cls = cls;
        this.email = email;
        this.school = school;
        this.status = "Active";
    }

    public Student(int id, Integer userId, String reg, String name, int classId, String cls, String email, String phone, String school, String status) {
        this.id = id;
        this.userId = userId;
        this.reg = reg;
        this.name = name;
        this.classId = classId;
        this.cls = cls;
        this.email = email;
        this.phone = phone;
        this.school = school != null ? school : "Sunrise Public School";
        this.status = status != null ? status : "Active";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getReg() { return reg; }
    public void setReg(String reg) { this.reg = reg; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCls() { return cls; }
    public void setCls(String cls) { this.cls = cls; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + " (" + reg + ") - " + cls;
    }
}
