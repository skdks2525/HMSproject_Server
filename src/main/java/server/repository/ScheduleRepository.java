package server.repository;

import server.model.RoomSchedule;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {
    private static final String SCH_FILE_PATH = "data/schedules.csv";

    public synchronized List<RoomSchedule> findAll() {
        List<RoomSchedule> list = new ArrayList<>();
        File file = new File(SCH_FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 6) {
                    // [수정] RoomSchedule 객체 생성
                    list.add(new RoomSchedule(
                        p[0].trim(), p[1].trim(), p[2].trim(), 
                        p[3].trim(), p[4].trim(), p[5].trim()
                    ));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public synchronized boolean add(RoomSchedule schedule) {
        boolean isNewFile = !new File(SCH_FILE_PATH).exists();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCH_FILE_PATH, true))) {
            if (isNewFile) {
                bw.write("ScheduleID,RoomNum,Type,StartDate,EndDate,Note");
                bw.newLine();
            } else {
                bw.newLine();
            }
            bw.write(schedule.toString());
            return true;
        } catch (IOException e) { return false; }
    }

    public synchronized boolean delete(String scheduleId) {
        // [수정] List<RoomSchedule> 사용
        List<RoomSchedule> all = findAll(); 
        if (all.removeIf(s -> s.getScheduleId().equals(scheduleId))) {
            return rewriteFile(all);
        }
        return false;
    }

    // [수정] List<RoomSchedule> 사용
    private boolean rewriteFile(List<RoomSchedule> all) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCH_FILE_PATH))) {
            bw.write("ScheduleID,RoomNum,Type,StartDate,EndDate,Note");
            for (RoomSchedule s : all) {
                bw.newLine();
                bw.write(s.toString());
            }
            return true;
        } catch (IOException e) { return false; }
    }
}