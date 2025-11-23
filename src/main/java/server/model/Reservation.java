package server.model;

/**
 *
 * @author user
 */
public class Reservation {
    private String reservationId;
    private String roomNumber;
    private String guestName;
    private String checkInDate;
    private String checkOutDate;
    private int guestNum;
    private String phoneNumber;

    public Reservation(String reservationId, String roomNumber, String guestName, String checkInDate, String checkOutDate, int guestNum, String phoneNumber) {
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestNum = guestNum;
        this.phoneNumber = phoneNumber;
    }

    public String getReservationId() { return reservationId; }
    public String getRoomNumber() { return roomNumber; }
    public String getGuestName() { return guestName; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public int getGuestNum() { return guestNum; }
    public String getPhoneNumber() { return phoneNumber; }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%d,%s", reservationId, roomNumber, guestName, checkInDate, checkOutDate, guestNum, phoneNumber);
    }
}
