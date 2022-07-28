package newbank.server;
import java.util.Random;

public class Account {
	
	private String accountName;

	private double balance;
	private long accountNumber;
	private double overdraftLimit;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName.toUpperCase();
		this.overdraftLimit = 500;
		this.balance = openingBalance;
		Random rand = new Random();
		this.accountNumber = Math.round(rand.nextFloat()*1e6);
	}
	
	public String toString() {
		return (accountName + ": " + balance + "\n");
	}

	public String getAccountName () {
		return accountName;
	}

	public Boolean deductAmount(double amount){
		if ((balance-amount) > 0) {
			balance = balance - amount;
			return true;
		} else{
			return false;
		}
	}

	public Boolean payAmount(double amount){
		if(accountName.equals("MAIN")) {
			if ((balance - amount) > overdraftLimit) {
				balance = balance - amount;
				return true;
			}
		}
		return false;
	}

	public Boolean addAmount (double amount){
		balance = balance + amount;
		return true;
	}

}
