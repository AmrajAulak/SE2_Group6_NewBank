package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class NewBankClientHandler extends Thread{
	
	private NewBank bank;
	private BufferedReader in;
	private PrintWriter out;

	
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
							} else {
								out.println("Registration Successful. Please login to continue");
								break;
							}
						case "login":
							if (customer != null) {
								out.println("Log In Successful. What do you want to do?");
								for(String element: bank.showMenu()){
									out.println(element);
								}

								while (true) {
									out.println(bank.checkIncomingLoanStatus(customer));
									String request = in.readLine();
									System.out.println("Request from " + customer.getKey());
									String response;

									switch(request.charAt(0)) {
										case '1': {	// SHOWMYACCOUNTS
											// prints customers accounts
											out.println(bank.showMyAccounts(customer));
											break;
										}
										case '2': {	//ADDACCOUNT
											// while loop so that customer can try again on incorrect formatting
											while (true) {
												String accountType;
												String depositAmount;
												out.println("Please choose account type from: MAIN, SAVINGS, or CHECKING");
												// reads account type response
												accountType = in.readLine();
												out.println("Please choose deposit amount");
												// reads deposit amount response
												depositAmount = in.readLine();

												// checks that the input can be parsed to a double otherwise prompts customer to try again
												try {
													Double.parseDouble(depositAmount);
													response = bank.addAccount(customer, accountType, depositAmount);
													out.println(response);
													break;
												} catch (NumberFormatException e) {
													out.println("Incorrect formatting: Please try again");
												}
											}
											break;
										}
										case '3': {	//MOVEFUNDS
											String accountFrom = "";
											String accountTo = "";
											String amount;
											out.println("Please choose account to withdraw funds");
											accountFrom= in.readLine();
											out.println("Please choose account to deposit funds");
											accountTo= in.readLine();
											out.println("Please enter the amount to transfer");
											amount= in.readLine();
											response = bank.move(customer, accountFrom, accountTo, amount);
											out.println(response);
											break;
										}
										case '4': {	//SENDFUNDS
											String customerTo = "";
											String amount;
											out.println("Please choose the customer Name to send funds to from your Main Account:");
											customerTo= in.readLine();
											out.println("Please enter the amount to transfer");
											amount= in.readLine();
											response = bank.send(customer, customerTo, amount);
											out.println(response);
											break;
										}

										case '5': { //REQUESTLOAN

											String recieverName;
											String amountRequested;
											out.println("Please input the name of the customer you would like to request a loan from");
											recieverName = in.readLine();
											out.println("Please input the amount you would like to request");
											amountRequested = in.readLine();
											response = bank.LoanRequest(customer, recieverName, amountRequested);
											out.println(response);
											break;
										}
                    
										case '6':{ //SEETXNS

											response = bank.seeTransactions(customer);
											out.println(response);
                      						break;
										}
										case '7' :{ //LOGOUT
											run();
										}
									}

									// Allows customer to type in another request
									out.println("What would you like to do next?");
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
