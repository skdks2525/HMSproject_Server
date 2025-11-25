/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.repository;

import server.model.Menu;
// import model.ItemCategory;

/*.
import com.google.gson.Gson; // Java 객체를 JSON 문자열로, JSON 문자열을 Java 객체로 변환해주는 역할
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken; // 제네릭 타입 정보를 런타임까지 보존하기 위하다. List<String>, <MyObject> 등 */

// import java.lang.reflect.Type; // 클래스 필드, 메서드의 구체적인 타입이나 정보를 알지 못하더라도 해당 클래스의 필드, 메서드, 타입들에 접근할 수 있도록 해주는 API
import java.io.*; // 파일 입출력을 담당 (영속적 데이터(json)와 상호작용하기 위한)
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // NullPointerException 방지 (null값을 참조해도 NPE가 발생하지 않도록)

import java.nio.charset.StandardCharsets;

public class MenuRepository {
    
    // 파일 경로 지정
    private static final String MENU_FILE_PATH = "data/menus.csv";
    
    /**
    // 예쁘게 출력하는(=setPrettyPrinting()) Gson 객체 생성
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); */
    
    public MenuRepository() {
        File file = new File(MENU_FILE_PATH); // file 객체 생성 (파일 경로에 대한?)
        // =파일이 존재하지 않다면 (파일 존재 여부 검사)
        if(!file.exists()) {
            // 예외 처리 구문 (try를 if로 생각하면 편함)
            try {
                // 부모 디렉토리를 file 객체로 생성 후 반환(=getParentFile())
                // 부모 디렉토리가 존재하지 않을 경우, 해당 부모 디렉토리를 포함한 상위 디렉토리들을 재귀적으로 모두 생성
                file.getParentFile().mkdirs();
                if (file.createNewFile()) {
                    // 빈 json 파일 생성
                    saveAll(new ArrayList<>());
                }
            }
            // 예외 발생 시 처리하는 구문
            catch (IOException e) {
                System.err.println("파일 생성 중 오류");
            }
        }
    }
    
    public List<Menu> findAll() {
        List<Menu> menus = new ArrayList<>();
        File file = new File(MENU_FILE_PATH);
        
        
        // 한글을 못 읽어서 넣음
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            
            // 첫 번째 라인 (헤더) 스킵
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                
                if (parts.length >= 5) {
                    try {
                        String menuId = parts[0].trim();
                        String name = parts[1].trim();
                        int price = Integer.parseInt(parts[2].trim());
                        String category = parts[3].trim();
                        String availabilityStr = parts[4].trim();
                        
                        boolean isAvailable = availabilityStr.equals("판매중");
                        menus.add(new Menu(menuId, name, price, category, isAvailable));
                    }
                    
                    catch (NumberFormatException e) {
                        System.err.println("가격 변환 오류 :" + e.getMessage());
                    }
                }
            }
        }
        
        catch (FileNotFoundException e) {
            // 파일이 없을 경우 빈 리스트 반환
            return new ArrayList<>();
        }
        catch (IOException e) {
            System.err.println("메뉴 CSV 파일 읽기 오류 :");
        }
        return menus;
    }
    
    /**
     * 현재 메모리에 있는 전체 사용자 리스트를 json 파일에 덮어쓰기 (아님)
     * @param menus 메뉴 리스트
     */
    public void saveAll (List<Menu> menus) {
        File file = new File(MENU_FILE_PATH);
        
        // 마찬가지 (한글을 못 씀)
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            
            // CSV 헤더 작성
            writer.write("menuId,name,price,category,IsAvailable");
            writer.newLine();
            
            for (Menu menu : menus) {
                String isavailableStr = menu.getIsAvailable() ? "판매중" : "판매중지";
                String csvLine = String.format("%s,%s,%s,%s,%s",
                        menu.getMenuId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getCategory(),
                        isavailableStr);
                writer.write(csvLine);
                writer.newLine();
            }
        }
        catch (IOException e) {
            System.err.println("메뉴 CSV 파일 쓰기 오류 :" + e.getMessage());
        }
        /**
        // json 파일에서 문자 쓰는 writer 객체 생성?
        try (Writer writer = new FileWriter(FILE_PATH)) {
           // gson 사용하여 menus (List<Menu>)를 json 문자열로 직렬화하여 파일에 저장
           gson.toJson(menus, writer); 
        }
        catch (IOException e) {
            System.err.println("데이터 쓰는데 오류 나." + e.getMessage());
        } */
    }
    
    // SFR-503 메서드
    /**
     * @param newMenu 새로운 메뉴
     * @return 저장되었는지 여부
     */
    public boolean save(Menu newMenu) {
        // findAll 메서드를 호출해서 반환된 모든 Menu 목록을 menus 리스트에 저장
        List<Menu> menus = findAll();
        // 메뉴 ID 중복 검사
        if (menus.stream().anyMatch(u -> u.getMenuId().equals(newMenu.getMenuId()))) {
            System.out.println("오류: 이미 존재하는 메뉴 ID입니다.");
            return false;
        }
        
        // 새 메뉴 추가
        menus.add(newMenu);
        
        saveAll(menus);
        return true;
    }
    
    /**
     * 특정 ID를 가진 메뉴 탐색 (메뉴 정보를 수정할 때 필요)
     * Optional을 사용하여 null 대신 값이 없을 수 있음을 명확히 표현
     * @param menuId 찾고자 하는 메뉴 ID
     * @return Menu 객체를 담은 Optional
     */
    // userId라는 문자열을 받아서 그 ID를 가진 Menu를 찾아서 Optional로 반환하는 메서드
    public Optional<Menu> findById(String menuId) {
        // 전체 사용자 목록을 List로 받아온 다음, 그것을 Stream 형태로 변환
        return findAll().stream()
        // 전체 사용자 중에서 menu.getMenuId()가 userId와 같은 Menu만 남기다.
        .filter(menu -> menu.getMenuId().equals(menuId))
        // 필터링된 사용자 중 첫 번째 요소를 Optional 형태로 반환
        .findFirst();
    }
    
    /**
     * 기존 메뉴의 정보 업데이트
     * @param updatedMenu 업데이트할 정보가 담긴 Menu 객체
     * @return 업데이트되었는지 여부
     */
    public boolean update(Menu updatedMenu) {
        // Menu 리스트를 탐색하는 menus 객체
        List<Menu> menus = findAll();
        
        // ID를 기준으로 기존 메뉴의 인덱스 탐색
        int index = -1;
        
        // menus 리스트의 크기만큼 i 증가
        for (int i=0; i<menus.size(); i++) {
            // menus 리스트의 i번째 요소를 꺼내서, 그 요소의 ID가 updatedMenu의 ID와 같다면
            // 요약하자면 같은 메뉴를 찾았다면
            if (menus.get(i).getMenuId().equals(updatedMenu.getMenuId())) {
                index = i;
                break; // 더 이상 반복할 필요 없으니 break
            }
        }
        
        // 정보 업데이트하는 구문
        if (index != -1) {
            // menus 리스트에 업데이트된 메뉴 정보를 넣음
            menus.set(index, updatedMenu);
            // 저장
            saveAll(menus);
            return true;
        }
        return false;
    }
    
    /**
     * 특정 ID를 가진 메뉴 삭제
     * @param menuId 삭제할 메뉴 ID
     * @return 삭제되었는지 여부
     */
    public boolean delete(String menuId) {
        List<Menu> menus = findAll();
        boolean removed = menus.removeIf(menu -> menu.getMenuId().equals(menuId));
        
        if (removed) {
            saveAll(menus);
        }
        return removed;
    }
}
