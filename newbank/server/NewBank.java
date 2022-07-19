package newbank.server;

import java.util.*;


public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,Customer> customers;
	private HashMap<String,String> passwords;
    private List<String> menuList = new ArrayList<>();
	private ArrayList <Transaction> bankLedger;
	
	private NewBank() {
		customers = new HashMap<>();
		passwords = new HashMap<>();
		addTestData();
		bankLedger = new ArrayList<>();
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

    public List<String> showMenu(){
        Collections.addAll(menuList,
                "SHOWMYACCOUNTS",
                "MAKEAPAYMENT", //What is this for? Is it not covered in Send Funds?
                "ADDACCOUNT",
                "MOVEFUNDS",
				"SENDFUNDS",
				"SEETXNS",
                "LOGOUT"
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

		Transaction transaction = new Transaction(Double.parseDouble(depositAmount), customer, "DEPOSIT", accountType );
		bankLedger.add(transaction);

		return currentCustomer.accountsToString();
	}

	public String move(CustomerID customer,String accountFrom, String accountTo, String amount){
		Customer currentCustomer = customers.get(customer.getKey());
		if (currentCustomer.moveFunds(accountFrom, accountTo, Double.parseDouble(amount))) {

			Transaction receiveTransaction = new Transaction(Double.parseDouble(amount), customer, "MOVE", accountTo );
			Transaction sendTransaction = new Transaction(-1*Double.parseDouble(amount), customer, "MOVE", accountFrom );

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

				Transaction receiveTransaction = new Transaction(Double.parseDouble(amount),senderID,"RECEIVE FROM "+senderID.getKey(),receiverCustomer.getAccounts().get(0).getAccountName());
				Transaction sendTransaction = new Transaction(-1*Double.parseDouble(amount),senderID,"SEND TO "+receiverName,senderCustomer.getAccounts().get(0).getAccountName() );

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

	public String seeTransactions(CustomerID customer){

		String transactionList="Created Date \t Transaction Type \t Amount";

		for (Transaction i : bankLedger) {
			if(i.getCustomer()==customer) {
				transactionList = transactionList.concat(i.getString());
			}
		}
		return transactionList;
		}
	}

