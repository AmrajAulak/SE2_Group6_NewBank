package newbank.server;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
		bhagy.addAccount(new Account("Main", 100.0));
		customers.put("Bhagy", bhagy);
		passwords.put("Bhagy", "123");
		
		Customer christina = new Customer();
		christina.addAccount(new Account("Savings", 1500.0));
		customers.put("Christina", christina);
		passwords.put("Christina", "456");

		Customer john = new Customer();
		john.addAccount(new Account("Main", 100.0));
		john.addAccount(new Account("Checking", 250.0));
		customers.put("John", john);
		passwords.put("John", "789");

	}

	public String registerNewCustomer(String userName, String password) {

		Pattern numberCheck = Pattern.compile("[0-9]");
		Pattern capsCheck = Pattern.compile("[A-Z]");
		Matcher numMatch = numberCheck.matcher(password);
		Matcher capsMatch = capsCheck.matcher(password);
		boolean numFound = numMatch.find();
		boolean capsFound = capsMatch.find();

		Properties p = new Properties();
		try {
			p.load(new FileReader("userStore.properties"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		if (password.length() < 4){
			return "passwordError";
//		} else if (customers.containsKey(userName)){
//			return "userNameError";
		} else if (p.containsKey(userName)){
			return "userNameError";
		}else if (!numFound){
			return "numError";
		}else if (!capsFound){
			return "capsError";
		}  else {
//			customers.put(userName, new Customer());
//			passwords.put(userName, password);
			try {
				Properties p2 = new Properties();
				p2.setProperty(userName, password);
				p2.store(new FileWriter("userStore.properties", true), "");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "registered";
	}

	public String passwordReset(String userName, String oldPassword, String newPassword) {

		Pattern numberCheck = Pattern.compile("[0-9]");
		Pattern capsCheck = Pattern.compile("[A-Z]");
		Matcher numMatch = numberCheck.matcher(newPassword);
		Matcher capsMatch = capsCheck.matcher(newPassword);
		boolean numFound = numMatch.find();
		boolean capsFound = capsMatch.find();

		Properties p = new Properties();
		try {
			p.load(new FileReader("userStore.properties"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

//		if(!oldPassword.equals( passwords.get(userName))){
//			return "incorrect password";
		if(!oldPassword.equals(p.getProperty(userName))){
			return "incorrect password";
		} else if (newPassword.length() < 8) {
			return "passwordError";
		}else if (!numFound){
			return "numError";
		}else if (!capsFound){
			return "capsError";
		} else {
			try {
				p.setProperty(userName, newPassword);
				p.store(new FileWriter("userStore.properties"), "");
			} catch (IOException e) {
				e.printStackTrace();
			}
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

		if (senderAccount.payAmount(value)) {
			if (receiverAccount.addAmount(value)) {

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

