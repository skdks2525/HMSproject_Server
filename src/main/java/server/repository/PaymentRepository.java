package server.repository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import server.model.Payment;
/**
 *
 * @author skdks
 */
public class PaymentRepository {
    private static final String PAY_FILE_PATH = "data/payments.csv";
    
    public synchronized boolean add(Payment payment){
        File file = new File(PAY_FILE_PATH);
        boolean isNewFile = !file.exists();
        
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                bw.write("PaymentID,ResID,Method,CardNum,CVC,Expiry,PW,PaymentTime");
                bw.newLine();
            } else {
                bw.newLine();
            }
            
            bw.write(payment.toString());
            return true;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Find the latest payment record for given reservationId (returns null if not found)
    public synchronized server.model.Payment findLatestByReservationId(String resId) {
        File file = new File(PAY_FILE_PATH);
        if (!file.exists()) return null;
        server.model.Payment found = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 9) {
                    String paymentId = cols[0];
                    String reservationId = cols[1];
                    if (reservationId.equals(resId)) {
                        String method = cols[2];
                        String cardNum = cols[3];
                        String cvc = cols[4];
                        String expiry = cols[5];
                        String pw = cols[6];
                        int amount = 0;
                        try { amount = Integer.parseInt(cols[7]); } catch (NumberFormatException ex) { amount = 0; }
                        String time = cols[8];
                        found = new server.model.Payment(paymentId, reservationId, method, cardNum, cvc, expiry, pw, amount, time);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return found;
    }
}
