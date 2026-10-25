package com.srms.model;

public class Teacher {
    private int id;
    private int userId;
    private String employeeNo;
    private String fullName;
    private String department;
    private String qualification;
    private String phone;

    public Teacher() {}

    public Teacher(int id, int userId, String employeeNo, String fullName, String department, String qualification, String phone) {
        this.id = id;
        this.userId = userId;
        this.employeeNo = employeeNo;
        this.fullName = fullName;
        this.department = department;
        this.qualification = qualification;
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return fullName + " (" + employeeNo + ")";
    }
}
