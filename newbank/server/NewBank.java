package newbank.server;

import java.io.*;
import java.util.*;


public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,Customer> customers;
	private HashMap<String,String> passwords;
    private List<String> menuList = new ArrayList<>();
	private ArrayList <Transaction> bankLedger;
	ArrayList <Loan> loansList = new ArrayList<>();
	
	private NewBank() {
		customers = new HashMap<>();
		passwords = new HashMap<>();
		addTestData();
    bankLedger = new ArrayList<>();
		
		Collections.addAll(menuList,
				"1 SHOW MY ACCOUNTS",
				"2 ADD ACCOUNT",
				"3 MOVE FUNDS",
				"4 SEND FUNDS",
				"5 REQUEST LOAN",
				"6 SEE TRANSACTIONS",
				"7 LOG-OUT",
				"8 CHANGE PASSWORD"
		);

	}
	
	private void addTestData() {
		Customer bhagy = new Customer();
		bhagy.addAccount(new Account("Main", 1000.0));
		customers.put("Bhagy", bhagy);
		passwords.put("Bhagy", "123");
		
		Customer christina = new Customer();
		christina.addAccount(new Account("Savings", 1500.0));
		customers.put("Christina", christina);
		passwords.put("Christina", "456");

		Customer john = new Customer();
		john.addAccount(new Account("Checking", 250.0));
		customers.put("John", john);
		passwords.put("John", "789");

	}

	public String registerNewCustomer(String userName, String password) {
		if (password.length() < 4) {
			return "passwordError";
		}

		Properties p = new Properties();
		try {
			p.load(new FileReader("userStore.properties"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		if(p.containsKey(userName)) {
			return "userNameError";
		}
		else{
			try {
				p.setProperty(userName, password);
				p.store(new FileWriter("userStore.properties", true), "");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "registered";
	}

	public String passwordReset(String userName, String oldPassword, String newPassword) {

		if(!oldPassword.equals( passwords.get(userName))){
			return "incorrect password";
		} else if (newPassword.length() < 8){
			return "passwordError";
		} else {
			passwords.put(userName, newPassword);
			return "You successfully changed your password";
		}
	}
	
	public static NewBank getBank() {
		return bank;
	}
	
	public synchronized CustomerID checkLogInDetails(String userName, String password) {
		Properties p = new Properties();
		try {
			p.load(new FileReader("userStore.properties"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		if(password.equals(p.getProperty(userName))) {
			Customer currentUser = new Customer();
			currentUser.addAccount(new Account("Savings", 1500.0));
			customers.put(userName, currentUser);
			passwords.put(userName, password);
			return new CustomerID(userName);
		}

//		if(customers.containsKey(userName)) {
//			if(passwords.get(userName).equals(password)) {
//				return new CustomerID(userName);
//			}
//		}
		return null;
	}

    public List<String> showMenu(){
        return menuList;
    }

	public String showMyAccounts(CustomerID customer) {
		if (customers.get(customer.getKey()).accountsToString().isEmpty()) {
			return "No accounts listed";
		} else {
			return (customers.get(customer.getKey())).accountsToString();
		}
	}

	// method to add account - gets current customer and adds account based off input
	public String addAccount(CustomerID customer, String accountType, String depositAmount) {
		Customer currentCustomer = customers.get(customer.getKey());
		currentCustomer.addAccount(new Account(accountType, Double.parseDouble(depositAmount)));

		Transaction transaction = new Transaction(Double.parseDouble(depositAmount), currentCustomer, "DEPOSIT", accountType );
		bankLedger.add(transaction);

		return currentCustomer.accountsToString();
	}

	public String move(CustomerID customer,String accountFrom, String accountTo, String amount){
		Customer currentCustomer = customers.get(customer.getKey());
		if (currentCustomer.moveFunds(accountFrom, accountTo, Double.parseDouble(amount))) {

			Transaction receiveTransaction = new Transaction(Double.parseDouble(amount), currentCustomer, "MOVE", accountTo );
			Transaction sendTransaction = new Transaction(-1*Double.parseDouble(amount), currentCustomer, "MOVE", accountFrom );

			bankLedger.add(receiveTransaction);
			bankLedger.add(sendTransaction);

			return "SUCCESS";
		} else {
			return "FAIL";
		}
	}

	public String send(CustomerID senderID, String receiverName, String amount){

		Customer receiverCustomer = customers.get(receiverName);
		Customer senderCustomer = customers.get(senderID.getKey());

		Account receiverAccount= receiverCustomer.getAccounts().get(0);
		Account senderAccount= senderCustomer.getAccounts().get(0);

		double value=Double.parseDouble(amount);

		if (senderAccount.deductBalance(value)) {
			if (receiverAccount.addBalance(value)) {

				Transaction receiveTransaction = new Transaction(Double.parseDouble(amount),receiverCustomer,"RECEIVE FROM "+senderID.getKey(),receiverCustomer.getAccounts().get(0).getAccountName());
				Transaction sendTransaction = new Transaction(-1*Double.parseDouble(amount),senderCustomer,"SEND TO "+receiverName,senderCustomer.getAccounts().get(0).getAccountName() );

				bankLedger.add(receiveTransaction);
				bankLedger.add(sendTransaction);

				return "Success! "+amount+" sent to "+receiverName;
			} else {
				return "Failure - Unable to transmit funds";
			}
		}
		else {
			return "Failure - Unable to withdraw funds";
			}
	}

	public String seeTransactions(CustomerID customerID){

		Customer customer = customers.get(customerID.getKey());
		String transactionList="Created Date \t \t \t Transaction Type \t \t \t Amount \n";

		for (Transaction i : bankLedger) {
			if(i.getCustomer()==customer) {
				transactionList = transactionList.concat(i.getString());
			}
		}
		return transactionList;
	}

	// checks that customer exists and then makes a new loan request and adds to arraylist of loans
	public String LoanRequest(CustomerID senderId, String receiverName, String amount) {
		if (customers.containsKey(receiverName)) {
			Loan loan = new Loan(senderId.getKey(), receiverName);
			loan.requestLoan(senderId.getKey(), receiverName, amount);
			loansList.add(loan);
			return "Loan request successful, awaiting approval...";
		} else {
			return "Customer not found";
		}
	}

	// iterates loan array and if the customer has any loan requests returns the status
	public String checkIncomingLoanStatus (CustomerID customer) {
		for (Loan loan:loansList) {
			if (loan.getRecieverName().equals(customer.getKey())){
				return loan.checkLoanRequestStatus(customer.getKey());
			}
		}
		return "No messages";
	}
}

