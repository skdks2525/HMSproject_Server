/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package model;

/**
 *
 * @author infin
 */
public enum UserRole {
    ADMIN("관리자"), CSR("서비스 담당자"), CUSTOMER("고객");
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
