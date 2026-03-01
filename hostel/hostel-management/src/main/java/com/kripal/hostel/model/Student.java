package com.kripal.hostel.model;

import java.math.BigDecimal;

public class Student {
    private String     rollNo;      // PRIMARY KEY
    private String     name;
    private String     branch;
    private String     year;
    private BigDecimal cgpa;
    private BigDecimal attendance;
    private String     email;
    private String     gender;      // M / F
    private String     status;      // ACTIVE / LEFT / DAY_SCHOLAR

    public Student() {}

    public Student(String rollNo, String name, String branch, String year,
                   BigDecimal cgpa, BigDecimal attendance,
                   String email, String gender, String status) {
        this.rollNo     = rollNo;
        this.name       = name;
        this.branch     = branch;
        this.year       = year;
        this.cgpa       = cgpa;
        this.attendance = attendance;
        this.email      = email;
        this.gender     = gender;
        this.status     = status;
    }

    public String     getRollNo()     { return rollNo; }
    public String     getName()       { return name; }
    public String     getBranch()     { return branch; }
    public String     getYear()       { return year; }
    public BigDecimal getCgpa()       { return cgpa; }
    public BigDecimal getAttendance() { return attendance; }
    public String     getEmail()      { return email; }
    public String     getGender()     { return gender; }
    public String     getStatus()     { return status; }

    public void setRollNo    (String rollNo)          { this.rollNo     = rollNo; }
    public void setName      (String name)            { this.name       = name; }
    public void setBranch    (String branch)          { this.branch     = branch; }
    public void setYear      (String year)            { this.year       = year; }
    public void setCgpa      (BigDecimal cgpa)        { this.cgpa       = cgpa; }
    public void setAttendance(BigDecimal attendance)  { this.attendance = attendance; }
    public void setEmail     (String email)           { this.email      = email; }
    public void setGender    (String gender)          { this.gender     = gender; }
    public void setStatus    (String status)          { this.status     = status; }

    public boolean isGirl() { return "F".equalsIgnoreCase(gender); }

    @Override
    public String toString() {
        return String.format("Student[roll=%s, name=%s, branch=%s, year=%s, cgpa=%s, att=%s, gender=%s, status=%s]",
                rollNo, name, branch, year, cgpa, attendance, gender, status);
    }
}
