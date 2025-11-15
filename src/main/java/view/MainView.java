/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import model.User;
import model.UserRole;
import service.UserService;
// import controller.UserManagementController; // 향후 컨트롤러 사용

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainView extends JFrame {
    private final User loggedInUser; // 로그인된 사용자 객체 (권한 정보 포함)
    private final UserService userService;

    // ⭐️ 관리자 전용 메뉴 (SFR-605 제어 대상) ⭐️
    private final JButton userManagementButton;   // SFR-601
    private final JButton systemReportButton; // SFR-800
    private final JButton roomTypeManagementButton; // (예시: 시스템 설정 관련)

    // CSR/고객 공통 메뉴 (예시)
    private final JButton reservationButton;
    private final JButton checkInOutButton;
    
    private final JPanel menuPanel;

    public MainView(User user, UserService userService) {
        super("호텔 관리 시스템 - " + user.getRole().getDescription()); // 창 제목에 권한 표시
        this.loggedInUser = user;
        this.userService = userService;
        
        // --- 1. 컴포넌트 초기화 ---
        userManagementButton = new JButton("직원/권한 관리 (SFR-601)");
        systemReportButton = new JButton("시스템 보고서 (SFR-800)");
        roomTypeManagementButton = new JButton("객실 유형 관리");
        
        reservationButton = new JButton("예약 및 조회");
        checkInOutButton = new JButton("체크인/아웃");
        
        // --- 2. 레이아웃 설정 (예시: FlowLayout) ---
        menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        // 버튼 패널에 추가
        menuPanel.add(userManagementButton);
        menuPanel.add(systemReportButton);
        menuPanel.add(roomTypeManagementButton);
        menuPanel.add(reservationButton);
        menuPanel.add(checkInOutButton);
        
        this.add(menuPanel, BorderLayout.NORTH); // 상단에 메뉴 패널 추가

        // --- 3. ⭐️ SFR-605 핵심: 권한 제어 로직 실행 ⭐️ ---
        applyAuthorization(user.getRole());
        
        // --- 4. 이벤트 리스너 등록 (직원 관리 버튼 예시) ---
        userManagementButton.addActionListener(this::handleUserManagementClick);

        // --- 5. 프레임 설정 ---
        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null); // 화면 중앙에 표시
    }
    
    // =======================================================
    // ⭐️ SFR-605 구현의 핵심 메서드: 권한에 따른 메뉴 제어 ⭐️
    // =======================================================
    private void applyAuthorization(UserRole role) {
        // 모든 메뉴를 기본적으로 비활성화 또는 숨김 처리한 후, 권한에 맞는 것만 활성화/표시합니다.
        
        // 1. 관리자 전용 기능 (ADMIN 권한만 가능)
        boolean isAdmin = (role == UserRole.ADMIN);
        
        // SFR-601, SFR-800 등 관리자 기능 버튼의 가시성 설정
        userManagementButton.setVisible(isAdmin);
        systemReportButton.setVisible(isAdmin);
        roomTypeManagementButton.setVisible(isAdmin);

        // 2. CSR/직원 전용 기능 (ADMIN과 CSR 모두 가능)
        // CSR 이상 (ADMIN 포함)이면 예약, 체크인/아웃 가능
        boolean isStaff = (role == UserRole.ADMIN || role == UserRole.CSR);
        
        reservationButton.setVisible(isStaff);
        checkInOutButton.setVisible(isStaff);
        
        // 참고: 고객(CUSTOMER) 권한은 로그인 시 다른 View(예: MyReservationView)를 띄우거나, 
        // MainView에서 오직 자기 정보만 조회하는 메뉴만 보이도록 설정합니다.
    }
    
    // =======================================================
    // ⭐️ 버튼 클릭 이벤트 처리 (다음 화면 연결) ⭐️
    // =======================================================
    private void handleUserManagementClick(ActionEvent e) {
        // 직원 관리 버튼 클릭 시, SFR-601 화면(UserManagementView)을 띄웁니다.
        // UserManagementController를 통해 Service/Repository와 연동됩니다.
        
        // UserManagementView를 띄우는 코드 (UserService 객체가 필요함)
        // (주의: 여기서는 UserService 객체를 넘겨주는 로직이 생략되어 있습니다. 
        // 실제 구현 시에는 MainView를 띄울 때 UserService 객체를 함께 전달받아야 합니다.)
        System.out.println("직원 관리 화면 열기 시도 (권한: " + loggedInUser.getRole() + ")");
        
        // 예시: new UserManagementView(userService).setVisible(true);
        // this.setEnabled(false);
        UserManagementView userManagementView = new UserManagementView(this.userService);
        userManagementView.setVisible(true);

        System.out.println("직원 관리 화면 열기 성공 (권한: " + loggedInUser.getRole().getDescription() + ")");
    }
}
