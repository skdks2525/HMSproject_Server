package server.service;

import server.model.Room;
import server.model.Reservation;
import server.repository.RoomRepository;
import server.repository.ReservationRepository;
import java.util.*;
import java.util.stream.Collectors;

public class HotelService {
    private final RoomRepository roomRepo = new RoomRepository();
    private final ReservationRepository resRepo = new ReservationRepository();

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    public Room getRoom(String roomNum) {
        return roomRepo.findByNumber(roomNum);
    }

    public List<Reservation> getReservationsByName(String name) {
        return resRepo.findAll().stream()
                .filter(r -> r.getGuestName().equals(name))
                .collect(Collectors.toList());
    }

    // (심화) 날짜 중복 체크 로직이 들어가야 하지만 일단 전체 룸 반환 (임시)
    public List<Room> getAvailableRooms(String inDate, String outDate) {
        return roomRepo.findAll(); 
    }

    public boolean createReservation(String roomNum, String name, String in, String out) {
        // 실제로는 여기서 방이 비었는지 확인해야 함
        return resRepo.add(roomNum, name, in, out) != null;
    }

    public boolean cancelReservation(String resId) {
        return resRepo.delete(resId);
    }
}