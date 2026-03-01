package com.kripal.hostel.model;

public class Hostel {
    private int    hostelId;
    private String hostelName;
    private int    totalRooms;
    private int    wardenId;

    public Hostel() {}

    public Hostel(int hostelId, String hostelName, int totalRooms, int wardenId) {
        this.hostelId   = hostelId;
        this.hostelName = hostelName;
        this.totalRooms = totalRooms;
        this.wardenId   = wardenId;
    }

    public int    getHostelId()   { return hostelId; }
    public String getHostelName() { return hostelName; }
    public int    getTotalRooms() { return totalRooms; }
    public int    getWardenId()   { return wardenId; }

    public void setHostelId  (int hostelId)      { this.hostelId   = hostelId; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }
    public void setTotalRooms(int totalRooms)    { this.totalRooms = totalRooms; }
    public void setWardenId  (int wardenId)      { this.wardenId   = wardenId; }

    @Override
    public String toString() {
        return String.format("Hostel[id=%d, name=%s, rooms=%d, wardenId=%d]",
                hostelId, hostelName, totalRooms, wardenId);
    }
}
