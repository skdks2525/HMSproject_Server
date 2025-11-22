package server.repository;
import server.model.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author user
 */
public class RoomRepository {
    private static final String ROOM_FILE_PATH = "data/rooms.csv";
    
        /** 모든 사용자 목록 조회 */
    public synchronized List<Room> findAll(){
        List<Room> RoomList = new ArrayList<>();
        File file = new File(ROOM_FILE_PATH);
        if(!file.exists()) 
            return RoomList; // 빈 리스트 반환
        
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            reader.readLine();
            
            while((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                if(parts.length >= 4){
                    RoomList.add(new Room(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return RoomList;
    }
        public Room findByNumber(String roomNum){
            return findAll().stream().filter(r -> r.getRoomNumber().equals(roomNum)).findFirst().orElse(null);
        }
    }
    

