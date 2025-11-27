package server.repository;
import server.model.*;
import java.io.*;
import java.util.*;
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
}
