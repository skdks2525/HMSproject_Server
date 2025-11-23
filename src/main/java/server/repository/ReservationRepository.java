package server.repository;
import server.model.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author user
 */
public class ReservationRepository {
    private static final String RES_FILE_PATH = "data/reservation.csv";
    
    public synchronized List<Reservation> findAll(){
        List<Reservation> list = new ArrayList<>();
        File file = new File(RES_FILE_PATH);
        if(!file.exists()) return list;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;    
            reader.readLine();
            
            while((line = reader.readLine()) != null){
                String[] parts = line.split(",");    
                if (parts.length >= 9) {
                    list.add(new Reservation(
                        parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
                            parts[4].trim(),Integer.parseInt(parts[5].trim()), parts[6].trim(),
                            parts[7].trim(),parts[8].trim()));
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }   
        return list;
    }
    
    public synchronized String add(String roomNum, String name, String inDate, String outDate, int guestNum, String phone, String createdAt){
        String resId = "R-" + (System.currentTimeMillis() % 10000); // 간단한 ID 생성
        String payment = "Unpaid";
        boolean isNewFile = !new File(RES_FILE_PATH).exists();
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RES_FILE_PATH, true))) {
            if (isNewFile) {
                bw.write("ResID,RoomNum,GuestName,CheckIn,CheckOut,Guests,Phone,Payment,CreatedAt");
                bw.newLine();
            } else {
                bw.newLine();
            }
            // 7개 필드 저장
            String line = String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s", 
                    resId, roomNum, name, inDate, outDate, guestNum, phone, payment, createdAt);
            bw.write(line);
            return resId;
        }
        catch (IOException ex){ 
            return null; 
        }
    }
    
    private boolean rewriteFile(List<Reservation> all) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RES_FILE_PATH))) {
            bw.write("ResID,RoomNum,GuestName,CheckIn,CheckOut,Guests,Phone,Payment,CreatedAt");
            for (Reservation r : all) {
                bw.newLine();
                bw.write(r.toString());
            }
            return true;
        } catch (IOException e) { return false; }
    }
    
    public synchronized boolean updatePayment(String resId, String paymentInfo) {
        List<Reservation> all = findAll();
        for (Reservation r : all) {
            if (r.getReservationId().equals(resId)) {
                r.setPaymentInfo(paymentInfo);
                return rewriteFile(all);
            }
        }
        return false;
    }
    
    public synchronized boolean delete(String resId) {
        List<Reservation> all = findAll();

        // 삭제 대상이 있으면 제거하고 파일 다시 쓰기
        if (all.removeIf(r -> r.getReservationId().equals(resId))) {
            return rewriteFile(all); // [수정] 중복 코드 대신 rewriteFile 호출
        }
        return false;
    }
}
    
