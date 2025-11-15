/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import model.Room;
import model.Reservation;

import java.util.List;

/**
 * 관리자 패널의 출력(View) 역할을 담당하는 클래스
 */
public class RoomManagementView {

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayError(String errorMessage) {
        System.err.println("오류: " + errorMessage);
    }

    public void displayRoomList(List<Room> rooms, String title) {
        displayMessage("=============================================");
        displayMessage(title);
        displayMessage("=============================================");
        if (rooms.isEmpty()) {
            displayMessage("조회된 객실이 없습니다.");
            return;
        }
        rooms.forEach(room -> displayMessage(room.toString()));
        displayMessage("---------------------------------------------");
        displayMessage("총 " + rooms.size() + "개의 객실이 조회되었습니다.");
    }

    public void displayReservationList(List<Reservation> reservations, String title) {
        displayMessage("=============================================");
        displayMessage(title);
        displayMessage("=============================================");
        if (reservations.isEmpty()) {
            displayMessage("조회된 예약이 없습니다.");
            return;
        }
        reservations.forEach(reservation -> displayMessage(reservation.toString()));
        displayMessage("---------------------------------------------");
        displayMessage("총 " + reservations.size() + "개의 예약이 조회되었습니다.");
    }

    public void displayReservationDetail(Reservation reservation) {
        displayMessage("===================== 예약 상세 정보 =====================");
        displayMessage("ID: " + reservation.getReservationId());
        displayMessage("객실 번호: " + reservation.getRoomNumber());
        displayMessage("투숙객 이름: " + reservation.getGuestName());
        displayMessage("체크인 날짜: " + reservation.getCheckInDate());
        displayMessage("체크아웃 날짜: " + reservation.getCheckOutDate());
        displayMessage("예약 상태: " + reservation.getStatus());
        displayMessage("=========================================================");
    }
}
