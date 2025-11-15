/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

import com.google.gson.Gson; // Java 객체를 JSON 문자열로, JSON 문자열을 Java 객체로 변환해주는 역할
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.User;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // NullPointerException 방지를 위해 사용
/**
 *
 * @author infin
 */
public class UserRepository {
    // 데이터 파일 경로 정의 (프로젝트 루트 폴더 기준)
    private static final String FILE_PATH = "data/users.json";
    
    // JSON 직렬화/역직렬화를 위한 Gson 객체 생성
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // 파일 입출력 시, 파일 경로가 유효한지 확인하고 파일 또는 디렉토리를 생성하는 초기화 로직
    public UserRepository() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                // 상위 디렉토리가 없다면 생성
                file.getParentFile().mkdirs();
                // 빈 JSON 파일 생성
                if (file.createNewFile()) {
                    saveAll(new ArrayList<>());
                    }
                }
                catch (IOException e) {
                System.err.println("UserRepository 초기화 중 오류 발생: " + e.getMessage());
            }
        }
    }
    /**
     * 파일에서 모든 사용자 데이터를 읽어와 List<User> 객체로 반환
     * @return 파일이 비어 있거나 파싱 오류 발생 시 빈 리스트를 반환(Read: All)
     */
    public List<User> findAll() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            // JSON 파일을 List<User> 타입으로 변환하기 위한 TypeToken 정의
            Type listType = new TypeToken<ArrayList<User>>() {}.getType();
            // Gson을 사용하여 JSON을 객체 리스트로 역직렬화
            List<User> users = gson.fromJson(reader, listType);
            // 파일이 비어 있어 null이 반환될 경우ㅡ 빈 리스트로 처리
            return users != null ? users : new ArrayList<>();
        }
        catch (FileNotFoundException e) {
            // 파일이 없다면 초기화 로직이 제대로 작동하지 않은 것이므로 빈 리스트 반환
            return new ArrayList<>();
        }
        catch (IOException e) {
            System.err.println("사용자 데이터 읽기 중 오류 발생: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    /**
     * 현재 메모리에 있는 전체 사용자 리스트를 JSON 파일에 덮어씌움 (Write: All)
     */
    public void saveAll(List<User> users) {
            try (Writer writer = new FileWriter(FILE_PATH)) {
                // Gson을 사용하여 List<User>를 JSON 문자열로 직렬화하여 파일에 저장
                gson.toJson(users, writer);
            }
            catch (IOException e) {
                System.err.println("사용자 데이터 쓰기 중 오류 발생: " + e.getMessage());
            }
        }
    
    // --- SFR-601 (CRUD) 구현에 필요한 메서드 ---
    
    /**
     * 새로운 사용자를 등록 (Create)
     * @param newUser 를 저장할 User 객체
     * @return 성공적으로 저장되었는지 여부
     */
    public boolean save(User newUser) {
        List<User> users = findAll();
        // ID 중복 검사
        if (users.stream().anyMatch(u -> u.getUserId().equals(newUser.getUserId()))) {
            System.out.println("오류: 이미 존재하는 사용자 ID입니다.");
            return false;
        }
        users.add(newUser);
        saveAll(users);
        return true;
    }
    /**
     * 특정 ID를 가진 사용자 탐색 (Read: By ID)
     * Optional을 사용하여 null 대신 값이 없을 수 있음을 명확히 표현
     * @param userId 찾고자 하는 사용자 ID
     * @return User 객체를 담은 Optional
     */
    public Optional<User> findById(String userId) {
        return findAll().stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst();
    }
    /**
     * 기존 사용자의 정보를 업데이트합니다. (Update)
     * @param updatedUser 업데이트할 정보가 담긴 User 객체 (ID는 기존과 동일해야 함)
     * @return 성공적으로 업데이트되었는지 여부
     */
    public boolean update(User updatedUser) {
        List<User> users = findAll();
        // ID를 기준으로 기존 사용자의 인덱스 탐색
        int index = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(updatedUser.getUserId())) {
                index = i;
                break;
            }
        }
        
        if (index != -1) {
            // 기존 객체를 업데이트된 객체로 대체
            users.set(index, updatedUser); 
            saveAll(users);
            return true;
        }
        return false; // 해당 ID의 사용자를 찾지 못함
    }

    /**
     * 특정 ID를 가진 사용자를 삭제합니다. (Delete)
     * @param userId 삭제할 사용자 ID
     * @return 성공적으로 삭제되었는지 여부
     */
    public boolean delete(String userId) {
        List<User> users = findAll();
        boolean removed = users.removeIf(user -> user.getUserId().equals(userId));
        
        if (removed) {
            saveAll(users);
        }
        return removed;
    }
    }
