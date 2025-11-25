/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.service;

import server.model.Menu;
import server.repository.MenuRepository;

import java.util.List;
import java.util.Optional; // NullPointerException 방지 (null값을 참조해도 NPE가 발생하지 않도록)

public class MenuService {
    // MenuRepository를 주입받아 데이터에 접근
    private final MenuRepository menuRepository;
    
    public MenuService() {
        this.menuRepository = new MenuRepository(); // menuservice 생성과 동시에 리포지토리 객체 생성
    }
    
    /**
     * SFR-503 식음료 메뉴 등록
     * @param menuId 메뉴 ID
     * @param name 메뉴 이름
     * @param price 메뉴 가격
     * @param category 메뉴의 종류
     * @param isAvailable 판매 여부
     * @return 등록되었는가?
     */
    public boolean AddMenu(String menuId, String name, int price, String category, boolean isAvailable) {
        // ID, 이름, 가격을 가진 newMenu 객체 생성
        Menu newMenu = new Menu(menuId, name, price, category, isAvailable);
        
        // 판매 여부 선택
        newMenu.setIsAvailable(isAvailable);
        
        // 리포지토리의 save 반환(호출)
        return menuRepository.save(newMenu);
    }
    
    /**
     * SFR-503 식음료 메뉴 수정
     * @param menuId 메뉴 ID
     * @param name 메뉴 이름
     * @param price 메뉴 가격
     * @param category 메뉴 종류
     * @param isAvailable 판매 여부
     * @return 업데이트되었는가?
     */
    
    public boolean updateMenu(String menuId, String name, int price, String category, boolean isAvailable) {
        Optional<Menu> menuOptional = menuRepository.findById(menuId);
        
        // Optional 안에 값이 있다면(=Menu 객체)
        if (menuOptional.isPresent()) {
            // Optional에 존재하는 Menu 객체를 가진 existingMenu 변수 선언
            Menu updatingMenu = menuOptional.get();
            
            // setter 메서드 호출
            updatingMenu.setName(name);
            updatingMenu.setPrice(price);
            updatingMenu.setCategory(category);
            updatingMenu.setIsAvailable(isAvailable);
            
            // update 메서드는 전달받은 객체를 updatedMenu라는 이름으로 받음
            // 이름, 가격 등 필드들은 이미 변경된 상태
            menuRepository.update(updatingMenu);
            return true;
        }
        return false;
    }
    
    /**
     * 메뉴 삭제
     * @param menuId 삭제할 메뉴 ID
     * @return 삭제되었는가?
     */
    public boolean deleteMenu(String menuId) {
        // Menu Optional 리스트 중 삭제할 menuId 탐색
        Optional<Menu> menuToDelete = menuRepository.findById(menuId);
        
        if (menuToDelete.isPresent()) {
            // 리포지토리에서 delete 메서드 반환(호출)
            return menuRepository.delete(menuId);
        }
        return false; // 메뉴를 찾지 못함
    }
    
    /**
     * 메뉴 목록 조회
     * @return 모든 메뉴 List
     */
    
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }
}


