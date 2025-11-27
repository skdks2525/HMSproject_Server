package server.repository;

import server.model.Menu;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuRepository {
    
    private static final String MENU_FILE_PATH = "data/menus.csv";
    
    public MenuRepository() {
        File file = new File(MENU_FILE_PATH);
        if(!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                if (file.createNewFile()) {
                    saveAll(new ArrayList<>());
                }
            } catch (IOException e) {
                System.err.println("파일 생성 중 오류: " + e.getMessage());
            }
        }
    }
    
    public List<Menu> findAll() {
        List<Menu> menus = new ArrayList<>();
        File file = new File(MENU_FILE_PATH);
        
        if (!file.exists()) return menus;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            reader.readLine(); // 헤더 스킵
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        String menuId = parts[0].trim();
                        String name = parts[1].trim();
                        int price = Integer.parseInt(parts[2].trim());
                        String category = parts[3].trim();
                        String availabilityStr = parts[4].trim();
                        int stock = Integer.parseInt(parts[5].trim());
                        
                        // [수정] String -> boolean 변환
                        // (CSV에 "true"로 저장되어 있으면 true, 아니면 false 반환)
                        boolean isAvailable = Boolean.parseBoolean(availabilityStr);
                        
                        // 변환된 boolean 값을 생성자에 전달
                        menus.add(new Menu(menuId, name, price, category, isAvailable, stock));
                        
                    } catch (NumberFormatException e) {
                        System.err.println("데이터 변환 오류: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("파일 읽기 오류: " + e.getMessage());
        }
        return menus;
    }
    
    public void saveAll(List<Menu> menus) {
        File file = new File(MENU_FILE_PATH);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"))) {

            writer.write("menuId,name,price,category,IsAvailable,Stock");
            writer.newLine();

            for (Menu menu : menus) {
                // CSV 포맷으로 저장 (%b는 boolean을 "true"/"false"로 저장)
                String csvLine = String.format("%s,%s,%d,%s,%b,%d",
                        menu.getMenuId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getCategory(),
                        menu.getIsAvailable(), // boolean 값
                        menu.getStock());
                
                writer.write(csvLine);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("파일 쓰기 오류: " + e.getMessage());
        }
    }
    
    public boolean save(Menu newMenu) {
        List<Menu> menus = findAll();
        if (menus.stream().anyMatch(u -> u.getMenuId().equals(newMenu.getMenuId()))) {
            return false; // ID 중복
        }
        menus.add(newMenu);
        saveAll(menus);
        return true;
    }
    
    public Optional<Menu> findById(String menuId) {
        return findAll().stream()
                .filter(menu -> menu.getMenuId().equals(menuId))
                .findFirst();
    }
    
    public boolean update(Menu updatedMenu) {
        List<Menu> menus = findAll();
        for (int i=0; i<menus.size(); i++) {
            if (menus.get(i).getMenuId().equals(updatedMenu.getMenuId())) {
                menus.set(i, updatedMenu);
                saveAll(menus);
                return true;
            }
        }
        return false;
    }
    
    public boolean delete(String menuId) {
        List<Menu> menus = findAll();
        if (menus.removeIf(menu -> menu.getMenuId().equals(menuId))) {
            saveAll(menus);
            return true;
        }
        return false;
    }
}