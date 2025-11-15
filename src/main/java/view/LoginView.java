/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import controller.LoginController;
import service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {

    // ⭐️ UI 컴포넌트 ⭐️
    private final JTextField userIdField;
    private final JPasswordField passwordField;
    private final JButton loginButton;

    // ⭐️ 컨트롤러 객체 ⭐️
    private final LoginController loginController;

    /**
     * View는 Controller를 알지만, Controller는 Service를 통해 비즈니스 로직을 처리합니다.
     * @param userService Controller 초기화를 위해 Service 객체를 주입받습니다.
     */
    public LoginView(UserService userService) {
        super("호텔 관리 시스템 - 로그인"); // 창 제목 설정
        
        // 1. Controller 초기화
        // Controller는 View의 인스턴스를 받아 로그인 성공 시 창을 닫는 데 사용합니다.
        this.loginController = new LoginController(userService, this);
        
        // 2. 컴포넌트 초기화
        this.userIdField = new JTextField(15);
        this.passwordField = new JPasswordField(15);
        this.loginButton = new JButton("로그인");

        // 3. UI 디자인 및 레이아웃 구성
        initializeUI();

        // 4. 이벤트 리스너 연결
        setupEventListeners();
        
        // 5. 창 설정
        this.setSize(350, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫을 때 프로그램 종료
        this.setLocationRelativeTo(null); // 화면 중앙에 표시
    }

    private void initializeUI() {
        // 메인 패널 생성 및 레이아웃 설정 (GridBagLayout 사용 예시)
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 컴포넌트 간의 여백

        // --- User ID (사용자 ID) ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(userIdField, gbc);

        // --- Password (비밀번호) ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("PW:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        // --- Login Button (로그인 버튼) ---
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST; // 버튼을 오른쪽으로 정렬
        panel.add(loginButton, gbc);

        this.add(panel);
    }
    
    /**
     * 로그인 버튼 클릭 이벤트 리스너 설정
     */
    private void setupEventListeners() {
        // 로그인 버튼 클릭 시 이벤트 처리
        loginButton.addActionListener(e -> attemptLogin());
        
        // 비밀번호 필드에서 Enter 키 입력 시에도 로그인 시도
        passwordField.addActionListener(e -> attemptLogin());
    }

    /**
     * 로그인 시도 로직 (Controller에 위임)
     */
    private void attemptLogin() {
        String userId = userIdField.getText();
        // JPasswordField는 getPassword() 후 String으로 변환해야 하지만, 
        // 보안상 위험하므로 char[]를 바로 사용하거나 getText()를 사용하는 경우도 많음
        String password = new String(passwordField.getPassword()); 
        
        // ⭐️ Controller에 로그인 시도 위임 ⭐️
        // View는 로직을 처리하지 않고, 데이터만 Controller로 전달합니다.
        loginController.attemptLogin(userId, password);
    }
}