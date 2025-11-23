package server.service;

import server.model.*;
import server.repository.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class HotelService {
    private final RoomRepository roomRepo = new RoomRepository();
    private final ReservationRepository resRepo = new ReservationRepository();
    private static final Object LOCK = new Object();

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public HotelService(){
        startAutoCancelScheduler();
    }   
    
    private void startAutoCancelScheduler() {
                
        checkAndCancelUnpaidReservations();
        scheduleDailyTask();
    }

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

    // 예약 생성 (현재 시간)
    public String createReservationByType(String type, String name, String reqIn, String reqOut, int guestNum, String phone) {
        synchronized (LOCK) {
            List<Room> targetRooms = new ArrayList<>();
            for (Room r : roomRepo.findAll()) {
                if (convertCodeToType(type).equalsIgnoreCase(r.getType())) {
                    targetRooms.add(r);
                }
            }
            if (targetRooms.isEmpty()) return null;

            List<Reservation> allRes = resRepo.findAll();
            String availableRoomNum = null;

            for (Room room : targetRooms) {
                if (isRoomAvailable(room.getRoomNumber(), reqIn, reqOut, allRes)) {
                    availableRoomNum = room.getRoomNumber();
                    break;
                }
            }

            if (availableRoomNum != null) {
                // 현재 시간을 생성일로 저장
                String nowStr = LocalDateTime.now().format(formatter);
                String resId = resRepo.add(availableRoomNum, name, reqIn, reqOut, guestNum, phone, nowStr);
                return (resId != null) ? availableRoomNum : null;
            }
            return null;
        }
    }
    
    public boolean cancelReservation(String resId) {
        synchronized (LOCK) { return resRepo.delete(resId); }
    }

    public boolean processPayment(String resId, String paymentInfo) {
        synchronized (LOCK) {
            return resRepo.updatePayment(resId, paymentInfo);
        }
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
    
    private void scheduleDailyTask(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0, 0));
        
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1);
        }
        
        // 목표 시간까지 남은 초(Seconds) 계산
        long initialDelay = Duration.between(now, targetTime).getSeconds();
        long oneDayInSeconds = 24 * 60 * 60; // 24시간 주기
        
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (LOCK) { 
                System.out.println("18:00 미보장 예약 자동취소");
                checkAndCancelUnpaidReservations();
            }
        }, initialDelay, oneDayInSeconds, TimeUnit.SECONDS);
    }
    
    private void checkAndCancelUnpaidReservations() {
        List<Reservation> all = resRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Reservation r : all) {
            // 이미 결제했으면 패스
            if (!"Unpaid".equals(r.getPaymentInfo())) continue;

            try {
                // 예약 생성 시간 파싱
                LocalDateTime createdAt = LocalDateTime.parse(r.getCreatedAt(), formatter);
                LocalDateTime deadline;

                // [조건] 17시 이전 예약 -> 당일 18시 마감
                // [조건] 17시 이후 예약 -> 다음날 18시 마감
                if (createdAt.getHour() < 17) {
                    deadline = createdAt.toLocalDate().atTime(18, 0, 0);
                } else {
                    deadline = createdAt.plusDays(1).toLocalDate().atTime(18, 0, 0);
                }

                // 현재 시간이 마감 시간을 지났으면 삭제
                if (now.isAfter(deadline)) {
                    System.out.println("[자동취소] 기한 만료! ID: " + r.getReservationId() + 
                            " (생성: " + r.getCreatedAt() + " / 마감: " + deadline + ")");
                    resRepo.delete(r.getReservationId());
                }

            } catch (Exception e) {
                System.out.println("날짜 파싱 오류 (ID: " + r.getReservationId() + "): " + e.getMessage());
            }
        }
    }        
}