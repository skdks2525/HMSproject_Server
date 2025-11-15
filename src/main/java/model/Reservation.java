/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 예약 정보를 나타내는 모델 클래스
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String reservationId; // 예약 ID (고유 식별자)
    private final String roomNumber;    // 예약된 객실 번호
    private final String guestName;     // 투숙객 이름
    private final LocalDate checkInDate;  // 체크인 날짜
    private final LocalDate checkOutDate; // 체크아웃 날짜
    private ReservationStatus status; // 예약 상태 (예: CONFIRMED, CANCELLED)

    public enum ReservationStatus {
        CONFIRMED, CANCELLED, CHECKED_IN, CHECKED_OUT
    }

    public Reservation(String reservationId, String roomNumber, String guestName,
                       LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.CONFIRMED; // 기본 상태는 확정(CONFIRMED)
    }

    // --- Getter and Setter ---

    public String getReservationId() {
        return reservationId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    // --- Utility Methods ---

    /**
     * 해당 기간에 객실이 예약되어 있는지 확인
     */
    public boolean overlapsWith(LocalDate startDate, LocalDate endDate) {
        // 예약 기간: [checkInDate, checkOutDate - 1일] (체크아웃 날은 비어있음)
        // 요청 기간: [startDate, endDate - 1일]
        return !checkOutDate.isBefore(startDate) && !checkInDate.isAfter(endDate.minusDays(1));
    }

    // --- toString for easy logging and display ---

    @Override
    public String toString() {
        return String.format("[예약 ID: %s] 객실: %s, 고객: %s, 기간: %s ~ %s, 상태: %s",
                reservationId, roomNumber, guestName, checkInDate, checkOutDate, status);
    }
}
