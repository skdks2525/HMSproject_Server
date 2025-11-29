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
                if(parts.length >= 5){
                    RoomList.add(new Room(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim()), parts[4].trim()));
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
        
        public synchronized boolean add(Room room) {
        // 이미 존재하는 방 번호인지 확인
        if (findByNumber(room.getRoomNumber()) != null) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ROOM_FILE_PATH, true))) {
            bw.newLine();
            String line = String.format("%s,%s,%d,%d,%s", 
                    room.getRoomNumber(), room.getType(), room.getPrice(), room.getCapacity(), room.getDescription());
            bw.write(line);
            return true;
        } catch (IOException e) { return false; }
    }

    public synchronized boolean update(Room newRoom) {
        List<Room> all = findAll();
        boolean found = false;
        
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getRoomNumber().equals(newRoom.getRoomNumber())) {
                all.set(i, newRoom); // 정보 교체
                found = true;
                break;
            }
        }
        if (!found) return false;

        return rewriteFile(all); // 파일 덮어쓰기
    }

    public synchronized boolean delete(String roomNum) {
        List<Room> all = findAll();
        boolean removed = all.removeIf(r -> r.getRoomNumber().equals(roomNum));
        if (!removed) return false;

        return rewriteFile(all);
    }

    private boolean rewriteFile(List<Room> rooms) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ROOM_FILE_PATH))) {
            bw.write("RoomNum,Type,Price,Capacity,Description"); // 헤더
            for (Room r : rooms) {
                bw.newLine();
                String line = String.format("%s,%s,%d,%d,%s", 
                        r.getRoomNumber(), r.getType(), r.getPrice(), r.getCapacity(), r.getDescription());
                bw.write(line);
            }
            return true;
        } catch (IOException e) { return false; }
    }
    }
    

