package newbank.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Transaction {

    private String createdDate;
    private String transactionID;
    private double amount;
    private Customer customer;
    private String transactionType;
    private String account;

    public Transaction(double amount, Customer customer, String type, String account){

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.createdDate=formatter.format(date);

        Random rand = new Random();
        this.transactionID = "TXN"+Math.round(rand.nextFloat()*1e6);

        this.amount = amount;
        this.customer=customer;
        this.transactionType=type;
        this.account=account;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getString() {
        return createdDate+" \t \t \t "+transactionType+" \t \t \t "+amount+" \n";
    }
}
