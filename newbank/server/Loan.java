package newbank.server;

import java.util.ArrayList;
import java.util.HashMap;

public class Loan {

    private final HashMap<Integer, LoanRequest> loanRequestList;
    private String senderId;
    private String recieverName;
    private int key;

    Loan(String senderId, String recieverName) {
        this.loanRequestList = new HashMap<>();
        this.senderId = senderId;
        this.recieverName = recieverName;
        this.key = 0;
    }

    public void requestLoan(String senderId, String recieverName, String amount) {
        LoanRequest newRequest = new LoanRequest(senderId, recieverName, amount);
        loanRequestList.put(key, newRequest);
        key++;
    }

    public String getSenderId(){return this.senderId;}

    public String getRecieverName(){return this.recieverName;}

    public LoanRequest getLoanRequest(int number) {
        return loanRequestList.get(number);
    }

    public String checkLoanRequestStatus(String customerId) {

        String str = "";

        for (HashMap.Entry<Integer, LoanRequest> entry :  loanRequestList.entrySet()) {
            str += entry.getKey() + ": " + entry.getValue().getLoanRequestStatus() + "\n";
        }
        return str;
    }

}
