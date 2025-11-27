package server.repository;

import server.model.MenuOrder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 메뉴 주문 내역(menu_orders.csv) 파일을 관리하는 저장소 클래스
 * - 주문 내역 전체 조회 및 단일 주문 저장 기능 제공
 * - 동기화(synchronized)로 멀티스레드 환경에서 파일 접근 충돌 방지
 */
public class MenuOrderRepository {
    /**
     * 주문 내역이 저장되는 CSV 파일 경로
     */
    private static final String ORDER_FILE_PATH = "data/menu_orders.csv";

    /**
     * 주문 시간 포맷 (예: 2025-11-27 14:30:00)
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 모든 주문 내역을 menu_orders.csv에서 읽어와 리스트로 반환
     * - 파일이 없으면 빈 리스트 반환
     * - 각 주문은 MenuOrder 객체로 변환
     * - 파일 접근 및 파싱 중 예외 발생 시 스택 트레이스 출력
     * - 동기화로 멀티스레드 환경에서 안전하게 동작
     * @return 주문 내역 리스트
     */
    public synchronized List<MenuOrder> findAll() {
        List<MenuOrder> orders = new ArrayList<>();
        File file = new File(ORDER_FILE_PATH);
        // 주문 내역 파일이 없으면 빈 리스트 반환
        if (!file.exists()) return orders;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // 첫 줄(헤더) 스킵
            while ((line = reader.readLine()) != null) {
                // 빈 줄은 무시
                if (line.trim().isEmpty()) continue;
                // CSV를 6개 항목으로 분리 (SaleId, GuestName, OrderTime, TotalPrice, Payment, FoodName)
                String[] parts = line.split(",", 6);
                if (parts.length == 6) {
                    // 각 필드를 파싱하여 MenuOrder 객체 생성
                    String saleId = parts[0].trim();
                    String guestName = parts[1].trim();
                    LocalDateTime orderTime = LocalDateTime.parse(parts[2].trim(), FORMATTER);
                    int totalPrice = Integer.parseInt(parts[3].trim());
                    String payment = parts[4].trim();
                    // 여러 음식 이름은 '|'로 구분되어 저장됨
                    List<String> foodNames = Arrays.asList(parts[5].split("\\|"));
                    orders.add(new MenuOrder(saleId, guestName, orderTime, totalPrice, payment, foodNames));
                }
                // 필드 개수가 맞지 않으면 해당 줄은 무시 (데이터 손상 방지)
            }
        } catch (IOException e) {
            // 파일 읽기 중 예외 발생 시 콘솔에 에러 출력
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * 단일 주문 내역을 menu_orders.csv에 저장
     * - 파일이 없거나 비어있으면 헤더를 먼저 작성
     * - 주문 정보는 CSV 한 줄로 저장 (음식 이름은 '|'로 구분)
     * - 파일 접근 중 예외 발생 시 스택 트레이스 출력
     * - 동기화로 멀티스레드 환경에서 안전하게 동작
     * @param order 저장할 주문 객체
     */
    public synchronized void save(MenuOrder order) {
        // 파일이 없거나 비어있으면 헤더 필요
        boolean needHeader = !(new File(ORDER_FILE_PATH)).exists() || (new File(ORDER_FILE_PATH)).length() == 0;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ORDER_FILE_PATH, true), StandardCharsets.UTF_8))) {
            if (needHeader) {
                // 첫 저장 시 헤더 작성
                writer.write("SaleId,GuestName,OrderTime,TotalPrice,Payment,FoodName");
                writer.newLine();
            }
            // 음식 이름 리스트를 '|'로 연결하여 문자열로 변환
            String foodNamesStr = String.join("|", order.getFoodNames());
            // 주문 정보를 CSV 포맷으로 변환
            String line = String.format("%s,%s,%s,%d,%s,%s", order.getSaleId(), order.getGuestName(), order.getOrderTime().format(FORMATTER), order.getTotalPrice(), order.getPayment(), foodNamesStr);
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            // 파일 쓰기 중 예외 발생 시 콘솔에 에러 출력
            e.printStackTrace();
        }
    }
}
