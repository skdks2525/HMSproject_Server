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
    private String reservationStatus;
    private String createdAt;
    private String customerRequest;

    public Reservation(String reservationId, String roomNumber, String guestName, String checkInDate, String checkOutDate, int guestNum, String phoneNumber, String reservationStatus, String createdAt, String customerRequsest) {
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestNum = guestNum;
        this.phoneNumber = phoneNumber;
        this.reservationStatus = (reservationStatus == null) ? "Unpaid" : reservationStatus;
        this.createdAt = createdAt;
        this.customerRequest = (customerRequest == null) ? "" : customerRequest;
    }

    public String getReservationId() { return reservationId; }
    public String getRoomNumber() { return roomNumber; }
    public String getGuestName() { return guestName; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public int getGuestNum() { return guestNum; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getReservationStatus() { return reservationStatus; }
    public String getCreatedAt() { return createdAt; }
    public String getCustomerRequest() { return customerRequest;}

    //setter
    public void setReservationStatus(String reservationStatus) { this.reservationStatus = reservationStatus; }
    public void setCustomerRequest(String customerRequest){this.customerRequest = customerRequest;}
    
    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%d,%s, %s, %s, %s", reservationId, roomNumber, guestName, checkInDate, checkOutDate, guestNum, phoneNumber, reservationStatus, createdAt, customerRequest);
    }
}