package server.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import server.model.Payment;
import server.model.Reservation;
import server.model.Room;
import server.repository.PaymentRepository;
import server.repository.ReservationRepository;
import server.repository.RoomRepository;

public class HotelService {

    private final RoomRepository roomRepo;
    private final ReservationRepository resRepo;
    private final PaymentRepository payRepo;    
    private final Set<String> cleaningRooms = Collections.synchronizedSet(new HashSet<>());
    private static final Object LOCK = new Object();
    private static final int EXTRA_PERSON_FEE = 20000;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public HotelService(){
        this.roomRepo = new RoomRepository();
        this.resRepo = new ReservationRepository();
        this.payRepo = new PaymentRepository();
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

    /**
     * 지정한 날짜 범위의 일별 객실 매출 합계를 반환
     * 결제 정보가 "Paid"인 예약만 포함
     * @param startDate yyyy-MM-dd
     * @param endDate yyyy-MM-dd
     * @return Map<LocalDate,Integer> (LinkedHashMap 날짜 순서 유지)
     */
    public Map<LocalDate, Integer> getRoomSalesByDateRange(String startDate, String endDate) {
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 입력으로 받은 문자열 날짜(startDate, endDate)를 LocalDate로 변환합니다.
        // 여기서는 yyyy-MM-dd 포맷을 가정합니다. 파싱 실패 시 DateTimeParseException이 발생합니다.
        LocalDate start = LocalDate.parse(startDate, dateOnlyFormatter);
        LocalDate end = LocalDate.parse(endDate, dateOnlyFormatter);

        // 날짜(키) -> 매출(값) 매핑을 만들되, 날짜 순서를 유지하기 위해 LinkedHashMap을 사용합니다.
        // 먼저 요청된 범위의 모든 날짜를 0으로 초기화하면 이후 누적 합산이 쉬워집니다.
        Map<LocalDate, Integer> salesMap = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) salesMap.put(d, 0);

        // 저장소에서 모든 예약을 읽어 옵니다. (CSV 기반 Repository의 현재 스냅샷)
        List<Reservation> all = resRepo.findAll();

        for (Reservation r : all) {
            try {
                // ReservationStatus가 'Confirmed'인 예약만 포함
                if (r.getReservationStatus() == null || !r.getReservationStatus().equalsIgnoreCase("Confirmed")) continue;
                LocalDate in = LocalDate.parse(r.getCheckInDate(), dateOnlyFormatter);
                LocalDate out = LocalDate.parse(r.getCheckOutDate(), dateOnlyFormatter);

                // 해당 예약이 사용한 방의 가격을 조회합니다. RoomRepository에서 방 번호로 찾습니다.
                // 방 정보가 없으면 가격을 0으로 처리(안정성 확보).
                Room room = roomRepo.findByNumber(r.getRoomNumber());
                int price = (room != null) ? room.getPrice() : 0;

                // 예약은 [checkIn, checkOut) 구간의 각 하룻밤에 대해 매출을 발생시킵니다.
                // 따라서 checkIn부터 (checkOut - 1)일까지 반복하며 해당 날짜가 요청 범위에 포함되면 해당 날짜의 합계에 더합니다.
                for (LocalDate d = in; d.isBefore(out); d = d.plusDays(1)) {
                    // salesMap에 날짜 키가 존재하고, 요청한 범위(start..end)에 속한 날짜만 누적합니다.
                    if (!d.isBefore(start) && !d.isAfter(end)) {
                        salesMap.put(d, salesMap.getOrDefault(d, 0) + price);
                    }
                }
            } catch (Exception ex) {
                // 파싱(날짜 형식) 오류 등 예외가 발생하면 해당 예약은 집계에서 건너뛰고 계속 진행합니다.
                // 필요시 로깅을 추가하여 데이터 문제를 추적하는 것이 좋습니다.
            }
        }
        return salesMap;
    }

    // 예약 가능한 방 타입 목록 반환
    public String getAvailableRoomTypes(String reqIn, String reqOut) {
        synchronized (LOCK) {
            List<String> availableTypes = new ArrayList<>();
            List<Reservation> allRes = resRepo.findAll();

            // 각 타입의 대표 방번호로 가능여부 체크 
            if (isRoomAvailable("101", reqIn, reqOut, allRes)) availableTypes.add("STD");
            if (isRoomAvailable("201", reqIn, reqOut, allRes)) availableTypes.add("DLX");
            if (isRoomAvailable("301", reqIn, reqOut, allRes)) availableTypes.add("STE");

            return String.join(",", availableTypes);
        }
    }
    
    public String getRoomStatusList(String reqIn, String reqOut) {
        StringBuilder sb = new StringBuilder("ROOM_STATUS_LIST:");
        List<Room> rooms = roomRepo.findAll();
        List<Reservation> allRes = resRepo.findAll();

        for (Room r : rooms) {
            boolean isBooked = false;
            String status = "AVAILABLE";

            // 예약 확인
            for (Reservation res : allRes) {
                if (res.getRoomNumber().equals(r.getRoomNumber())) {
                    if ("CheckedOut".equals(res.getReservationStatus())) continue;
                    if (isDateOverlapping(reqIn, reqOut, res.getCheckInDate(), res.getCheckOutDate())) {
                        isBooked = true;
                        status = "BOOKED";
                        break;
                    }
                }
            }

            // 예약이 안 잡혀있다면 청소 상태 확인
            if (!isBooked) {
                if (cleaningRooms.contains(r.getRoomNumber())) {
                    // 관리자 화면에서 노란색(Cleaning)으로 보여주기 위함
                    // isBooked=true로 하면 예약불가
                    status = "Cleaning"; 
                }
            }

            sb.append(String.format("%s,%s,%d,%d,%s,%s|", 
                    r.getRoomNumber(), r.getType(), r.getPrice(), r.getCapacity(), r.getDescription(), status));
        }
        return sb.toString();
    }

    // [수정] 예약 + 결제 통합 메서드
    public synchronized String createReservationWithPayment(
            String roomNum, String name, String reqIn, String reqOut, 
            int guestNum, String phone, String request,
            String cardNum, String cvc, String expiry, String cardPw) {
        
        // 1. 방 확인 & 가용성 확인 (기존 동일)
        Room room = roomRepo.findByNumber(roomNum);
        if (room == null) return "FAIL:InvalidRoom";
        if (!isRoomAvailable(roomNum, reqIn, reqOut, resRepo.findAll())) {
            return "FAIL:RoomNotAvailable";
        }

        // 2. 비용 계산 (기존 동일)
        LocalDate inDate = LocalDate.parse(reqIn);
        LocalDate outDate = LocalDate.parse(reqOut);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(inDate, outDate);
        if (nights < 1) nights = 1;

        int basePrice = room.getPrice();
        int extraCost = 0;
        if (guestNum > room.getCapacity()) {
            extraCost = (guestNum - room.getCapacity()) * EXTRA_PERSON_FEE;
        }
        int totalAmount = (int)((basePrice + extraCost) * nights);

        // 3. 예약 정보 저장
        String nowStr = LocalDateTime.now().format(formatter);
        String resId = resRepo.add(roomNum, name, reqIn, reqOut, guestNum, phone, nowStr, request);
        if (resId == null) return "FAIL:ReservationSaveError";

        // 4. [수정됨] Payment 저장 (금액 포함!)
        String payId = "P-" + System.currentTimeMillis();
        
        // Payment 생성자에 totalAmount 추가
        Payment payment = new Payment(payId, resId, "CreditCard", cardNum, cvc, expiry, cardPw, totalAmount, nowStr);
        
        boolean paySaved = payRepo.add(payment);

        // 5. 결과 확인
        if (paySaved) {
            resRepo.updateStatus(resId, "Confirmed"); // 결제 성공 시 확정
            return "SUCCESS:" + resId + ":" + totalAmount;
        } else {
            resRepo.delete(resId); // 실패 시 롤백
            return "FAIL:PaymentSaveError";
        }
    }
    
    public boolean cancelReservation(String resId) {
        synchronized (LOCK) { return resRepo.delete(resId); }
    }

    public synchronized boolean processPayment(String resId, String method, String cardNum, String cvc, String expiry, String pw, int amount) {
        String payId = "P-" + System.currentTimeMillis();
        String paymentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String useMethod = method == null ? "" : method;

        // If client requests to use stored card info, fetch existing payment record by reservation id
        if ("Stored".equalsIgnoreCase(useMethod) || "StoredCard".equalsIgnoreCase(useMethod) || "UseSaved".equalsIgnoreCase(useMethod)) {
            server.model.Payment saved = payRepo.findLatestByReservationId(resId);
            if (saved == null) {
                return false; // no saved card info
            }
            // reuse card fields from saved payment
            cardNum = saved.getCardNumber();
            cvc = saved.getCvc();
            expiry = saved.getExpiryDate();
            pw = saved.getPassword();
            // proceed to create new payment record with requested amount
        }

        // Payment(ID, ResID, Method, Card, CVC, Expiry, PW, Amount, Time)
        Payment newPayment = new Payment(payId, resId, method, cardNum, cvc, expiry, pw, amount, paymentTime);

        boolean paySaved = payRepo.add(newPayment);

        if (paySaved) {
            // 결제 정보 저장 성공 시 -> 예약 상태를 'Confirmed'로 변경
            return resRepo.updateStatus(resId, "Confirmed");
        }
        return false;
    }
    
    public boolean checkIn(String resId){
        synchronized (LOCK){
            return resRepo.updateStatus(resId, "CheckedIn");
        }
    }
    
    public boolean checkOut(String resId){
        synchronized(LOCK){
            return resRepo.updateStatus(resId, "CheckedOut");
        }
    }
    
public List<String> getReservationsWithRoomInfo(String guestName){
        List<Reservation> myReservations = resRepo.findAll().stream()
                .filter(r -> r.getGuestName().equals(guestName))
                .collect(Collectors.toList());

        List<Room> allRooms = roomRepo.findAll();
        List<String> resultList = new ArrayList<>();
        
        for(Reservation res : myReservations){
            // 기존 res.toString() (CSV 형태)
            String resStr = res.toString(); 
            String roomType = "Unknown";
            int roomPrice = 0;
            int roomCapacity = 2; // [추가] 기본값 설정

            for (Room r : allRooms){
                if(r.getRoomNumber().equals(res.getRoomNumber())){
                    roomType = r.getType();
                    roomPrice = r.getPrice();
                    roomCapacity = r.getCapacity(); // [추가] 방 정원 가져오기
                    break;
                }
            }
            // [핵심 수정] 데이터 끝에 ",타입,가격,정원" 순서로 붙여서 전송
            // 예: "...,Unpaid,타입,100000,2"
            resultList.add(resStr + "," + roomType + "," + roomPrice + "," + roomCapacity);
        } 
        return resultList;
    }
    
    private boolean isRoomAvailable(String roomNum, String reqIn, String reqOut, List<Reservation> allRes) {
        // 예약 겹침 확인
        for (Reservation res : allRes) {
            if (res.getRoomNumber().equals(roomNum)) {
                // 체크아웃 된 건은 무시 (예약 가능)
                if ("CheckedOut".equals(res.getReservationStatus())) continue;

                if (isDateOverlapping(reqIn, reqOut, res.getCheckInDate(), res.getCheckOutDate())) {
                    return false; // 겹침
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
        
        long initialDelay = Duration.between(now, targetTime).getSeconds();
        long oneDayInSeconds = 24 * 60 * 60; 
        
        // 중복 실행 방지를 위해 Runnable 작업 정의
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (LOCK) { 
                System.out.println("[System] 18:00 미보장 예약 자동취소 점검 시작");
                checkAndCancelUnpaidReservations();
            }
        }, initialDelay, oneDayInSeconds, TimeUnit.SECONDS);
    }
    
    public synchronized String toggleCleaningStatus(String roomNum) {
        if (cleaningRooms.contains(roomNum)) {
            cleaningRooms.remove(roomNum); // 있으면 끄고
        } else {
            cleaningRooms.add(roomNum);    // 없으면 킴
        }
        return "SUCCESS";
    }
    
    private void checkAndCancelUnpaidReservations() {
        List<Reservation> all = resRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Reservation r : all) {
            if (!"Unpaid".equals(r.getReservationStatus())) continue;

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
    
    // 예약 생성
    public String createReservationByRoomNum(String roomNum, String name, String reqIn, String reqOut, int guestNum, String phone, String request) {
        synchronized (LOCK) {
            // 1. 해당 방 번호가 실존하는지 확인
            Room room = roomRepo.findByNumber(roomNum);
            if (room == null) return null; // 없는 방
            List<Reservation> allRes = resRepo.findAll();
            
            if (isRoomAvailable(roomNum, reqIn, reqOut, allRes)) {
                // 3. 예약 저장
                String nowStr = LocalDateTime.now().format(formatter);
                String resId = resRepo.add(roomNum, name, reqIn, reqOut, guestNum, phone, nowStr, request);
                return (resId != null) ? roomNum : null;
            }
            return null; // 이미 예약됨
        }
    }
    
    public String getRoomDashboard(String targetDate) {
        StringBuilder sb = new StringBuilder("DASHBOARD_LIST:");
        List<Room> rooms = roomRepo.findAll();
        List<Reservation> reservations = resRepo.findAll();
        String today = LocalDate.now().toString();
        for (Room r : rooms) {
            String status = "Empty";
            String guestName = "-";
            String resId = "-";
            int guestNum = 0; 
            String phone = "-";
            String inDate = "-"; 
            String outDate = "-";
            String note = r.getDescription();
            String detail = "-";

            // 예약 확인
            for (Reservation res : reservations) {
                if (res.getRoomNumber().equals(r.getRoomNumber())) {
                    // 날짜 범위 확인: 입실일 <= 조회일 < 퇴실일
                    // (퇴실일 당일은 아직 체크아웃 전이라도, 숙박의 관점에서는 오후에 빈 방이 됨)
                    if (isDateIncluded(targetDate, res.getCheckInDate(), res.getCheckOutDate())
                            && targetDate.compareTo(res.getCheckOutDate()) < 0) {

                        status = res.getReservationStatus();
                        guestName = res.getGuestName();
                        resId = res.getReservationId();
                        guestNum = res.getGuestNum();
                        phone = res.getPhoneNumber();
                        inDate = res.getCheckInDate();
                        outDate = res.getCheckOutDate();
                        detail = res.getCustomerRequest(); // 요청사항
                        break;
                    }
                }
            }
            
            if (targetDate.equals(today) && cleaningRooms.contains(r.getRoomNumber())) {
                status = "Cleaning"; 
            }
            
            sb.append(String.format("%s,%s,%d,%s,%s,%s,%d,%s,%s,%s,%s,%s|", 
                    r.getRoomNumber(), r.getType(), r.getPrice(), status, guestName, 
                    resId, guestNum, phone, inDate, outDate, note, detail));
        }
        return sb.toString();
    }
    
    public boolean addRoom(String num, String type, int price, int cap, String desc) {
        return roomRepo.add(new Room(num, type, price, cap, desc));
    }

    public boolean updateRoom(String num, String type, int price, int cap, String desc) {
        return roomRepo.update(new Room(num, type, price, cap, desc));
    }
    
    private boolean isDateIncluded(String target, String start, String end) {
        return target.compareTo(start) >= 0 && target.compareTo(end) <= 0;
    }
    
    public boolean updateReservationRequest(String resId, String newRequest) {
        synchronized (LOCK) {
            return resRepo.updateRequest(resId, newRequest);
        }
    }
    
    public boolean updateReservationStatus(String resId, String newStatus) {
        synchronized (LOCK) {
            return resRepo.updateStatus(resId, newStatus);
        }
    }
    
    public ReservationRepository getReservationRepository(){
        return resRepo;
    }
    
    public RoomRepository getRoomRepository(){
        return roomRepo;
    }
}