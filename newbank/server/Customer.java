package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	
	public Customer() {
		accounts = new ArrayList<>();
	}
	
	public String accountsToString() {
		String s = "";
		for(Account a : accounts) {
			s += a.toString();
		}
		return s;
	}

	public void addAccount(Account account) {
		accounts.add(account);		
	}


	public Boolean moveFunds(String accountFrom, String accountTo, double amount){
		boolean canDebitAccount = false;
		boolean canCreditAccount = false;

		for(Account a : accounts) {
			if (a.getAccountName().equals(accountFrom)){
				canDebitAccount = true;
				if (!a.deductBalance(amount)){
					return false;
				}
			}
			if (a.getAccountName().equals(accountTo)){
				canCreditAccount = true;
				a.addBalance(amount);
			}
		}

		if (canDebitAccount && canCreditAccount) {
			return true;
		}
		return false;
	}
}
