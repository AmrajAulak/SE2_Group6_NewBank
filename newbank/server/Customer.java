package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	
	public Customer() {
		accounts = new ArrayList<>();
	}
//
//	public void addAccounts(ArrayList<Account> accountList){
//		accounts = accountList;
//	}
	
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

	public ArrayList<Account> getAccounts() {
		return accounts;
	}

	public boolean moveFunds(String accountFrom, String accountTo, double amount){
		boolean debitAccount = false;
		boolean creditAccount = false;

		for(Account a : accounts) {
			if (a.getAccountName().equals(accountFrom)){
				debitAccount = true;
				if (!a.deductBalance(amount)){
					return false;
				}
			}
			if (a.getAccountName().equals(accountTo)){
				creditAccount = true;
				a.addBalance(amount);
			}
		}

		if (debitAccount && creditAccount) {
			return true;
		}
		return false;
	}
}
