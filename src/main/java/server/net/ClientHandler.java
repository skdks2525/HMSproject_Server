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
    //private final Reservation reservationService;
    
    public ClientHandler(Socket socket){
        this.clientSocket = socket;
        this.authService = new AuthService(); // 사용할 서비스 객체 초기화
        this.hotelService = new HotelService();
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
                        sb.append(u.getId()).append(",").append(u.getRole()).append("/");
                    }
                    return sb.toString();
                
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
                    
                case "ADD_RESERVATION":
                    if (parts.length == 5) { // ADD_RESERVATION:방:이름:입실:퇴실
                        boolean ok = hotelService.createReservation(parts[1], parts[2], parts[3], parts[4]);
                        return ok ? "RESERVE_SUCCESS" : "RESERVE_FAIL";
                    }
                    return "ERROR:Format";
                    
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
}
