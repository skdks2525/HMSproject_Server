/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private String userId; // 사용자 ID
    private String password; // 비밀번호
    private String name; // 이름
    private String phoneNumber; // 전화번호
    private String email; // 이메일
    private UserRole role;
    
    public User() { }
    public User(String userId, String password, String name, String phoneNumber, String email, UserRole role){
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
    }    
    public String getUserId() {
        return userId;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    // JSON 저장소에서 특정 사용자를 찾거나 삭제하기 위해 ID 기반 동등성 비교 정의
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId); // ID가 같으면 같은 User로 간주
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
    
    // 디버깅 및 로깅을 위해 사용
    @Override
    public String toString() {
        return "User{" + "userId='" + userId + '\'' + 
                ", role=" + role +
                ", name='" + name + '\'' +
                '}';
    }
}

