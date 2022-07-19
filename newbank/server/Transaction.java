package newbank.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Transaction {

    private String createdDate;
    private String transactionID;
    private long amount;
    private CustomerID CustomerID;
    private String transactionType;

    public Transaction(long amount, CustomerID customer, String type){

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.createdDate=formatter.format(date);

        Random rand = new Random();
        this.transactionID = "TXN"+Math.round(rand.nextFloat()*1e6);

        this.amount = amount;
        this.CustomerID=customer;
        this.transactionType=type;
    }

    public CustomerID getCustomer() {
        return CustomerID;
    }

    public String getString() {
        return createdDate+" \t "+transactionType+" \t "+amount+" \n";
    }
}
