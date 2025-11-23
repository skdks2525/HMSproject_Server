package server.model;
/**
 *
 * @author user
 */
public class Room {
    private String roomNumber;
    private String type;
    private int price;
    private int capacity;
    private String description;
    
    public Room(String roomNumber, String type, int price, int capacity, String description) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.description = description;
    }
    
    public String getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public int getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return roomNumber + "," + type + "," + price + "," + capacity + "," + description;
    }
}
