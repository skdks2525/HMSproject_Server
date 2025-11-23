package server.service;

import server.model.Room;
import server.model.Reservation;
import server.repository.RoomRepository;
import server.repository.ReservationRepository;
import java.util.*;
import java.util.stream.Collectors;

public class HotelService {
    private final RoomRepository roomRepo = new RoomRepository();
    private final ReservationRepository resRepo = new ReservationRepository();
    private static final Object LOCK = new Object();

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    public Room getRoom(String roomNum) {
        return roomRepo.findByNumber(roomNum);
    }

    public List<Reservation> getReservationsByName(String name) {
        return resRepo.findAll().stream()
                .filter(r -> r.getGuestName().equals(name))
                .collect(Collectors.toList());
    }

// [기능 1] 예약 가능한 방 타입 목록 반환
    public String getAvailableRoomTypes(String reqIn, String reqOut) {
        synchronized (LOCK) {
            List<String> availableTypes = new ArrayList<>();
            List<Reservation> allRes = resRepo.findAll();

            // 각 타입의 대표 방 하나씩만 체크 (간소화 버전)
            // 실제로는 해당 타입의 모든 방을 검사해야 함
            if (isRoomAvailable("101", reqIn, reqOut, allRes)) availableTypes.add("STD");
            if (isRoomAvailable("201", reqIn, reqOut, allRes)) availableTypes.add("DLX");
            if (isRoomAvailable("301", reqIn, reqOut, allRes)) availableTypes.add("STE");

            return String.join(",", availableTypes);
        }
    }

    // [기능 2] 타입으로 자동 배정하여 예약
    public String createReservationByType(String type, String name, String reqIn, String reqOut, int guestNum, String phone) {
        synchronized (LOCK) {
            List<Room> targetRooms = new ArrayList<>();
            for (Room r : roomRepo.findAll()) {
                // CSV의 "Standard"와 요청 "STD" 매칭
                if (convertCodeToType(type).equalsIgnoreCase(r.getType())) {
                    targetRooms.add(r);
                }
            }
            if (targetRooms.isEmpty()) return null;

            List<Reservation> allRes = resRepo.findAll();
            String availableRoomNum = null;

            // 빈 방 찾기
            for (Room room : targetRooms) {
                if (isRoomAvailable(room.getRoomNumber(), reqIn, reqOut, allRes)) {
                    availableRoomNum = room.getRoomNumber();
                    break;
                }
            }

            // 예약 저장
            if (availableRoomNum != null) {
                String resId = resRepo.add(availableRoomNum, name, reqIn, reqOut, guestNum, phone);
                return (resId != null) ? availableRoomNum : null;
            }
            return null;
        }
    }
    
    public boolean cancelReservation(String resId) {
        synchronized (LOCK) { return resRepo.delete(resId); }
    }

    // --- 헬퍼 메서드 ---
    private boolean isRoomAvailable(String roomNum, String reqIn, String reqOut, List<Reservation> allRes) {
        for (Reservation res : allRes) {
            if (res.getRoomNumber().equals(roomNum)) {
                // 겹치면 false (예약 불가)
                if (isDateOverlapping(reqIn, reqOut, res.getCheckInDate(), res.getCheckOutDate())) {
                    return false; 
                }
            }
        }
        return true;
    }

    private boolean isDateOverlapping(String reqIn, String reqOut, String existIn, String existOut) {
        // (요청 시작 < 기존 종료) AND (요청 종료 > 기존 시작)
        return (reqIn.compareTo(existOut) < 0) && (reqOut.compareTo(existIn) > 0);
    }

    private String convertCodeToType(String code) {
        if ("STD".equals(code)) return "Standard";
        if ("DLX".equals(code)) return "Deluxe";
        if ("STE".equals(code)) return "Suite";
        return code;
    }
}