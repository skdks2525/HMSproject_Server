package server.model;

import java.time.LocalDateTime;
import java.util.List;

public class MenuOrder {
    private String saleId; // S-1000 등
    private String guestName; // 사용자 ID
    private LocalDateTime orderTime;
    private int totalPrice;
    private String payment; // Paid, Unpaid
    private List<String> foodNames; // 여러 음식명

    public MenuOrder(String saleId, String guestName, LocalDateTime orderTime, int totalPrice, String payment, List<String> foodNames) {
        this.saleId = saleId;
        this.guestName = guestName;
        this.orderTime = orderTime;
        this.totalPrice = totalPrice;
        this.payment = payment;
        this.foodNames = foodNames;
    }

    public String getSaleId() { return saleId; }
    public String getGuestName() { return guestName; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public int getTotalPrice() { return totalPrice; }
    public String getPayment() { return payment; }
    public List<String> getFoodNames() { return foodNames; }

    public void setSaleId(String saleId) { this.saleId = saleId; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
    public void setPayment(String payment) { this.payment = payment; }
    public void setFoodNames(List<String> foodNames) { this.foodNames = foodNames; }
}
