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
				"1 SHOWMYACCOUNTS",
				"2 ADDACCOUNT",
				"3 MOVEFUNDS",
				"4 SENDFUNDS",
				"5 REQUESTLOAN",
				"6 SEETXNS",
				"7 LOGOUT"
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

	private void writeCustomerDataToFile(Customer customer){
		// Could generalise using Object method input field a then setting file to "Bank_" a.getClass().toString()+".txt";
		try {
			FileOutputStream outputFile = new FileOutputStream("BankCustomers.txt");
			ObjectOutputStream outputObject = new ObjectOutputStream(outputFile);
			outputObject.writeObject(customer);
			outputObject.close();
			outputFile.close();
		}
		catch (Exception e) {
			System.out.println("Output Exception found:" + e);
		}
		readCustomerDataFromFile();
	}

	private void readCustomerDataFromFile(){

		try {
			FileInputStream inputFile = new FileInputStream("BankCustomers.txt");
			ObjectInputStream inputObject = new ObjectInputStream(inputFile);

			// Read objects
			Customer customer = (Customer) inputObject.readObject();

			System.out.println(customer.toString());

			inputObject.close();
			inputFile.close();
		}
		catch(Exception e){
			System.out.println("Input Exception found:" + e);
		}

	}
	public String registerNewCustomer(String userName, String password) {

		if (password.length() < 4){
			return "passwordError";
		} else if (customers.containsKey(userName)){
			return "userNameError";
		} else {
			customers.put(userName, new Customer());
			passwords.put(userName, password);
			writeCustomerDataToFile(customers.get(userName));
			return "registered";
		}
	}
	
	public static NewBank getBank() {
		return bank;
	}
	
	public synchronized CustomerID checkLogInDetails(String userName, String password) {

		if(customers.containsKey(userName)) {
			if(passwords.get(userName).equals(password)) {
				return new CustomerID(userName);
			}
		}
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

