/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import model.User;
import model.UserRole;
import service.UserService;
import view.LoginView;
import view.MainView;

import javax.swing.JOptionPane;
import java.util.Optional;

public class LoginController {
    
    // 비즈니스 로직을 처리하는 UserService에 의존
    private final UserService userService;
    // 현재 로그인 창 객체를 받아, 로그인 성공 시 닫는 데 사용
    private final LoginView loginView;
    
    public LoginController(UserService userService, LoginView loginView) {
        this.userService = userService;
        this.loginView = loginView;
    }
    
    /**
     * SFR-605의 핵심 로직: 사용자 인증 및 권한 확인 후 메인 화면 전환
     * @param userId 사용자가 입력한 ID
     * @param password 사용자가 입력한 비밀번호
     */
    public void attemptLogin(String userId, String password) {
        // 입력 유효성 검사 (공백 여부 등)
        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(loginView, "ID와 비밀번호를 모두 입력해주세요.", "로그인 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // UserService에 인증 위임
        Optional<User> loggedInUserOptional = userService.login(userId, password);
        
        if (loggedInUserOptional.isPresent()) {
            // 로그인 성공 처리
            User user = loggedInUserOptional.get();
            JOptionPane.showMessageDialog(loginView, user.getName() + "님, " + user.getRole().getDescription() + " 권한으로 로그인했습니다.",
                     "로그인 성공", JOptionPane.INFORMATION_MESSAGE);
             // 권한에 따라 메인 애플리케이션 화면 열기
            openMainApplication(user);
        
            // 현재 로그인 창 받기
            loginView.dispose();
        }
        else {
            // 로그인 실패 처리
            JOptionPane.showMessageDialog(loginView, "사용자 ID 또는 비밀번호가 올바르지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
        /**
         * 로그인된 사용자의 권한을 MainView에 전달하여 메뉴 설정 위임
         * @param user 로그인 성공한 User 객체
         */
        private void openMainApplication(User user) {
            // 고객의 경우 직원과는 다른 별도의 화면을 띄울 수 있도록 분기 처리
            if (user.getRole() == UserRole.CUSTOMER) {
                // new CustomerMainView(user).setVisible(true); // 고객 전용 뷰 호출
                JOptionPane.showMessageDialog(loginView, "고객 전용 기능은 개발 중입니다.", "안내", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                // ADMIN 또는 CSR은 MainView를 사용
                // MainView 생성자에 User 객체를 전달하여 SFR-605 권한 설정을 위임
                new MainView(user, userService).setVisible(true);
            }
        }
}