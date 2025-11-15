/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import controller.RoomAdminPanel;
import service.HotelService;
import model.Room;
import model.Reservation;
import model.Reservation.ReservationStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 호텔 객실 관리자 패널의 Swing 기반 GUI View
 * RoomAdminPanel과 연동되어 사용자 요청을 처리하고 결과를 표시
 */
public class RoomManagementView extends JFrame {
    
    private RoomAdminPanel controller;
    private JTextArea outputArea;
    
    // 쿼리 패널 입력 필드
    private JTextField txtRoomNumQuery;
    private JTextField txtGuestNameQuery;
    private JTextField txtCheckInDateQuery;
    private JTextField txtCheckOutDateQuery;
    
    // 관리(CRUD) 패널 입력 필드
    private JTextField txtRoomNumCreate;
    private JTextField txtGuestNameCreate;
    private JTextField txtCheckInDateCreate;
    private JTextField txtCheckOutDateCreate;
    private JTextField txtResIdUpdate;
    private JTextField txtGuestNameUpdate;
    private JTextField txtCheckInDateUpdate;
    private JTextField txtCheckOutDateUpdate;
    private JTextField txtResIdCancel;
    private JTextField txtResIdStatus;
    private JComboBox<String> cmbStatus;

    /**
     * View 생성자: UI 컴포넌트를 초기화하고 컨트롤러 설정
     * @param controller 객실 관리 작업을 담당하는 컨트롤러
     */
    public RoomManagementView(RoomAdminPanel controller) {
        this.controller = controller;
        setTitle("호텔 객실 및 예약 관리 시스템 (관리자)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initComponents();
        
        // 애플리케이션 시작 시 로직 실행 (선택 사항)
        SwingUtilities.invokeLater(() -> controller.startPanel());
        
        pack(); // 컴포넌트 크기에 맞춰 프레임 크기 조정
        setLocationRelativeTo(null); // 화면 중앙에 표시
        setVisible(true);
    }
    
    private void initComponents() {
        // 메인 컨테이너 설정
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. 탭 패널 (쿼리/관리)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("조회 기능 (SFR-20x)", createQueryPanel());
        tabbedPane.addTab("예약 관리 (SFR-40x)", createManagementPanel());
        add(tabbedPane, BorderLayout.NORTH);

        // 2. 출력 영역
        outputArea = new JTextArea(20, 80);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 조회(Query) 기능을 위한 패널을 생성 (SFR-201, 202, 204, 205)
     */
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 3, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("객실 및 예약 조회"));
        
        // SFR-201: 전체 객실 정보 조회
        JButton btnGetAllRooms = new JButton("SFR-201: 전체 객실 조회");
        btnGetAllRooms.addActionListener(e -> controller.handleGetAllRooms());
        panel.add(btnGetAllRooms);
        panel.add(new JLabel("")); // Placeholder
        panel.add(new JLabel("")); // Placeholder

        // SFR-202: 객실 번호로 조회
        panel.add(new JLabel("SFR-202 객실 번호:"));
        txtRoomNumQuery = new JTextField(10);
        panel.add(txtRoomNumQuery);
        JButton btnGetRoomByNum = new JButton("객실 정보 조회");
        btnGetRoomByNum.addActionListener(e -> {
            String roomNum = txtRoomNumQuery.getText().trim();
            if (!roomNum.isEmpty()) {
                controller.handleGetRoomByNumber(roomNum);
            } else {
                displayError("객실 번호를 입력해주세요.");
            }
        });
        panel.add(btnGetRoomByNum);
        
        // SFR-204: 투숙객 이름으로 예약 조회
        panel.add(new JLabel("SFR-204 고객명:"));
        txtGuestNameQuery = new JTextField(10);
        panel.add(txtGuestNameQuery);
        JButton btnGetResByGuestName = new JButton("예약 목록 조회");
        btnGetResByGuestName.addActionListener(e -> {
            String guestName = txtGuestNameQuery.getText().trim();
            if (!guestName.isEmpty()) {
                controller.handleGetReservationsByGuestName(guestName);
            } else {
                displayError("고객 이름을 입력해주세요.");
            }
        });
        panel.add(btnGetResByGuestName);

        // SFR-205: 특정 기간 예약 가능 객실 조회
        panel.add(new JLabel("SFR-205 기간 (YYYY-MM-DD):"));
        JPanel datePanel = new JPanel(new GridLayout(1, 4, 2, 2));
        txtCheckInDateQuery = new JTextField("2025-01-01", 8);
        txtCheckOutDateQuery = new JTextField("2025-01-05", 8);
        datePanel.add(txtCheckInDateQuery);
        datePanel.add(new JLabel("~"));
        datePanel.add(txtCheckOutDateQuery);
        panel.add(datePanel);

        JButton btnGetAvailableRooms = new JButton("예약 가능 객실 조회");
        btnGetAvailableRooms.addActionListener(e -> {
            String checkIn = txtCheckInDateQuery.getText().trim();
            String checkOut = txtCheckOutDateQuery.getText().trim();
            if (!checkIn.isEmpty() && !checkOut.isEmpty()) {
                controller.handleGetAvailableRooms(checkIn, checkOut);
            } else {
                displayError("체크인/체크아웃 날짜를 모두 YYYY-MM-DD 형식으로 입력해주세요.");
            }
        });
        panel.add(btnGetAvailableRooms);
        
        return panel;
    }

    /**
     * 예약 관리(CRUD) 기능을 위한 패널을 생성 (SFR-401, 402, 403, 404)
     */
    private JPanel createManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("예약 관리 (CRUD)"));

        // --- SFR-401: 예약 생성 ---
        panel.add(createReservationCreationPanel());
        
        // --- SFR-402: 예약 수정 ---
        panel.add(createReservationUpdatePanel());
        
        // --- SFR-403: 예약 취소 ---
        panel.add(createReservationCancelPanel());
        
        // --- SFR-404: 예약 상태 변경 ---
        panel.add(createStatusChangePanel());

        return panel;
    }

    private JPanel createReservationCreationPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder("SFR-401: 예약 생성 (RoomNum, GuestName, InDate, OutDate)"));
        txtRoomNumCreate = new JTextField(5);
        txtGuestNameCreate = new JTextField(10);
        txtCheckInDateCreate = new JTextField("YYYY-MM-DD", 8);
        txtCheckOutDateCreate = new JTextField("YYYY-MM-DD", 8);

        p.add(new JLabel("객실#"));
        p.add(txtRoomNumCreate);
        p.add(new JLabel("고객명"));
        p.add(txtGuestNameCreate);
        p.add(new JLabel("In"));
        p.add(txtCheckInDateCreate);
        p.add(new JLabel("Out"));
        p.add(txtCheckOutDateCreate);

        JButton btnCreate = new JButton("예약 생성");
        btnCreate.addActionListener(this::handleCreateReservationAction);
        p.add(btnCreate);
        return p;
    }
    
    private void handleCreateReservationAction(ActionEvent e) {
        String roomNum = txtRoomNumCreate.getText().trim();
        String guestName = txtGuestNameCreate.getText().trim();
        String checkIn = txtCheckInDateCreate.getText().trim();
        String checkOut = txtCheckOutDateCreate.getText().trim();
        
        if (roomNum.isEmpty() || guestName.isEmpty() || checkIn.isEmpty() || checkOut.isEmpty()) {
            displayError("예약 생성에 필요한 모든 필드를 채워주세요.");
            return;
        }
        controller.handleCreateReservation(roomNum, guestName, checkIn, checkOut);
    }
    
    private JPanel createReservationUpdatePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder("SFR-402: 예약 수정 (ResID, NewGuestName, NewInDate, NewOutDate)"));
        txtResIdUpdate = new JTextField(10);
        txtGuestNameUpdate = new JTextField(10);
        txtCheckInDateUpdate = new JTextField("YYYY-MM-DD", 8);
        txtCheckOutDateUpdate = new JTextField("YYYY-MM-DD", 8);

        p.add(new JLabel("예약ID"));
        p.add(txtResIdUpdate);
        p.add(new JLabel("새 고객명"));
        p.add(txtGuestNameUpdate);
        p.add(new JLabel("새 In"));
        p.add(txtCheckInDateUpdate);
        p.add(new JLabel("새 Out"));
        p.add(txtCheckOutDateUpdate);

        JButton btnUpdate = new JButton("예약 수정");
        btnUpdate.addActionListener(this::handleUpdateReservationAction);
        p.add(btnUpdate);
        return p;
    }

    private void handleUpdateReservationAction(ActionEvent e) {
        String resId = txtResIdUpdate.getText().trim();
        String newGuestName = txtGuestNameUpdate.getText().trim();
        String newCheckIn = txtCheckInDateUpdate.getText().trim();
        String newCheckOut = txtCheckOutDateUpdate.getText().trim();
        
        if (resId.isEmpty() || newGuestName.isEmpty() || newCheckIn.isEmpty() || newCheckOut.isEmpty()) {
            displayError("예약 수정에 필요한 모든 필드를 채워주세요.");
            return;
        }
        controller.handleUpdateReservation(resId, newGuestName, newCheckIn, newCheckOut);
    }
    
    private JPanel createReservationCancelPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder("SFR-403: 예약 취소 (ResID)"));
        txtResIdCancel = new JTextField(10);
        
        p.add(new JLabel("예약ID"));
        p.add(txtResIdCancel);
        
        JButton btnCancel = new JButton("예약 취소");
        btnCancel.addActionListener(e -> {
            String resId = txtResIdCancel.getText().trim();
            if (!resId.isEmpty()) {
                controller.handleCancelReservation(resId);
            } else {
                displayError("취소할 예약 ID를 입력해주세요.");
            }
        });
        p.add(btnCancel);
        return p;
    }
    
    private JPanel createStatusChangePanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setBorder(BorderFactory.createTitledBorder("SFR-404: 예약 상태 변경 (ResID, Status)"));
        txtResIdStatus = new JTextField(10);
        cmbStatus = new JComboBox<>(new String[]{"CHECKED_IN", "CHECKED_OUT"});

        p.add(new JLabel("예약ID"));
        p.add(txtResIdStatus);
        p.add(new JLabel("새 상태"));
        p.add(cmbStatus);

        JButton btnChangeStatus = new JButton("상태 변경");
        btnChangeStatus.addActionListener(e -> {
            String resId = txtResIdStatus.getText().trim();
            String status = (String) cmbStatus.getSelectedItem();
            if (!resId.isEmpty() && status != null) {
                controller.handleChangeStatus(resId, status);
            } else {
                displayError("상태를 변경할 예약 ID와 상태를 선택해주세요.");
            }
        });
        p.add(btnChangeStatus);
        return p;
    }
    
    // ===============================================
    // View 인터페이스 (Controller가 호출하는 메서드)
    // ===============================================

    /**
     * 일반 메시지를 출력
     */
    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> outputArea.append("\n[INFO] " + message + "\n"));
    }

    /**
     * 오류 메시지를 출력 영역에 표시하고 경고 
     */
    public void displayError(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append("\n[ERROR] " + message + "\n");
            JOptionPane.showMessageDialog(this, message, "오류 발생", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * 객실 목록을 표 형식으로 출력 영역에 표시
     */
    public void displayRoomList(List<Room> rooms, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================\n");
        sb.append("== ").append(title).append(" ==\n");
        sb.append("=========================================================\n");
        if (rooms.isEmpty()) {
            sb.append("조회된 객실이 없습니다.\n");
        } else {
            // 헤더
            sb.append(String.format("%-10s | %-10s | %-5s | %s\n", "객실 번호", "유형", "인원", "가격 (1박)"));
            sb.append("---------------------------------------------------------\n");
            // 데이터
            for (Room room : rooms) {
                sb.append(String.format("%-10s | %-10s | %-5d | %,.0f원\n",
                    room.getRoomNumber(), room.getRoomType(), room.getCapacity(), room.getPricePerNight()));
            }
        }
        sb.append("=========================================================\n");
        SwingUtilities.invokeLater(() -> outputArea.append(sb.toString()));
    }

    /**
     * 예약 목록을 표 형식으로 출력 영역에 표시
     */
    public void displayReservationList(List<Reservation> reservations, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("== ").append(title).append(" ==\n");
        sb.append("========================================================================================\n");
        if (reservations.isEmpty()) {
            sb.append("조회된 예약이 없습니다.\n");
        } else {
            // 헤더
            sb.append(String.format("%-12s | %-6s | %-15s | %-10s | %-10s | %s\n", 
                "예약 ID", "객실#", "고객명", "체크인", "체크아웃", "상태"));
            sb.append("----------------------------------------------------------------------------------------\n");
            // 데이터
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Reservation res : reservations) {
                sb.append(String.format("%-12s | %-6s | %-15s | %-10s | %-10s | %s\n",
                    res.getReservationId(), 
                    res.getRoomNumber(), 
                    res.getGuestName(), 
                    res.getCheckInDate().format(dtf),
                    res.getCheckOutDate().format(dtf),
                    res.getStatus()));
            }
        }
        sb.append("========================================================================================\n");
        SwingUtilities.invokeLater(() -> outputArea.append(sb.toString()));
    }

    /**
     * 단일 예약의 상세 정보를 출력 영역에 표시
     */
    public void displayReservationDetail(Reservation reservation) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- [예약 상세 정보] ---\n");
        sb.append("예약 ID: ").append(reservation.getReservationId()).append("\n");
        sb.append("객실 번호: ").append(reservation.getRoomNumber()).append("\n");
        sb.append("고객명: ").append(reservation.getGuestName()).append("\n");
        sb.append("체크인 날짜: ").append(reservation.getCheckInDate()).append("\n");
        sb.append("체크아웃 날짜: ").append(reservation.getCheckOutDate()).append("\n");
        sb.append("현재 상태: ").append(reservation.getStatus()).append("\n");
        sb.append("-----------------------\n");
        SwingUtilities.invokeLater(() -> outputArea.append(sb.toString()));
    }
    
    // ===============================================
    // 메인 메서드: 애플리케이션 실행
    // ===============================================

    public static void main(String[] args) {
        // 1. 서비스 초기화 (데이터 모델 관리)
        HotelService hotelService = new HotelService();
        
        // 2. View 초기화 및 Controller 생성
        SwingUtilities.invokeLater(() -> {
            RoomManagementView view = new RoomManagementView(null); // 초기 null 전달, 나중에 설정
            RoomAdminPanel controller = new RoomAdminPanel(hotelService, view);
            view.controller = controller; // View에 컨트롤러 인스턴스 설정
        });
    }
}
