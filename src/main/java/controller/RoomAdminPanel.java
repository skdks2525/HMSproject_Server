/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import service.HotelService;
import model.Room;
import model.Reservation;
import model.Reservation.ReservationStatus;
import view.RoomManagementView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 객실 관리자 패널의 역할을 수행하는 컨트롤러 클래스
 * 사용자(관리자)의 요청을 받아 HotelService를 호출하고 View를 통해 결과를 표시
 */
public class RoomAdminPanel {
    private final HotelService hotelService;
    private final RoomManagementView view;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public RoomAdminPanel(HotelService hotelService, RoomManagementView view) {
        this.hotelService = hotelService;
        this.view = view;
    }

    public void startPanel() {
        view.displayMessage("=== 호텔 객실 및 예약 관리자 패널 시작 ===");
    }

    // --- SFR-201: 전체 객실 정보 조회 처리 ---
    public void handleGetAllRooms() {
        try {
            List<Room> rooms = hotelService.getAllRooms();
            view.displayRoomList(rooms, "전체 객실 목록");
        } catch (Exception e) {
            view.displayError("객실 정보 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // --- SFR-202: 객실 번호로 객실 정보 조회 처리 ---
    public void handleGetRoomByNumber(String roomNumber) {
        try {
            Optional<Room> roomOpt = hotelService.getRoomByNumber(roomNumber);
            if (roomOpt.isPresent()) {
                view.displayMessage("[SFR-202] 조회된 객실 정보: " + roomOpt.get());
            } else {
                view.displayMessage("[SFR-202] 객실 번호 " + roomNumber + "를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            view.displayError("객실 정보 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // --- SFR-204: 투숙객 이름으로 예약 목록 조회 처리 ---
    public void handleGetReservationsByGuestName(String guestName) {
        try {
            List<Reservation> reservations = hotelService.getReservationsByGuestName(guestName);
            if (reservations.isEmpty()) {
                view.displayMessage("[SFR-204] 고객명 '" + guestName + "'의 예약이 없습니다.");
            } else {
                view.displayReservationList(reservations, "고객명 '" + guestName + "'의 예약 목록");
            }
        } catch (Exception e) {
            view.displayError("예약 정보 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // --- SFR-205: 특정 기간 예약 가능 객실 조회 처리 ---
    public void handleGetAvailableRooms(String checkInStr, String checkOutStr) {
        try {
            LocalDate checkInDate = LocalDate.parse(checkInStr, DATE_FORMATTER);
            LocalDate checkOutDate = LocalDate.parse(checkOutStr, DATE_FORMATTER);

            List<Room> availableRooms = hotelService.getAvailableRooms(checkInDate, checkOutDate);
            view.displayRoomList(availableRooms,
                    String.format("[SFR-205] 기간 (%s ~ %s)에 예약 가능한 객실 목록", checkInStr, checkOutStr));
        } catch (Exception e) {
            view.displayError("예약 가능 객실 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // --- SFR-401: 예약 생성 처리 ---
    public void handleCreateReservation(String roomNumber, String guestName, String checkInStr, String checkOutStr) {
        try {
            LocalDate checkInDate = LocalDate.parse(checkInStr, DATE_FORMATTER);
            LocalDate checkOutDate = LocalDate.parse(checkOutStr, DATE_FORMATTER);

            Reservation newReservation = hotelService.createReservation(roomNumber, guestName, checkInDate, checkOutDate);
            view.displayMessage("--- [SFR-401] 예약 생성 성공 ---");
            view.displayReservationDetail(newReservation);
        } catch (Exception e) {
            view.displayError("예약 생성 실패: " + e.getMessage());
        }
    }

    // --- SFR-402: 예약 수정 처리 (고객명, 날짜만 수정 가능하다고 가정) ---
    public void handleUpdateReservation(String reservationId, String newGuestName, String newCheckInStr, String newCheckOutStr) {
        try {
            LocalDate newCheckInDate = LocalDate.parse(newCheckInStr, DATE_FORMATTER);
            LocalDate newCheckOutDate = LocalDate.parse(newCheckOutStr, DATE_FORMATTER);

            Reservation updatedReservation = hotelService.updateReservation(
                    reservationId, newGuestName, newCheckInDate, newCheckOutDate);

            view.displayMessage("--- [SFR-402] 예약 수정 성공 ---");
            view.displayReservationDetail(updatedReservation);
        } catch (Exception e) {
            view.displayError("예약 수정 실패: " + e.getMessage());
        }
    }

    // --- SFR-403: 예약 취소 처리 ---
    public void handleCancelReservation(String reservationId) {
        try {
            Reservation cancelledReservation = hotelService.cancelReservation(reservationId);
            view.displayMessage("--- [SFR-403] 예약 취소 성공 ---");
            view.displayReservationDetail(cancelledReservation);
        } catch (Exception e) {
            view.displayError("예약 취소 실패: " + e.getMessage());
        }
    }

    // --- SFR-404: 예약 상태 변경 처리 (체크인/체크아웃) ---
    public void handleChangeStatus(String reservationId, String statusStr) {
        try {
            ReservationStatus newStatus = ReservationStatus.valueOf(statusStr.toUpperCase());
            Reservation updatedReservation = hotelService.updateReservationStatus(reservationId, newStatus);
            view.displayMessage("--- [SFR-404] 예약 상태 변경 성공 ---");
            view.displayReservationDetail(updatedReservation);
        } catch (IllegalArgumentException e) {
            view.displayError("상태 변경 실패: 유효하지 않은 상태 값 또는 예약 ID입니다. (유효 상태: CHECKED_IN, CHECKED_OUT)");
        } catch (Exception e) {
            view.displayError("예약 상태 변경 실패: " + e.getMessage());
        }
    }
}
