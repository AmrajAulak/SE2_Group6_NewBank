package newbank.server;

import java.util.ArrayList;

public class Customer{
	
	private ArrayList<Account> accounts;
	private String currentUser;
	
	public Customer() {
		accounts = new ArrayList<>();
	}
//
//	public void addAccounts(ArrayList<Account> accountList){
//		accounts = accountList;
//	}

	public String getUser(){
		return currentUser;
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

	public ArrayList<Account> getAccounts() {
		return accounts;
	}

	public Account getCurrentAccount(){
		for(Account a : accounts) {
			if (a.getAccountName().equals("MAIN")) {
				return a;
			}
		}
		return null;
	}

	public boolean moveFunds(String accountFrom, String accountTo, double amount){
		boolean debitAccount = false;
		boolean creditAccount = false;

		for(Account a : accounts) {
			if (a.getAccountName().equals(accountFrom)){
				debitAccount = true;
				if (!a.deductAmount(amount)){
					return false;
				}
			}
			if (a.getAccountName().equals(accountTo)){
				creditAccount = true;
				a.addAmount(amount);
			}
		}

		if (debitAccount && creditAccount) {
			return true;
		}
		return false;
	}
}
