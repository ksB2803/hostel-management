package com.kripal.hostel.model;

public class Room {
    private int    roomId;
    private int    hostelId;
    private String roomNumber;
    private String roomType;   // SINGLE / DOUBLE / TRIPLE
    private int    capacity;
    private int    occupied;

    public Room() {}

    public Room(int roomId, int hostelId, String roomNumber, String roomType,
                int capacity, int occupied) {
        this.roomId     = roomId;
        this.hostelId   = hostelId;
        this.roomNumber = roomNumber;
        this.roomType   = roomType;
        this.capacity   = capacity;
        this.occupied   = occupied;
    }

    public int    getRoomId()     { return roomId; }
    public int    getHostelId()   { return hostelId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType()   { return roomType; }
    public int    getCapacity()   { return capacity; }
    public int    getOccupied()   { return occupied; }

    public void setRoomId    (int roomId)       { this.roomId     = roomId; }
    public void setHostelId  (int hostelId)     { this.hostelId   = hostelId; }
    public void setRoomNumber(String roomNumber){ this.roomNumber = roomNumber; }
    public void setRoomType  (String roomType)  { this.roomType   = roomType; }
    public void setCapacity  (int capacity)     { this.capacity   = capacity; }
    public void setOccupied  (int occupied)     { this.occupied   = occupied; }

    public boolean hasSpace() { return occupied < capacity; }

    @Override
    public String toString() {
        return String.format("Room[id=%d, hostel=%d, no=%s, type=%s, cap=%d, occ=%d]",
                roomId, hostelId, roomNumber, roomType, capacity, occupied);
    }
}
