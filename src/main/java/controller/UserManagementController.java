/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import model.User;
import service.UserService;

import java.util.List;
import model.UserRole;

public class UserManagementController {
    private final UserService userService;
    
    public UserManagementController(UserService userService) {
        this.userService = userService;
    }
    // =======================================================
    // SFR-601: 신규 CSR 등록 요청 처리
    // =======================================================
    /**
     * @param userId ID
     * @param password 비밀번호
     * @param name 이름
     * @param phoneNumber 전화번호
     * @param email 이메일
     * @return 성공 메시지 또는 실패 원인이 담긴 오류 메시지
     */
    public String registerNewStaff(String userId, String password, String name, String phoneNumber, String email) {
        // 1. View에서 받은 입력 데이터의 기본 유효성 검사 (Controller에서 처리)
        if (userId == null || userId.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return "오류: 사용자 ID와 비밀번호는 필수 입력 항목입니다.";
        }
        
        // 2. Service 계층으로 데이터 전달 (ID 중복 확인 및 CSR 역할 고정은 Service에서 처리)
        boolean success = userService.registerCSR(userId.trim(), password, name, phoneNumber, email);
        
        if (success) {
            return "직원 [" + name + "]의 등록이 완료되었습니다.";
        }
        else {
            // 실패 시 : Service에서 처리된 결과를 바탕으로 메시지 반환
            return "오류: 사용자 ID [" + userId + "]는 이미 존재합니다.";
        }
    }
    
    // =======================================================
    // SFR-601: 직원 정보 수정 요청 처리
    // =======================================================
    /**
     * @param userId 수정할 사용자 ID
     * @param name 수정된 사용자 이름
     * @param phoneNumber 수정된 사용자 전화번호
     * @param email 수정된 사용자 이메일
     * @param newRoleString 수정된 권한
     * @return 수정 성공 여부
     */
    public boolean updateStaffInfo(String userId, String name, String phoneNumber, String email, String newRoleString) {
        // View에서 수정 요청이 들어온 User 객체를 Service로 전달
        return userService.updateUserInfo(userId, name, phoneNumber, email, newRoleString);
    }
    
    // =======================================================
    // SFR-601: 직원 정보 삭제 요청 처리
    // =======================================================
    /**
     * @param userId 삭제할 사용자 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteStaff(String userId) {
        // Service 계층으로 삭제 요청 전달 (관리자 계정 삭제 금지 규칙 등은 Service에서 처리)
        return userService.deleteUser(userId);
    }
    
    /**
     * SFR-602 사용자 목록 조회
     * @return 시스템에 등록된 모든 User 객체의 리스트
     */
    public List<User> getAllStaff() {
        return userService.getAllUsers();
    }
    
    /**
     * SFR-604 사용자 비밀번호 변경
     * @param userId 변경할 사용자의 id
     * @param newPassword 새 비밀번호
     * @return 음...
     */
    
    public boolean changeStaffPassword(String userId, String newPassword) {
        return userService.changePassword(userId, newPassword);
    }
}
