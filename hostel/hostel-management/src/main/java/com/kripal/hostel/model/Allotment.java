package com.kripal.hostel.model;

import java.time.LocalDate;

public class Allotment {
    private int       allotmentId;
    private String    rollNo;        // FK → STUDENT.roll_no
    private int       roomId;
    private LocalDate allotmentDate;
    private LocalDate checkoutDate;
    private String    status;

    public Allotment() {}

    public Allotment(int allotmentId, String rollNo, int roomId,
                     LocalDate allotmentDate, LocalDate checkoutDate, String status) {
        this.allotmentId   = allotmentId;
        this.rollNo        = rollNo;
        this.roomId        = roomId;
        this.allotmentDate = allotmentDate;
        this.checkoutDate  = checkoutDate;
        this.status        = status;
    }

    public int       getAllotmentId()   { return allotmentId; }
    public String    getRollNo()        { return rollNo; }
    public int       getRoomId()        { return roomId; }
    public LocalDate getAllotmentDate() { return allotmentDate; }
    public LocalDate getCheckoutDate()  { return checkoutDate; }
    public String    getStatus()        { return status; }

    public void setAllotmentId  (int allotmentId)        { this.allotmentId   = allotmentId; }
    public void setRollNo       (String rollNo)          { this.rollNo        = rollNo; }
    public void setRoomId       (int roomId)             { this.roomId        = roomId; }
    public void setAllotmentDate(LocalDate d)            { this.allotmentDate = d; }
    public void setCheckoutDate (LocalDate d)            { this.checkoutDate  = d; }
    public void setStatus       (String status)          { this.status        = status; }

    @Override
    public String toString() {
        return String.format("Allotment[id=%d, roll=%s, room=%d, date=%s, status=%s]",
                allotmentId, rollNo, roomId, allotmentDate, status);
    }
}
