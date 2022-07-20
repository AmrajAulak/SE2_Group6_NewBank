package newbank.server;

import java.util.*;


public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,Customer> customers;
	private HashMap<String,String> passwords;
    private List<String> menuList = new ArrayList<>();
	ArrayList <Loan> loansList = new ArrayList<>();

	
	private NewBank() {
		customers = new HashMap<>();
		passwords = new HashMap<>();
		addTestData();
		ArrayList <Transaction> bankLedger = new ArrayList<Transaction>();
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

		if (password.length() < 4){
			return "passwordError";
		} else if (customers.containsKey(userName)){
			return "userNameError";
		} else {
			customers.put(userName, new Customer());
			passwords.put(userName, password);
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


	// commands from the NewBank customer are processed in this method
	// commented out for the time being as not using
//	public synchronized String processRequest(CustomerID customer, String request) {
//		if(customers.containsKey(customer.getKey())) {
//			switch(request) {
//			case "SHOWMYACCOUNTS" : return showMyAccounts(customer);
//			default : return "FAIL";
//			}
//		}
//		return "FAIL";
//	}

	// checks whether customer has any accounts and returns them as a string

	// hello there

    public List<String> showMenu(){
        Collections.addAll(menuList,
                "SHOWMYACCOUNTS",
                "MAKEAPAYMENT",
                "ADDACCOUNT",
                "MOVEFUNDS",
				"SENDFUNDS",
                "LOGOUT",
				"REQUESTLOAN"
                );
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
		return currentCustomer.accountsToString();
	}

	public String move(CustomerID customer,String accountFrom, String accountTo, String amount){
		Customer currentCustomer = customers.get(customer.getKey());
		if (currentCustomer.moveFunds(accountFrom, accountTo, Double.parseDouble(amount))) {
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
				return "Success! "+amount+" sent to "+receiverName;
			} else {
				return "Failure - Unable to transmit funds";
			}
		}
		else {
			return "Failure - Unable to withdraw funds";
			}
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

