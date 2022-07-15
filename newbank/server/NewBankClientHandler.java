package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
								while (true) {
									String request = in.readLine();
									System.out.println("Request from " + customer.getKey());
									String response;

									switch(request) {
										case "SHOWMYACCOUNTS" : {
											// prints customers accounts
											out.println(bank.showMyAccounts(customer));
											break;
										}
										case "ADDACCOUNT" : {
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
										}
									}

									// Allows customer to type in another request
									out.println("What would you like to do next?");

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
