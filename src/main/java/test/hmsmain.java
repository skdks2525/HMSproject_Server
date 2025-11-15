/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package test;

import repository.UserRepository;
import service.UserService;
import view.LoginView;

import javax.swing.SwingUtilities;

public class hmsmain {
    public static void main(String[] args) {
       // 1. Repository 계층 초기화 (데이터 접근)
        // JSON 파일이 없으면 자동으로 생성/초기화됩니다.
        UserRepository userRepository = new UserRepository();

        // 2. Service 계층 초기화 (비즈니스 로직)
        // Service는 Repository에 의존합니다.
        UserService userService = new UserService(userRepository);

        // 3. (선택 사항) 초기 관리자 계정 생성 및 저장
        // 테스트를 위해 관리자 계정이 없으면 하나 만듭니다.
        // 이 코드는 UserRepositoryTest에서 한번 실행했으므로, 
        // 여기서는 생략하거나 (필요하면) 주석 해제하여 사용합니다.
        /*
        if (userRepository.findById("admin01").isEmpty()) {
            User initialAdmin = new User("admin01", "pass123", UserRole.ADMIN, 
                                         "시스템 관리자", "010-0000-0000", "admin@hotel.com");
            userRepository.save(initialAdmin);
        }
        */

        // 4. ⭐️ View 계층 시작 (GUI 실행) ⭐️
        // Swing 애플리케이션은 반드시 Event Dispatch Thread(EDT)에서 실행되어야 합니다.
        SwingUtilities.invokeLater(() -> {
            // LoginView를 띄울 때 UserService 객체를 전달합니다.
            new LoginView(userService).setVisible(true);
        });
    }
}
