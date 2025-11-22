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
        try{
            BufferedReader reader = new BufferedReader(new FileReader(RES_FILE_PATH));
            String line;    
            reader.readLine();
            
            while((line = reader.readLine()) != null){
                String[] parts = line.split(",");    
                if(parts.length >= 5){
                    list.add(new Reservation(parts[0], parts[1], parts[2], parts[3], parts[4]));    
                }    
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }   
        return list;
    }
    
    public synchronized String add(String roomNum, String name, String inDate, String outDate){
        String resId = "R-" + (System.currentTimeMillis() % 10000); // 간단한 ID 생성
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RES_FILE_PATH, true))) {
            bw.newLine();
            bw.write(resId + "," + roomNum + "," + name + "," + inDate + "," + outDate);
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
            bw.write("ResID,RoomNum,GuestName,CheckIn,CheckOut");
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
    
