package server.model;

/**
 *
 * @author user
 */
public class Payment {
    private String paymentId;      // 결제 고유 번호 (P-xxxx)
    private String reservationId;  // 어떤 예약을 결제했는지 (FK)
    private String method;         // 결제 수단 (신용카드/계좌이체)
    private String cardNumber;     // 카드 번호
    private String cvc;            // CVC
    private String expiryDate;     // 유효기간
    private String password;       // 비번 앞 2자리
    private String paymentTime;      // 결제 일시

    public Payment(String paymentId, String reservationId, String method, String cardNumber, String cvc, String expiryDate, String password, String paymentTime) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.method = method;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.expiryDate = expiryDate;
        this.password = password;
        this.paymentTime = paymentTime;
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public String getReservationId() { return reservationId; }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                paymentId, reservationId, method, cardNumber, cvc, expiryDate, password, paymentTime);
    }
}
