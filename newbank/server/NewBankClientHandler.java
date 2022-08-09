package newbank.server;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class NewBankClientHandler extends Thread{
	
	private NewBank bank;
	private BufferedReader in;
	private PrintWriter out;
	private ArrayList<String> AccountList = new ArrayList<>(Arrays.asList(
			"MAIN", "SAVINGS", "CHECKING"
	));

	private Properties customersFile = new Properties();
	private boolean validInput;
	
	public NewBankClientHandler(Socket s) throws IOException {
		bank = NewBank.getBank();
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream(), true);
	}
	
	public void run() {
		// keep getting requests from the client and processing them
			try {
				while (true) {
					out.println("Enter 'login' or 'register' ");
					String userChoice = in.readLine();
					// ask for user name
					out.println("Enter Username");
					String userName = in.readLine();
					// ask for password
					out.println("Enter Password");
					String password = in.readLine();
					out.println("Checking Details...");
					// authenticate user and get customer ID token from bank for use in subsequent requests
					CustomerID customer = bank.checkLogInDetails(userName, password);
					// if the user is authenticated then get requests from the user and process them
					switch (userChoice) {
						case "register":
							String str = bank.registerNewCustomer(userName, password);
							if (str.equals("passwordError")) {
								out.println("Error: your password needs to be at least 4 characters long");
								break;
							} else if (str.equals("userNameError")) {
								out.println("Error: username is already in use");
								break;
							} else if (str.equals("numError")) {
								out.println("Error: your password needs to contain at least one number character");
								break;
							} else if (str.equals("capsError")) {
								out.println("Error: your password needs to contain at least one capitalised letter");
								break;
							}else {
								out.println("Registration Successful. Please login to continue");
								break;
							}
						case "login":

							// checks if the user has any messages, waits for response before continuing
							if (!bank.checkIncomingLoanStatus(customer).equals("No messages")) {
								out.println(bank.checkIncomingLoanStatus(customer));
								out.println("Do you wish to ACCEPT or REJECT any of these loan requests? (YES/NO)");
								String response = in.readLine();

								if (response.equals("YES")) {
									out.println("Please input the number of the loan you would like to response to");
									String number = in.readLine();
									out.println("Please input ACCEPT or REJECT");
									String selection = in.readLine();
									out.println(bank.updateLoanStatus(customer, number, selection));

								}
							}

							if (customer != null) {
								out.println("Log In Successful. What do you want to do?");
								for(String element: bank.showMenu()){
									out.println(element);
								}

								while (true) {
									validInput = true;
									int request = 0;
									try {
										customersFile.load(new FileReader("userStore.properties"));
									}
									catch (IOException e) {
										e.printStackTrace();
									}

									try {
										request  = Integer.parseInt(in.readLine());
										if (request < 1 || request > 8){
											validInput = false;
											out.println("That is not an option. Try again.");
//											break; // this will escape the while loop
										}
									} catch (Exception e) {
										validInput = false;
										out.println("That is not an option. Try again.");
									}
//
									System.out.println("Request from " + customer.getKey());
									String response;

									switch(String.valueOf(request)) {
										case "1": {    // SHOWMYACCOUNTS
											// prints customers accounts
											out.println(bank.showMyAccounts(customer));
											break;
										}
										case "2": {    //ADDACCOUNT
											// while loop so that customer can try again on incorrect formatting
//											while (true) {
											String accountType;
											String depositAmount;


											// reads account type response
											while(true){
												out.println("Please choose account type from: MAIN, SAVINGS, or CHECKING");
												accountType = in.readLine();
												if (AccountList.contains(accountType)){
													break;
												}
												out.println("Not a valid account");
											}

											// reads deposit amount response
											while(true){
												out.println("Please enter deposit amount");
												depositAmount = in.readLine();
												try {
													Double.parseDouble(depositAmount);
													break;
												} catch (Exception e) {
													out.println("That is not a valid deposit amount");
												}
											}
											response = bank.addAccount(customer, accountType, depositAmount);
											out.println(response);
											break;
										}

										case "3": {    //MOVEFUNDS
											String accountFrom = "";
											String accountTo = "";
											String amount;
											while(true){
												out.println("Please choose account to withdraw funds");
												accountFrom = in.readLine();
												String usersAccounts = bank.showMyAccounts(customer);
												if (AccountList.contains(accountFrom) &&
														usersAccounts.contains(accountFrom)){
													break;
												}
												out.println("Not a valid account");
											}

											while(true){
												out.println("Please choose account to deposit funds");
												accountTo = in.readLine();
												String usersAccounts = bank.showMyAccounts(customer);
												if (AccountList.contains(accountTo) &&
														usersAccounts.contains(accountTo)){
													break;
												}
												out.println("Not a valid account");
											}

											while(true){
												out.println("Please enter the amount to transfer");
												amount = in.readLine();
												try {
													Double.parseDouble(amount);
													break;
												} catch (Exception e) {
													out.println("That is not a valid transfer amount");
												}
											}
											response = bank.move(customer, accountFrom, accountTo, amount);
											out.println(response);
											break;
										}
										case "4": {    //SENDFUNDS
											String customerTo = "";
											String amount;

											while(true) {
												out.println("Please choose the customer Name to send funds to from your Main Account:");
												customerTo = in.readLine();
												if(customersFile.containsKey(customerTo)){
													break;
												}
												out.println("Not a registered customer, please enter again");
											}

											while(true){
												out.println("Please enter the amount to transfer");
												amount = in.readLine();
												try {
													Double.parseDouble(amount);
													break;
												} catch (Exception e) {
													out.println("That is not a valid transfer amount");
												}
											}
											CustomerID payee = new CustomerID(customerTo);
//											response = bank.showMyAccounts(payee);
//											out.println(response);

											response = bank.send(customer, payee, amount);
											out.println(response);
											break;
										}

										case "5": { //REQUESTLOAN

											String recieverName;
											String amountRequested;
											while(true) {
												out.println("Please input the name of the customer you would like to request a loan from");
												recieverName = in.readLine();
												if(customersFile.containsKey(recieverName)){
													break;
												}
												out.println("Not a registered customer, please enter again");
											}

											while(true){
												out.println("Please input the amount you would like to request");
												amountRequested = in.readLine();
												try {
													Double.parseDouble(amountRequested);
													break;
												} catch (Exception e) {
													out.println("That is not a valid amount");
												}
											}

											response = bank.LoanRequest(customer, recieverName, amountRequested);
											out.println(response);
											break;
										}

										case "6": { //SEETXNS

											response = bank.seeTransactions(customer);
											out.println(response);
											break;
										}
										case "7": { //LOGOUT
											run();
										}

										case "8": {// CHANGE PASSWORD
											out.println("please confirm by entering your old password ");
											String oldPassword = in.readLine();
											// ask for user name
											out.println("Please enter your new password (it must be at least 8 characters long)");
											String newPassword = in.readLine();
											// ask for password
											out.println("Please confirm your new password by re-entering");
											String passwordCheck = in.readLine();
											if(!passwordCheck.equals(newPassword)) {
												out.println("Your password entries did not match");
												break;
											}else {
												out.println("Checking Details...");
												String responseStr = bank.passwordReset(userName, oldPassword, newPassword);
												if (responseStr.equals("passwordError")) {
													out.println("Error: your password needs to be at least 8 characters long");
													break;
												} else if (responseStr.equals("incorrect password")) {
													out.println("Error: you failed to correctly enter your existing password");
													break;

												} else if (responseStr.equals("numError")) {
													out.println("Error: your password needs to contain at least one number character");
													break;
												 	} else if (responseStr.equals("capsError")) {
												out.println("Error: your password needs to contain at least one capitalised letter");
												break;
											}else {
													out.println(responseStr);
													break;
												}
											}
										}
									}

									// Allows customer to type in another request
									if(validInput) {
										out.println("What would you like to do next?");
									}
									else{
										out.println("Please type a number to choose from the following options");
									}
									for(String element: bank.showMenu()){
										out.println(element);
									}

								}
							} else {
								out.println("Log In Failed");
							}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
	}

}
