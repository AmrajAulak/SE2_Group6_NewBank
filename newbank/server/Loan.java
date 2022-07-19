package newbank.server;

import java.util.ArrayList;

public class Loan {

    private final ArrayList<LoanRequest> loanRequestList;
    private String senderId;
    private String recieverName;

    Loan(String senderId, String recieverName) {
        this.loanRequestList = new ArrayList <>();
        this.senderId = senderId;
        this.recieverName = recieverName;
    }

    public void requestLoan(String senderId, String recieverName, String amount) {
        LoanRequest newRequest = new LoanRequest(senderId, recieverName, amount);
        loanRequestList.add(newRequest);
    }

    public String getSenderId(){return this.senderId;}

    public String getRecieverName(){return this.recieverName;}

    public String checkLoanRequestStatus(String customerId) {

        for (LoanRequest request : loanRequestList) {
            if (request.getRequestedCustomer().equals(customerId)) {
                return request.getLoanRequestStatus();
            }
        }
        return "No messages";
    }

}
