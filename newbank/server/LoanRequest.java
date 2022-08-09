package newbank.server;

public class LoanRequest {

    private String senderId;
    private String recieverName;
    private String requestedAmount;
    private String status;
    private String outstandingAmount;

    public LoanRequest(String senderId, String recieverName, String amount) {
        this.senderId = senderId;
        this.recieverName = recieverName;
        this.requestedAmount = amount;
        this.status = "awaitingApproval";
    }

    public String getRequestedCustomer() {
        return this.recieverName;
    }

    public String getOutstandingAmount(){return this.outstandingAmount;}

    public String getRequestedAmount() {return this.requestedAmount;}

    public String getLoanRequestStatus() {
        return this.senderId + " has requested a loan for " + this.requestedAmount + " and loan status is currently: " + this.status;
    }

    public void updateLoanStatus(String response) {
        if (response.equals("ACCEPT")) {
            this.status = "approved";
            this.outstandingAmount = this.requestedAmount;
        } else {
            this.status = "denied";
        }
    }

}
