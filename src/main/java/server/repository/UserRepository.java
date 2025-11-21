/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.repository;
import server.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author user
 */
public class UserRepository {
    private static final String USER_FILE_PATH = "data/users.csv"; //user.csv 파일경로 (혹시나 안되면 data/ 빼보셈)
    private static final int ID_INDEX = 0;
    private static final int PW_INDEX = 1;
    private static final int ROLE_INDEX = 2;
    
    public synchronized User findByUsername(String id) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(USER_FILE_PATH));
            String line;
            reader.readLine();
            
            while((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                
                 if(parts.length == 3 && parts[0].equals(id)){ //id 일치하면 User객체 생성 후 반환
                     return new User(parts[0], parts[1], parts[2]);
                 }
            }
        }
        catch(IOException ex){
            System.out.println("CVS 파일찾기 오류");
            ex.printStackTrace();
        }
        
        return null; //사용자를 찾기 못함
    } 
    
    public synchronized List<User> findAll(){
        List<User> userList = new ArrayList<>();
        File file = new File(USER_FILE_PATH);
        
        if(!file.exists()) return userList; // 빈 리스트 반환
        
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            reader.readLine();
            
            while((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                if(parts.length == 3){
                    userList.add(new User(parts[ID_INDEX].trim(), parts[PW_INDEX].trim(), parts[ROLE_INDEX] ));
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return userList;
    }
    
    public synchronized boolean add(User user){
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE_PATH, true)); 
            writer.newLine();
            
            String line = String.format("%s, %s, %s", user.getId(), user.getPassword(), user.getRole());
            writer.write(line);
            return true;
        }
        catch(IOException ex){
            ex.printStackTrace();
            return false;
        }
    }
    
    public synchronized boolean delete(String id){
        List<User> allUsers = findAll();
        
        boolean removed = allUsers.removeIf(u -> u.getId().equals(id));
        
        if(!removed) return false;
        
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE_PATH)); 
            writer.write("ID,Password,Role");
            
            for(User u : allUsers){
                writer.newLine();
                String line = String.format("%s, %s, %s", u.getId(), u.getPassword(), u.getRole());
                writer.write(line);
            }
            return true;
        }
        catch(IOException ex){
            ex.printStackTrace();
            return false;
        }
    }
}


