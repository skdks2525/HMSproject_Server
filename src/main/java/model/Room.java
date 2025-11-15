/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 객실 정보를 나타내는 모델 클래스
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String roomNumber; // 객실 번호 (예: "101", "205")
    private final String roomType;   // 객실 유형 (예: "Single", "Double", "Suite")
    private final int capacity;      // 최대 수용 인원
    private final double pricePerNight; // 1박당 가격

    public Room(String roomNumber, String roomType, int capacity, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    // --- Getter and Setter ---

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    // --- Equals and HashCode (roomNumber 기준으로 비교) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(roomNumber, room.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }

    // --- toString for easy logging and display ---

    @Override
    public String toString() {
        return String.format("[객실 %s] 유형: %s, 인원: %d명, 가격: %.0f원",
                roomNumber, roomType, capacity, pricePerNight);
    }
}
