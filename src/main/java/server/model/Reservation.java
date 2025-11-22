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
    
    public Reservation(String reservationId, String roomNumber, String guestName, String checkInDate, String checkOutDate){
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }
    
    //Getters        
    public String getReservationId() {
        return reservationId; 
    }
    public String getRoomNumber() { 
        return roomNumber; 
    }
    public String getGuestName() { 
        return guestName; 
    }
    public String getCheckInDate() { 
        return checkInDate; 
    }
    public String getCheckOutDate() { 
        return checkOutDate;
    }
    
    @Override
    public String toString(){
        return reservationId + "," + roomNumber + "," + guestName + "," + checkInDate + "," + checkOutDate;
    }
}
