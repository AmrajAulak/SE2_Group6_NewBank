package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NewBankClientHandler extends Thread {

	private NewBank bank;
	private BufferedReader in;
	private PrintWriter out;


	public NewBankClientHandler(Socket s) throws IOException {
		bank = NewBank.getBank();
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream(), true);
	}

//	public synchronized void waitBlock(String userInput){
//		userInput = in.readLine();
//		while( userInput.equals(null)){
//			try {wait(100);
//			} catch (Exception e) {}
//		}}
	//							waitBlock(accountType);

	public synchronized void customerActions(NewBank bank, BufferedReader in, PrintWriter out, CustomerID customer) {
		try {
			out.println("What do you want to do? \n Available commands: SHOWMYACCOUNTS, ADDACCOUNT, MOVE");
			while (true) {
				String request = in.readLine();
				System.out.println("Request from " + customer.getKey());
				String response;

				switch (request) {
					case "SHOWMYACCOUNTS": {
						// prints customers accounts
						out.println(bank.showMyAccounts(customer));
						break;
					}
					case "ADDACCOUNT": {
						while (true) {
							String accountType;
							String depositAmount;
							out.println("Please choose account type from: MAIN, SAVINGS, or CHECKING, or enter 'BACK' to return to menu");
							accountType = in.readLine();
							while (accountType.equals(null)) {
								try {wait(100);
								} catch (Exception e) {}
							}
							if (accountType.equals("BACK")) {
								customerActions(bank, in, out, customer);
							}
							out.println("Please choose deposit amount, or enter 'BACK' to return to menu");
							// reads deposit amount response
							depositAmount = in.readLine();
							while (depositAmount.equals(null)) {
								try {wait(100);
								} catch (Exception e) {}
							}
							if (depositAmount.equals("BACK")) {
								customerActions(bank, in, out, customer);
							}

							// checks that the input can be parsed to a double otherwise prompts customer to try again
							try {
								Double.parseDouble(depositAmount);
								response = bank.addAccount(customer, accountType, depositAmount);
								out.println(response);
								break;
							} catch (NumberFormatException e) {
								continue;
							}
						}
					}
					case "MOVE": {
						String accountFrom = "";
						String accountTo = "";
						String amount;
						out.println("Please choose account to withdraw funds, or enter 'BACK' to return to menu");
						// Add a BACK command
						accountFrom = in.readLine();
						while (accountFrom.equals(null)) {
							try {wait(100);
							} catch (Exception e) {}
						}
						if (accountFrom.equals("BACK")) {
							customerActions(bank, in, out, customer);
						} else {
							out.println("Please choose account to deposit funds, or enter 'BACK' to return to menu");
							// Add a return to menu command
							accountTo = in.readLine();
							while (accountTo.equals(null)) {
								try {wait(100);
								} catch (Exception e) {}
							}
							if (accountTo.equals("BACK")) {
								customerActions(bank, in, out, customer);
							} else {
								//Add a return to menu command
								out.println("Please enter the amount to transfer, or enter 'BACK' to return to menu");
								amount = in.readLine();
								while (amount.equals(null)) {
									try {wait(100);
									} catch (Exception e) {
									}
								}
								if (amount.equals("BACK")) {
									customerActions(bank, in, out, customer);
								} else {
									response = bank.move(customer, accountFrom, accountTo, amount);
									out.println(response);
									break;
								}
							}
						}

					}
				}
				// Allows customer to type in another request
				out.println("What would you like to do next? \n Available commands: SHOWMYACCOUNTS, ADDACCOUNT, MOVE");
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

	public void run() {
		// keep getting requests from the client and processing them
		try {
			while (true) {
				out.println("Enter 'login' or 'register' ");
				String userChoice = in.readLine();
				// Currently the above accepts any input and takes to username request. Should only accept login or register
				while (userChoice.equals(null)) {
					try {
						wait(100);
					} catch (Exception e) {
					}
				}


				switch (userChoice) {
					case "register":
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
						String str = bank.registerNewCustomer(userName, password);
						if (str.equals("passwordError")) {
							out.println("Error: your password needs to be at least 4 characters long");
							break;
						} else if (str.equals("userNameError")) {
							out.println("Error: username is already in use");
							break;
						} else {
							out.println("Registration Successful. Please login to continue");
							run();
						}
					case "login":
						// ask for user name
						out.println("Enter Username");
						userName = in.readLine();
						// ask for password
						out.println("Enter Password");
						password = in.readLine();
						out.println("Checking Details...");
						// authenticate user and get customer ID token from bank for use in subsequent requests
						customer = bank.checkLogInDetails(userName, password);
						// if the user is authenticated then get requests from the user and process them

						if (customer != null) {
							out.println("Log In Successful.");
							customerActions(bank, in, out, customer);

						} else {
							out.println("Log In Failed");
						}
				}
			}
			} catch(IOException e){
				e.printStackTrace();
			} finally{
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



