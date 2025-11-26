package server.net;
import server.service.*;
//import cse.oop2.hms_server.src.main.server.service.ReservationService;
import server.model.*;
import java.io.*;
import java.net.*;

/**
 *  각 클라이언트 연결을 개별 스레드에서 처리하는 클래스 (Runnable 인터페이스 구현)
 * @author user
 */
public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private final AuthService authService;
    private final HotelService hotelService;
    private final MenuService menuService;
    //private final Reservation reservationService;
    
    public ClientHandler(Socket socket){
        this.clientSocket = socket;
        this.authService = new AuthService(); // 사용할 서비스 객체 초기화
        this.hotelService = new HotelService();
        this.menuService = new MenuService();
        //this.reservation = new ReservationService();
    }
    
    @Override
    public void run(){
        String request;
        
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
            
            while((request = in.readLine()) != null){
                System.out.println("클라이언트 요청: " + request);
                
                //여기에 핵심코드 작성
                String respone = handleRequest(request); //위임
                out.println(respone); //응답
            }
        }
        
        catch(IOException ex){
            System.out.println("클라이언트 통신 오류");
            ex.printStackTrace();
        }

    }
    
    private String handleRequest(String request){
        try{
            String[] parts = request.split(":", 3);
            String command = parts[0];
            
            switch(command){
                case "LOGIN":
                    if(parts.length == 3){
                        String username = parts[1];
                        String password = parts[2];
                        
                        User user = authService.login(username, password);
                        if(user != null){
                            return "LOGIN_SUCCESS:" + user.getRole(); //로그인 성공    
                        }
                        else{    
                            return "LOGIN_FAIL:Invalid credentials"; // 로그인 실패    
                        }    
                    }    
                    else{    
                        return "ERROR:Invalid LOGIN format"; // 형식 오류
                    }    
                case "CHECKIN":    
                default:    
                    return "ERROR:Unknown command " + command;    
                    
                case "GET_USERS":
                    //서비스에서 모든 유저 가져오기
                    java.util.List<User> users = authService.getAllUsers();
                    StringBuilder sb = new StringBuilder("USER_LIST:");
                    for (User u : users) {
                        sb.append(u.getId()).append(",").append(u.getPassword()).append(",").append(u.getRole()).append(",").append(u.getPhone()).append(",").append(u.getName()).append("/");
                    }
                    return sb.toString();
                case "ADD_USER":
                    // 형식(필수): ADD_USER:id:name:pw:role:phone  => 총 6토큰, 모두 공백불가
                    {
                        String[] addParts = request.split(":");
                        if(addParts.length != 6) return "ADD_FAIL:Format"; 
                        String id = addParts[1];
                        String name = addParts[2];
                        String pw = addParts[3];
                        String role = addParts[4];
                        String phone = addParts[5];
                        if(blank(id) || blank(name) || blank(pw) || blank(role) || blank(phone)) return "ADD_FAIL:FieldRequired";
                        boolean ok = authService.addUser(id, name, pw, role, phone);
                        return ok ? "ADD_SUCCESS" : "ADD_FAIL:DuplicateOrError";
                    }
                
                case "DELETE_USER":
                    // 형식: DELETE_USER:id
                    if(parts.length >= 2) {
                        boolean ok = authService.deleteUser(parts[1]);
                        return ok ? "DELETE_SUCCESS" : "DELETE_FAIL";
                    }
                    return "DELETE_FAIL:Format";
                case "MODIFY_USER":
                    // 형식(필수): MODIFY_USER:id:name:pw:role:phone  => 총 6토큰, 모두 공백뵘8가
                    {
                        String[] mParts = request.split(":");
                        if(mParts.length != 6) return "MODIFY_FAIL:Format";
                        if(blank(mParts[1]) || blank(mParts[2]) || blank(mParts[3]) || blank(mParts[4]) || blank(mParts[5])) return "MODIFY_FAIL:FieldRequired";
                        boolean ok = authService.modifyUser(mParts[1], mParts[2], mParts[3], mParts[4], mParts[5]);
                        return ok ? "MODIFY_SUCCESS" : "MODIFY_FAIL";
                    }
                    
                case "GET_MENUS":
                    //서비스에서 모든 메뉴 가져오기
                    java.util.List<Menu> menus = menuService.getAllMenus();
                    StringBuilder sb2 = new StringBuilder("MENU_LIST:");
                    for (Menu m : menus) {
                        String isAvailableStr = String.valueOf(m.getIsAvailable());// m.getIsAvailable() ? "판매중" : "판매중지"; // boolean 값을 판매중, 판매중지로 보내기
                        sb2.append(m.getMenuId()).append(",").append(m.getName()).append(",").append(m.getPrice()).append(",").append(m.getCategory()).append(",").append(isAvailableStr).append("/");
                    }
                    return sb2.toString();
                case "GET_ROOM_SALES":
                    // 형식: GET_ROOM_SALES:yyyy-MM-dd:yyyy-MM-dd
                    // 요청 형식 검사 후 HotelService에 집계를 위임합니다.
                    // 클라이언트는 시작/종료 날짜(yyyy-MM-dd)를 전달하고,
                    // 서버는 "ROOM_SALES:yyyy-MM-dd=amount,yyyy-MM-dd=amount,..." 형태의 한 줄 응답을 반환합니다.
                    // LinkedHashMap을 사용해 날짜 순서가 유지되므로 클라이언트가 순서대로 그래프를 그릴 수 있습니다.
                    if (parts.length == 3) {
                        String start = parts[1];
                        String end = parts[2];

                        // HotelService에서 날짜별 매출을 계산해서 맵으로 돌려받습니다.
                        java.util.Map<java.time.LocalDate, Integer> sales = hotelService.getRoomSalesByDateRange(start, end);

                        // 응답 문자열을 구성합니다. 각 항목은 date=value(날짜=매출)로 콤마로 구분됩니다.
                        //ex ROOM_SALES:2025-10-01=220000,2025-10-02=440000,...
                        StringBuilder salesSb = new StringBuilder("ROOM_SALES:");
                        boolean first = true;
                        for (java.util.Map.Entry<java.time.LocalDate, Integer> en : sales.entrySet()) {
                            if (!first) salesSb.append(",");
                            salesSb.append(en.getKey().toString()).append("=").append(en.getValue());
                            first = false;
                        }
                        return salesSb.toString();
                    }
                    return "ERROR:Format";
                    
                case "ADD_MENU":
                    // 형식: ADD_MENU:menuId:name:price:category:isavailable
                {
                    String[] menuParts = request.split(":");
                    // 형식 검사: 토큰이 6개인지 확인
                    if (menuParts.length != 6) return "ADD_FAIL:Format";
                    
                    String menuId = menuParts[1];
                    String name = menuParts[2];
                    String priceStr = menuParts[3];
                    String category = menuParts[4];
                    String isavailableStr = menuParts[5];
                    
                    if (blank(menuId) || blank(name) || blank(priceStr) || blank(category) || blank(isavailableStr)) {
                        return "ADD_FAIL:FieldRequired";
                    }
                    
                    // 가격 (price) 숫자 변환 및 오류 처리
                    int price;
                    try {
                        price = Integer.parseInt(priceStr.trim());
                    }
                    catch (NumberFormatException e) {
                        return "ADD_FAIL:InvaildPriceFormat";
                    }
                    
                    // 판매 여부 변환
                    boolean isAvailable = Boolean.parseBoolean(isavailableStr.trim());
                    
                    // Service 호출
                    boolean ok = menuService.AddMenu(menuId, name, price, category, isAvailable);
                    
                    // 결과 반환
                    return ok ? "ADD_SUCCESS" : "ADD_FAIL:DuplicateIdOrError";
                }
                
                case "DELETE_MENU":
                    // 형식: DELETE_MENU:menuId (총 2개 토큰)
                {
                    if (parts.length != 2) return "DELETE_FAIL:Format";
                    String menuId = parts[1];
                    
                    if (blank(menuId)) {
                        return "DELETE_FAIL:FieldRequired";
                    }
                    
                    // Service 호출
                    boolean ok = menuService.deleteMenu(menuId);
                    
                    // 결과 반환
                    return ok ? "DELETE_SUCCESS" : "DELETE_FAIL:NotFound";
                }
                
                case "UPDATE_MENU":
                {
                    String[] menuParts = request.split(":");
                    // 형식 검사: 토큰이 6개인지 확인
                    if (menuParts.length != 6) return "UPDATE_FAIL:Format";
                    
                    String menuId = menuParts[1];
                    String name = menuParts[2];
                    String priceStr = menuParts[3];
                    String category = menuParts[4];
                    String isavailableStr = menuParts[5];
                    
                    if (blank(menuId) || blank(name) || blank(priceStr) || blank(category) || blank(isavailableStr)) {
                        return "UPDATE_FAIL:FieldRequired";
                    }
                    
                    // 가격 (price) 숫자 변환 및 오류 처리
                    int price;
                    try {
                        price = Integer.parseInt(priceStr.trim());
                    }
                    catch (NumberFormatException e) {
                        return "UPDATE_FAIL:InvaildPriceFormat";
                    }
                    
                    // 판매 여부 변환
                    boolean isAvailable = Boolean.parseBoolean(isavailableStr.trim());
                    
                    // Service 호출
                    boolean ok = menuService.updateMenu(menuId, name, price, category, isAvailable);
                    
                    // 결과 반환
                    return ok ? "UPDATE_SUCCESS" : "UPDATE_FAIL:NotFound";
                }
                
                case "GET_ALL_ROOMS":
                    StringBuilder roomSb = new StringBuilder("ROOM_LIST:");
                    for(Room r : hotelService.getAllRooms()){
                        roomSb.append(r.toString()).append("/");
                    }
                    return roomSb.toString();
                    
                case "GET_ROOM":
                    if (parts.length == 2) {
                        Room r = hotelService.getRoom(parts[1]);
                        return (r != null) ? "ROOM_INFO:" + r.toString() : "ERROR:Not Found";
                    }
                    return "ERROR:Format";
                        
                case "GET_RES_BY_NAME":
                    if (parts.length == 2) {
                        StringBuilder resSb = new StringBuilder("RES_LIST:");
                        for (Reservation r : hotelService.getReservationsByName(parts[1])) {
                            resSb.append(r.toString()).append("/");           
                        }
                        return resSb.toString();
                    }    
                    return "ERROR:Format";
                        
                case "GET_AVAILABLE_ROOMS":
                    // 날짜 로직 생략, 전체 방 목록 반환 (추후 구현)
                    StringBuilder availSb = new StringBuilder("ROOM_LIST:");
                    for (Room r : hotelService.getAllRooms()) {
                        availSb.append(r.toString()).append("/");
                    }
                    return availSb.toString();
                    
                case "CHECK_AVAILABILITY":
                    String[] checkParts = request.split(":");
                    if (checkParts.length == 3) {
                        String types = hotelService.getAvailableRoomTypes(checkParts[1], checkParts[2]);
                        return "AVAILABLE_TYPES:" + types;
                    }
                    return "ERROR:Format";
                    
               case "ADD_RESERVATION":
                    // ADD_RESERVATION:타입:이름:입실:퇴실:인원:폰
                    String[] resParts = request.split(":");
                    
                    if (resParts.length == 7) { 
                        // createReservationByType 호출 (자동 배정 로직)
                        String assignedRoom = hotelService.createReservationByType(
                            resParts[1], // Type
                            resParts[2], // Name
                            resParts[3], // In
                            resParts[4], // Out
                            Integer.parseInt(resParts[5]), // GuestNum
                            resParts[6]  // Phone
                        );
                        
                        if (assignedRoom != null) {
                            return "RESERVE_SUCCESS:" + assignedRoom;
                        } else {
                            return "RESERVE_FAIL:No Room Available";
                        }
                    }
                    return "ERROR:Format (Expected 7 parts)";
                    
                case "DELETE_RESERVATION":
                    if (parts.length == 2) {
                        boolean ok = hotelService.cancelReservation(parts[1]);
                        return ok ? "DELETE_SUCCESS" : "DELETE_FAIL";
                    }
                    return "ERROR:Format";
            }
        }

        catch(Exception ex){
                ex.printStackTrace();
                return "ERROR:Internal server error: " + ex.getMessage();
        }
    }

    // 공백/널 체크 유틸
    private boolean blank(String s){
        return s == null || s.trim().isEmpty();
    }
}
