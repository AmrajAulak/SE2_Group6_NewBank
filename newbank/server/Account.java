package newbank.server;
import java.util.Random;

public class Account {
	
	private String accountName;
	private double openingBalance;
	private long accountNumber;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName;
		this.openingBalance = openingBalance;
		Random rand = new Random();
		this.accountNumber = Math.round(rand.nextFloat()*1e6);
	}
	
	public String toString() {
		return (accountNumber + " " + accountName + ": " + openingBalance + "\n");
	}

}
