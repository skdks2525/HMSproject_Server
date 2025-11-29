package server.model;

/** 사용자 정보 담아두는 클래스
 * users.scv 파일의 사용자 1명씩 담아 둘 객체
 * @author user
 */
public class User {
    private String id;
    private String name;
    private String password;
    private String role;
    private String phone; // 휴대전화 번호

    public User(String id, String password, String role) {
        this(id, "", password, role, "");
    }

    public User(String id, String password, String role, String phone) {
        this(id, "", password, role, phone);
    }

    public User(String id, String name, String password, String role, String phone) {
        this.id = id;
        this.name = name == null ? "" : name.trim();
        this.password = password;
        this.role = role;
        this.phone = phone == null ? "" : phone.trim();
    }

    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getPassword(){
        return password;
    }
    public String getRole(){
        return role;
    }
    public String getPhone(){
        return phone;
    }

    public void setName(String name){
        this.name = name == null ? "" : name.trim();
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setRole(String role){
        this.role = role;
    }
    public void setPhone(String phone){
        this.phone = phone == null ? "" : phone.trim();
    }
}
