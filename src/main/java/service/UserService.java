/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import model.User;
import model.UserRole;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;
/**
 *
 * @author USER
 */
public class UserService {
    
    // UserRepository를 주입받아 데이터에 접근
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    /**
     * SFR-605의 핵심: 로그인 처리 및 권한이 부여된 User 객체 반환
     * @param userId 사용자 ID
     * @param password 비밀번호
     * @return 로그인 성공 시 User 객체, 실패 시 빈 Optional
     */
    public Optional<User> login(String userId, String password) {
        // UserRepository를 통해 ID로 사용자 조회 (JSON 파일 접근)
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            // ID가 존재하지 않음
            return Optional.empty();
        }
        User user = userOptional.get();
        
        // 비밀번호 검증 (실제 구현에서는 암호화된 비밀번호를 비교해야 함)
        if (user.getPassword().equals(password)) {
            // 로그인 성공 시 권한 정보(Role)가 포함된 User 객체를 반환
            return Optional.of(user);
        }
        else {
            // 비밀번호 불일치
            return Optional.empty();
        }
    }
    
    // =====================================
    // SFR-601: CSR 정보 관리
    // =====================================
    
    /**
     * SFR-601: 신규 직원 등록 (CSR 역할 강제)
     * @param userId ID
     * @param password 비밀번호
     * @param name 이름
     * @param phoneNumber 전화번호
     * @param email 이메일
     * @return 성공적으로 등록되었는지 여부
     */
    public boolean registerCSR(String userId, String password, String name, String phoneNumber, String email) {
        // 비즈니스 규칙 적용: 신규 등록되는 직원의 역할은 CSR로 고정
        User newUser = new User(userId, password, name, phoneNumber, email, UserRole.CSR);
        
        // UserReository의 저장 기능 호출 (ID 중복 검사는 Repository에서 처리)
        return userRepository.save(newUser);
    }
    
    /**
     * SFR-601: 사용자 정보 업데이트 (이름, 전화번호, 권한 등)
     * @param userId 수정할 사용자 ID
     * @param name 새 이름
     * @param phoneNumber 새 전화번호
     * @param email 새 이메일
     * @param newRoleString 새 권한
     * @return 성공적으로 업데이트되었는지 여부
     */
    public boolean updateUserInfo(String userId, String name, String phoneNumber, String email, String newRoleString) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            
            // 기존 객체의 일반 정보만 View에서 받은 새 데이터로 업데이트
            // (기존 pw와 role은 existingUser 객체에 그대로 유지)
            existingUser.setName(name);
            existingUser.setPhoneNumber(phoneNumber);
            existingUser.setEmail(email);
            
            // 권한 정보 업데이트
            if (newRoleString.contains(("ADMIN"))) {
                existingUser.setRole(UserRole.ADMIN);
            }
            else {
                existingUser.setRole(UserRole.CSR);
            }
            
            // Repository에 수정된 객체를 저장
            userRepository.update(existingUser);
            return true;
        }
        return false;
    }
    
    /**
     * SFR-601: 사용자 삭제
     * @param userId 삭제할 사용자 ID
     * @return 성공적으로 삭제되었는지 여부
     */
    public boolean deleteUser(String userId) {
        // 비즈니스 규칙 적용: 특정 역할 (예시: admin)은 삭제 금지 등 규칙 검사 가능
        Optional<User> userToDelete = userRepository.findById(userId);
        
        // 삭제하려는 userId의 권한이 관리자일 경우
        if (userToDelete.isPresent() && userToDelete.get().getRole() == UserRole.ADMIN) {
            System.err.println("관리자 계정은 삭제할 수  없습니다.");
            return false;
        }
        return userRepository.delete(userId);
    }
    
    /**
     * SFR-602: 사용자 목록 조회
     * @return 모든 사용자의 List
     */
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * SFR-604: 사용자 비밀번호 변경
     * @param userId 변경할 사용자 ID
     * @param newPassword 변경할 사용자의 비밀번호
     * @return 뭐였더라
     */
    
    public boolean changePassword(String userId, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 비즈니스 규칙: 비밀번호는 8자 이상이야 함 등 규칙 검사
            if (newPassword.length() < 8) {
                System.err.println("오류: 비밀번호는 최소 8자 이상이어야 합니다.");
                return false;
            }
            
            // User 객체의 비밀번호 필드 업데이트 (실제로는 여기서 암호화)
            user.setPassword(newPassword);
            
            // 변경된 객체를 저장소에 반영
            return userRepository.update(user);
        }
        return false;
    }
}
