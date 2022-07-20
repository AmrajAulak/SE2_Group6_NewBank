package newbank.server;

public class LoanRequest {

    private String senderId;
    private String recieverName;
    private String amount;
    private String status;

    public LoanRequest(String senderId, String recieverName, String amount) {
        this.senderId = senderId;
        this.recieverName = recieverName;
        this.amount = amount;
    }

    public String getRequestedCustomer() {
        return this.recieverName;
    }

    public String getLoanRequestStatus() {
        return this.senderId + " has requested a loan for " + this.amount;
    }

}
