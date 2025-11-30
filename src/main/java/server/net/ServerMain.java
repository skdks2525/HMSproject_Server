package server.net;
import java.io.*;
import java.net.*;
import java.util.Properties;
import server.service.*;
/**
 *  HMS서버 메인 클래스
 * @author user
 */
public class ServerMain {

    // private static final int PORT = 5000; // TCP서버 포트번호
    
    public static void main(String[] args) {
        System.out.println("HMS 서버 시작");

        // config.properties에서 포트 읽기
        int port = 5000;
        try (InputStream input = ServerMain.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String portStr = prop.getProperty("server.port");
                if (portStr != null) {
                    port = Integer.parseInt(portStr);
                }
            }
        } catch (Exception e) {
            System.out.println("[경고] config.properties에서 포트 정보를 읽지 못했습니다. 기본값 5000 사용");
        }

        // 서비스 객체들을 서버 시작 시점에 '단 한 번'만 생성
        AuthService authService = new AuthService();
        HotelService hotelService = new HotelService();
        MenuService menuService = new MenuService();
        MenuOrderService menuOrderService = new MenuOrderService();
        ReportService reportService = new ReportService(hotelService.getReservationRepository(), hotelService.getRoomRepository(), menuOrderService);

        try{
            ServerSocket serverSocket = new ServerSocket(port);
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 접속");

                ClientHandler handler = new ClientHandler(
                    clientSocket,
                    authService,
                    hotelService,
                    menuService,
                    menuOrderService,
                    reportService
                );

                Thread t = new Thread(handler);
                t.start();
            }
        }

        catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
