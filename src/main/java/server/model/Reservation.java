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
    private String paymentInfo;
    private String createdAt;

    public Reservation(String reservationId, String roomNumber, String guestName, String checkInDate, String checkOutDate, int guestNum, String phoneNumber, String paymentInfo, String createdAt) {
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestNum = guestNum;
        this.phoneNumber = phoneNumber;
        this.paymentInfo = (paymentInfo == null) ? "Unpaid" : paymentInfo;
        this.createdAt = createdAt;
    }

    public String getReservationId() { return reservationId; }
    public String getRoomNumber() { return roomNumber; }
    public String getGuestName() { return guestName; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public int getGuestNum() { return guestNum; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPaymentInfo() { return paymentInfo; }
    public String getCreatedAt() { return createdAt; }

    //setter
    public void setPaymentInfo(String paymentInfo) { this.paymentInfo = paymentInfo; }
    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%d,%s, %s, %s", reservationId, roomNumber, guestName, checkInDate, checkOutDate, guestNum, phoneNumber, paymentInfo, createdAt);
    }
}
