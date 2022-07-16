package newbank.server;
import java.util.Random;

public class Account {
	
	private String accountName;

	private double balance;
	private long accountNumber;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName.toUpperCase();

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

	public Boolean deductBalance(double amount){
		if ((balance-amount) > 0) {
			balance = balance - amount;
			return true;
		} else{
			return false;
		}
	}

	public void addBalance (double amount){
		balance = balance + amount;
	}

}
