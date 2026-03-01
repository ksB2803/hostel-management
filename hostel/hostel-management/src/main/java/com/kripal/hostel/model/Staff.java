package com.kripal.hostel.model;

public class Staff {
    private int    staffId;
    private String name;
    private String role;       // WARDEN / ATTENDANT / GUARD
    private int    hostelId;
    private String shift;
    private String phone;

    public Staff() {}

    public Staff(int staffId, String name, String role,
                 int hostelId, String shift, String phone) {
        this.staffId  = staffId;
        this.name     = name;
        this.role     = role;
        this.hostelId = hostelId;
        this.shift    = shift;
        this.phone    = phone;
    }

    public int    getStaffId()  { return staffId; }
    public String getName()     { return name; }
    public String getRole()     { return role; }
    public int    getHostelId() { return hostelId; }
    public String getShift()    { return shift; }
    public String getPhone()    { return phone; }

    public void setStaffId (int staffId)    { this.staffId  = staffId; }
    public void setName    (String name)    { this.name     = name; }
    public void setRole    (String role)    { this.role     = role; }
    public void setHostelId(int hostelId)   { this.hostelId = hostelId; }
    public void setShift   (String shift)   { this.shift    = shift; }
    public void setPhone   (String phone)   { this.phone    = phone; }

    @Override
    public String toString() {
        return String.format("Staff[id=%d, name=%s, role=%s, hostel=%d, shift=%s, phone=%s]",
                staffId, name, role, hostelId, shift, phone);
    }
}
