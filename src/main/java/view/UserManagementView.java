/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import controller.UserManagementController;
import model.User;
import service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementView extends JFrame {
    
    // 컴포넌트 선언
    private JComboBox<String> roleComboBox; 
    private static final String CSR_ROLE = "고객 서비스 담당자 (CSR)";
    private static final String ADMIN_ROLE = "관리자 (ADMIN)";

    // 컨트롤러 및 서비스 객체
    private final UserManagementController controller;
    
    // ⭐️ UI 컴포넌트 ⭐️
    private final JTable userTable;
    private final DefaultTableModel tableModel;
    private final JButton addButton;    // 등록
    private final JButton updateButton; // 수정
    private final JButton deleteButton; // 삭제 (SFR-601)

    // ⭐️ 입력 필드 (등록/수정용) ⭐️
    private final JTextField userIdField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JTextField nameField = new JTextField(15);
    private final JTextField phoneNumberField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);
    
    // 테이블 헤더 정의
    private final String[] columnNames = {"ID", "이름", "권한", "전화번호", "이메일"};

    public UserManagementView(UserService userService) {
        super("직원/권한 관리 (SFR-601)");
        
        // 1. Controller 초기화 (Service에 의존)
        this.controller = new UserManagementController(userService);
        
        // 2. 테이블 모델 및 JTable 초기화 (SFR-602)
        this.tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 테이블 셀 직접 수정 금지 (수정은 별도의 팝업이나 필드에서 진행)
                return false; 
            }
        };
        this.userTable = new JTable(tableModel);
        
        addButton = new JButton("직원 등록");
        updateButton = new JButton("정보 수정");
        deleteButton = new JButton("직원 삭제");
        
        // 3. UI 디자인 및 레이아웃 구성
        initializeUI();
        
        // 4. 이벤트 리스너 설정
        setupEventListeners();
        
        // 5. 초기 데이터 로딩 (SFR-602)
        loadUserData();
        
        // 6. 창 설정
        this.setSize(800, 500);
        this.setLocationRelativeTo(null);
        // 메인 뷰에서 이 창을 띄울 것이므로, 닫을 때는 이 창만 닫히도록 설정
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
    }

    // =======================================================
    // ⭐️ UI 디자인 및 컴포넌트 배치 ⭐️
    // =======================================================
    private void initializeUI() {
        this.setLayout(new BorderLayout(10, 10)); // 전체 레이아웃
        
        // 초기화
        roleComboBox = new JComboBox<>(new String[]{CSR_ROLE, ADMIN_ROLE});
        roleComboBox.setSelectedItem(CSR_ROLE); // 기본값은 CSR로 설정
        
        // 1. 테이블 패널 (중앙)
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        this.add(tableScrollPane, BorderLayout.CENTER);

        // 2. 입력 및 버튼 패널 (남쪽)
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        
        // 2-1. 입력 필드 패널
        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 10, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("직원 정보 입력/수정"));
        
        inputPanel.add(new JLabel("ID:"));
        inputPanel.add(userIdField);
        inputPanel.add(new JLabel("PW:"));
        inputPanel.add(passwordField);
        inputPanel.add(new JLabel("권한:"));
        inputPanel.add(roleComboBox);
        inputPanel.add(new JLabel("이름:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("전화:"));
        inputPanel.add(phoneNumberField);
        inputPanel.add(new JLabel("이메일:"));
        inputPanel.add(emailField);
        
        southPanel.add(inputPanel, BorderLayout.CENTER);

        // 2-2. 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        this.add(southPanel, BorderLayout.SOUTH);
    }

    // =======================================================
    // ⭐️ 이벤트 처리 및 Controller 연동 ⭐️
    // =======================================================
    private void setupEventListeners() {
        // [등록 버튼] 클릭 (SFR-601)
        addButton.addActionListener(e -> handleAddStaff());
        
        // [삭제 버튼] 클릭 (SFR-601)
        deleteButton.addActionListener(e -> handleDeleteStaff());
        
        // [테이블 Row 선택] 시, 입력 필드에 데이터 로드 (수정 준비)
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() != -1) {
                loadFieldsFromSelectedRow();
            }
        });
        
        // [수정 버튼] 클릭 (SFR-601)
        updateButton.addActionListener(e -> handleUpdateStaff());
    }

    /**
     * SFR-601: 직원 등록 처리
     */
    private void handleAddStaff() {
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());
        String name = nameField.getText();
        String phone = phoneNumberField.getText();
        String email = emailField.getText();
        
        // Controller에 등록 요청 위임
        String message = controller.registerNewStaff(userId, password, name, phone, email);
        
        // Controller가 반환한 메시지를 팝업으로 표시
        JOptionPane.showMessageDialog(this, message);
        
        // 성공적으로 등록되었다면 테이블 갱신
        if (!message.startsWith("오류")) {
            loadUserData();
            clearFields();
        }
    }
    
    /**
     * SFR-601: 직원 삭제 처리
     */
    private void handleDeleteStaff() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 직원을 테이블에서 선택해주세요.");
            return;
        }

        // 선택된 행의 첫 번째 컬럼(ID)을 가져옵니다.
        String userIdToDelete = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "정말로 [" + userIdToDelete + "] 직원을 삭제하시겠습니까?", "삭제 확인", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = controller.deleteStaff(userIdToDelete);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "직원 [" + userIdToDelete + "] 삭제 완료.");
                loadUserData(); // 테이블 갱신
                clearFields();
            } else {
                // UserService에서 구현한 '관리자 삭제 금지' 등의 오류 메시지가 발생할 수 있음
                JOptionPane.showMessageDialog(this, "삭제에 실패했습니다. (관리자 계정은 삭제할 수 없습니다.)", 
                                              "삭제 오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * SFR-601: 직원 정보 수정 처리
     * (이 코드는 비밀번호 변경은 포함하지 않고, 이름/전화번호/이메일만 수정한다고 가정)
     */
    private void handleUpdateStaff() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 직원을 테이블에서 선택해주세요.");
            return;
        }
        
        // 새로운 권한 추출 및 변환
        String selectedRoleString = (String) roleComboBox.getSelectedItem();
        
        // 현재 선택된 ID와 수정된 이름/전화번호/이메일을 가져와 새 User 객체를 생성 (권한은 그대로 유지)
        String newPassword = new String(passwordField.getPassword());
        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText();
        String phoneNumber = phoneNumberField.getText();
        String email = emailField.getText();
        // String newRoleCode = selectedRoleString.contains("ADMIN") ? "ADMIN : "CSR";

        boolean infoUpdateSuccess = false;
        boolean passwordUpdateSuccess = true;
        String finalMessage = "정보 수정 완료: ";
        
        // 일반 정보 업데이트 처리
        // 이름, 전화번호, 이메일은 항상 업데이트 시도
        infoUpdateSuccess = controller.updateStaffInfo(userId, name, phoneNumber, email, selectedRoleString);
        
        if (!newPassword.isEmpty()) {
            // 비밀번호 필드가 채워져 있다면 변경 로직 실행
            passwordUpdateSuccess = controller.changeStaffPassword(userId, newPassword);
            
            if (passwordUpdateSuccess) {
                finalMessage += "비밀번호 변경 완료.";
            }
            else {
                finalMessage += "비밀번호 변경 실패.";
            }
        }
        else {
            finalMessage += "비밀번호는 변경되지 않았습니다.";
        }
        
        // 최종 결과 메시지 표시 및 갱신
        if (infoUpdateSuccess && passwordUpdateSuccess) {
            JOptionPane.showMessageDialog(this, finalMessage, "수정 완료", JOptionPane.INFORMATION_MESSAGE);
            loadUserData(); // 테이블 갱신 (성공 시)
            clearFields();
        }
        else {
            JOptionPane.showMessageDialog(this, "정보 수정 중 오류가 발생했습니다. (일반 정보 또는 비밀번호)", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadUserData() {
        tableModel.setRowCount(0);
        List<User> users = controller.getAllStaff();
        
        for (User user : users) {
        Object[] rowData = {
            user.getUserId(),
            user.getName(),
            user.getRole().getDescription(),
            user.getPhoneNumber(),
            user.getEmail()
        };
        tableModel.addRow(rowData);
        }
    }

    // 기타 유틸리티 메서드
    private void loadFieldsFromSelectedRow() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            // 테이블의 데이터를 입력 필드에 표시 (PW는 보안상 제외)
            userIdField.setText((String) tableModel.getValueAt(row, 0));
            nameField.setText((String) tableModel.getValueAt(row, 1));
            // 권한은 필드에 표시하지 않음
            phoneNumberField.setText((String) tableModel.getValueAt(row, 3));
            emailField.setText((String) tableModel.getValueAt(row, 4));
            
            // 권한 (인덱스 2) 로드 로직 추가
            String currentRoleDescription = (String) model.getValueAt(row, 2);
            
            roleComboBox.setSelectedItem(currentRoleDescription);
            // 수정 시 ID 변경을 막기 위해 비활성화
            userIdField.setEnabled(false); 
        }
    }
    
    private void clearFields() {
        userIdField.setText("");
        passwordField.setText("");
        nameField.setText("");
        phoneNumberField.setText("");
        emailField.setText("");
        userIdField.setEnabled(true);
    }
}