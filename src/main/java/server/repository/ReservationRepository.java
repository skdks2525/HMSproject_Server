package server.repository;
import server.model.Reservation;
import java.io.*;
import java.util.*;
/**
 *
 * @author user
 */
public class ReservationRepository {
    private static final String RES_FILE_PATH = "data/reservaton.csv";
    
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
                if (parts.length >= 7) {
                    list.add(new Reservation(parts[0], parts[1], parts[2], parts[3], parts[4], Integer.parseInt(parts[5]), parts[6]));
                }  
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }   
        return list;
    }
    
    public synchronized String add(String roomNum, String name, String inDate, String outDate, int guestNum, String phone){
        String resId = "R-" + (System.currentTimeMillis() % 10000); // 간단한 ID 생성
        boolean isNewFile = !new File(RES_FILE_PATH).exists();
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RES_FILE_PATH, true))) {
            if (isNewFile) {
                bw.write("ResID,RoomNum,GuestName,CheckIn,CheckOut,Guests,Phone");
                bw.newLine();
            } else {
                bw.newLine();
            }
            // 7개 필드 저장
            String line = String.format("%s,%s,%s,%s,%s,%d,%s", 
                    resId, roomNum, name, inDate, outDate, guestNum, phone);
            bw.write(line);
            return resId;
        }
        catch (IOException ex){ 
            return null; 
        }
    }
    
    public synchronized boolean delete(String resId) {
        List<Reservation> all = findAll();
        boolean removed = all.removeIf(r -> r.getReservationId().equals(resId));
        if (!removed) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RES_FILE_PATH))) {
            bw.write("ResID,RoomNum,GuestName,CheckIn,CheckOut,Guests,Phone");
            for (Reservation r : all) {
                bw.newLine();
                bw.write(r.toString());
            }
            return true;
        } 
        catch (IOException ex) { 
            return false;
        }
    }
}
    
