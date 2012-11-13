import java.util.*;
import java.io.*;
import java.net.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Client {

	private static Socket clientSocket;
	private static ObjectOutputStream outStream;
	private static ObjectInputStream inStream;
	private static boolean isOpen = false;
	private static String server = "localhost";

	public Client() {

	}

	public void connectToServer(String server, int port) throws UnknownHostException, IOException {

		try {
			clientSocket = new Socket(server, port);
			System.out.println("Connected to middleware.");
			outStream = new ObjectOutputStream(clientSocket.getOutputStream());
			outStream.flush();
			inStream = new ObjectInputStream(clientSocket.getInputStream());
			isOpen = true;
		} catch (UnknownHostException uhe) {
			isOpen = false;
			System.err.println("Unknown host :" + server);
			throw(uhe);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for " + server);
			throw(e);
		}

	}

	public boolean connectionIsOpen() {
		return isOpen;
	}

	public Object receiveData() throws IOException {

		Object received;

		try {
			received = inStream.readObject();
			return received;

		} catch (Exception e) {
			System.err.println("Error reading from server");
			e.printStackTrace();
		}
		return null;

	}

	public void sendData(JSONObject obj) throws IOException {
		
		outStream.writeObject(obj);
		outStream.flush();

	}

	public static void closeEverything() throws UnknownHostException, IOException {
		
		try {
			outStream.close();
			inStream.close();
			clientSocket.close();
			isOpen = false;
		} catch (UnknownHostException uhe) {
			System.err.println("Couldn't close socket connection");
			throw(uhe);
		} catch (IOException e) {
			System.err.println("Couldn't close I/O");
			throw(e);
		} 
	}
 
	public static void main(String args[]) {

		Client client = new Client();
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String command = "";
	    Vector arguments  = new Vector();
	    int id, cid;
	    int flightNum;
	    int flightPrice;
	    int flightSeats;
	    boolean room;
	    boolean car;
	    int price;
	    int numRooms;
	    int numCars;
	    String location;

	    boolean quit = false;

	    if (args.length == 1) 
			server = args[0]; 
	    else if (args.length != 0 && args.length != 1) {
			System.out.println ("Usage: java client [rmihost]"); 
			System.exit(1); 
		}

	    try {
			client.connectToServer(server, 7654);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (client.connectionIsOpen() && !quit) {


			System.out.println("\n\n\tClient Interface");
	    	System.out.println("Type \"help\" for list of supported commands");

	    	System.out.print("\n>");
			
			try {
			    //read the next command
			    command = stdin.readLine();
			} catch (IOException io) {
			    System.out.println("Unable to read from standard in");
			    System.exit(1);
			}
			//remove heading and trailing white space
			command = command.trim();
			arguments = client.parse(command);
			
			//decide which of the commands this was
			switch (client.findChoice((String) arguments.elementAt(0))) {
				case 1: //help section
				    if (arguments.size() == 1)   //command was "help"
						client.listCommands();
				    else if (arguments.size() == 2)  //command was "help <commandname>"
						client.listSpecific((String) arguments.elementAt(1));
				    else  //wrong use of help command
						System.out.println("Improper use of help command. Type help or help, <commandname>");
				    break;
				    
				case 2:  //new flight
				    if (arguments.size() != 5) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Adding a new Flight using id: " + arguments.elementAt(1));
				    System.out.println("Flight number: " + arguments.elementAt(2));
				    System.out.println("Add Flight Seats: " + arguments.elementAt(3));
				    System.out.println("Set Flight Price: " + arguments.elementAt(4));
				    
				    try {
						id = client.getInt(arguments.elementAt(1));
						flightNum = client.getInt(arguments.elementAt(2));
						flightSeats = client.getInt(arguments.elementAt(3));
						flightPrice = client.getInt(arguments.elementAt(4));

						JSONObject obj = new JSONObject();
						obj.put("method", "addFlight");
						obj.put("id", new Integer(id));
						obj.put("flightNum", new Integer(flightNum));
						obj.put("flightSeats", new Integer(flightSeats));
						obj.put("flightPrice", new Integer(flightPrice));
						client.sendData(obj);

						System.out.println((String) client.receiveData());
				    
				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;  
				case 3:  //new Car
				    if (arguments.size() != 5) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Adding a new Car using id: " + arguments.elementAt(1));
				    System.out.println("Car Location: " + arguments.elementAt(2));
				    System.out.println("Add Number of Cars: " + arguments.elementAt(3));
				    System.out.println("Set Price: " + arguments.elementAt(4));
				    
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));
						numCars = client.getInt(arguments.elementAt(3));
						price = client.getInt(arguments.elementAt(4));

						JSONObject obj = new JSONObject();
						obj.put("method", "addCars");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						obj.put("numCars", new Integer(numCars));
						obj.put("price", new Integer(price));
						client.sendData(obj);
						System.out.println((String) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 4:  //new Room
				    if (arguments.size() != 5) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Adding a new Room using id: "+arguments.elementAt(1));
				    System.out.println("Room Location: "+arguments.elementAt(2));
				    System.out.println("Add Number of Rooms: "+arguments.elementAt(3));
				    System.out.println("Set Price: "+arguments.elementAt(4));
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));
						numRooms = client.getInt(arguments.elementAt(3));
						price = client.getInt(arguments.elementAt(4));
						
						JSONObject obj = new JSONObject();
						obj.put("method", "addRooms");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						obj.put("numRooms", new Integer(numRooms));
						obj.put("price", new Integer(price));
						client.sendData(obj);

						System.out.println((String) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 5:  //new Customer
				    if (arguments.size() != 2){
						client.wrongNumber();
						break;
				    }
				    System.out.println("Adding a new Customer using id: " + arguments.elementAt(1));
				    try {
						id = client.getInt(arguments.elementAt(1));

						JSONObject obj = new JSONObject();
						obj.put("method", "newCustomer");
						obj.put("id", new Integer(id));
						client.sendData(obj);

						System.out.println("new customer id: " + (Integer) client.receiveData());

					} catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 6: //delete Flight
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Deleting a flight using id: "+arguments.elementAt(1));
				    System.out.println("Flight Number: "+arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						flightNum = client.getInt(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "deleteFlight");
						obj.put("id", new Integer(id));
						obj.put("flightNum", new Integer(flightNum));
						client.sendData(obj);

						System.out.println((String) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 7: //delete Car
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Deleting the cars from a particular location  using id: " + arguments.elementAt(1));
				    System.out.println("Car Location: " + arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "deleteCars");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println(client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 8: //delete Room
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
				    System.out.println("Room Location: " + arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "deleteRooms");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println(client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 9: //delete Customer
				    if (arguments.size() != 3) { 
						client.wrongNumber();
						break;
				    }
				    System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
				    System.out.println("Customer id: " + arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						int customerID = client.getInt(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "deleteCustomer");
						obj.put("id", new Integer(id));
						obj.put("customerID", new Integer(customerID));
						client.sendData(obj);

						System.out.println(client.receiveData());
				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 10: //querying a flight
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Querying a flight using id: "+arguments.elementAt(1));
				    System.out.println("Flight number: " + arguments.elementAt(2));
				    try {

						id = client.getInt(arguments.elementAt(1));
						flightNum = client.getInt(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "queryFlight");
						obj.put("id", new Integer(id));
						obj.put("flightNum", new Integer(flightNum));
						client.sendData(obj);

						System.out.println("Number of seats available: " + (Integer) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 11: //querying a Car Location
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Querying a car location using id: "+arguments.elementAt(1));
				    System.out.println("Car location: "+arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "queryCars");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println("Number of cars at this location: " + (Integer) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 12: //querying a Room location
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Querying a room location using id: "+arguments.elementAt(1));
				    System.out.println("Room location: "+arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "queryRooms");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println("Number of rooms at this location: " + (Integer) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 13: //querying Customer Information
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Querying Customer information using id: "+arguments.elementAt(1));
				    System.out.println("Customer id: "+arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						int customerID = client.getInt(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "queryCustomerInfo");
						obj.put("id", new Integer(id));
						obj.put("customerID", new Integer(customerID));
						client.sendData(obj);

						String result = (String) client.receiveData();
						if (result.equals("")) {
							System.out.println("Customer doesn't exist.");
						} else {
							System.out.println("Customer bill: " + result);
						}

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;		       
				    
				case 14: //querying a flight Price
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Querying a flight Price using id: " + arguments.elementAt(1));
				    System.out.println("Flight number: " + arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						flightNum = client.getInt(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "queryFlightPrice");
						obj.put("id", new Integer(id));
						obj.put("flightNum", new Integer(flightNum));
						client.sendData(obj);

						System.out.println("Price of a seat: " + (Integer) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 15: //querying a Car Price
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Querying a car price using id: "+arguments.elementAt(1));
				    System.out.println("Car location: "+arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "queryCarsPrice");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println("Price of a car at this location: " + (Integer) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }			    
				    break;

				case 16: //querying a Room price
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Querying a room price using id: " + arguments.elementAt(1));
				    System.out.println("Room Location: " + arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						location = client.getString(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "queryRoomsPrice");
						obj.put("id", new Integer(id));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println("Price of a room at this location: " + (Integer) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 17:  //reserve a flight
				    if (arguments.size() != 4) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Reserving a seat on a flight using id: " + arguments.elementAt(1));
				    System.out.println("Customer id: " + arguments.elementAt(2));
				    System.out.println("Flight number: " + arguments.elementAt(3));
				    try{
						id = client.getInt(arguments.elementAt(1));
						int customerID = client.getInt(arguments.elementAt(2));
						flightNum = client.getInt(arguments.elementAt(3));

						JSONObject obj = new JSONObject();
						obj.put("method", "reserveFlight");
						obj.put("id", new Integer(id));
						obj.put("customerID", new Integer(customerID));
						obj.put("flightNum", new Integer(flightNum));
						client.sendData(obj);

						System.out.println((String) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 18:  //reserve a car
				    if (arguments.size() != 4) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Reserving a car at a location using id: " + arguments.elementAt(1));
				    System.out.println("Customer id: " + arguments.elementAt(2));
				    System.out.println("Location: " + arguments.elementAt(3));
				    
				    try {
						id = client.getInt(arguments.elementAt(1));
						int customerID = client.getInt(arguments.elementAt(2));
						location = client.getString(arguments.elementAt(3));

						JSONObject obj = new JSONObject();
						obj.put("method", "reserveCar");
						obj.put("id", new Integer(id));
						obj.put("customerID", new Integer(customerID));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println((String) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 19:  //reserve a room
				    if (arguments.size() != 4) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Reserving a room at a location using id: "+arguments.elementAt(1));
				    System.out.println("Customer id: "+arguments.elementAt(2));
				    System.out.println("Location: "+arguments.elementAt(3));
				    try {
						id = client.getInt(arguments.elementAt(1));
						int customerID = client.getInt(arguments.elementAt(2));
						location = client.getString(arguments.elementAt(3));

						JSONObject obj = new JSONObject();
						obj.put("method", "reserveRoom");
						obj.put("id", new Integer(id));
						obj.put("customerID", new Integer(customerID));
						obj.put("location", location);
						client.sendData(obj);

						System.out.println((String) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				case 20:  //reserve an Itinerary
				    if (arguments.size() < 7) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Reserving an Itinerary using id:" + arguments.elementAt(1));
				    System.out.println("Customer id:" + arguments.elementAt(2));
				    
				    for (int i=0 ; i < arguments.size() - 6; i++) {
						System.out.println("Flight number" + arguments.elementAt(3+i));
					}
				    
				    System.out.println("Location for Car/Room booking:" + arguments.elementAt(arguments.size()-3));
				    System.out.println("Car to book?:" + arguments.elementAt(arguments.size()-2));
				    System.out.println("Room to book?:" + arguments.elementAt(arguments.size()-1));
				    
				    try {
						id = client.getInt(arguments.elementAt(1));
						int customerID = client.getInt(arguments.elementAt(2));
						
						JSONArray flights = new JSONArray();

						for (int i = 0; i < arguments.size() - 6; i++) {
						    flights.add(new Integer(client.getInt(arguments.elementAt(3+i))));
						}
						
						location = client.getString(arguments.elementAt(arguments.size() - 3));
						car = client.getBoolean(arguments.elementAt(arguments.size() - 2));
						room = client.getBoolean(arguments.elementAt(arguments.size() - 1));

						JSONObject obj = new JSONObject();
						obj.put("method", "itinerary");
						obj.put("id", new Integer(id));
						obj.put("customerID", new Integer(customerID));
						obj.put("flightNumbers", flights);
						obj.put("location", location);
						obj.put("car", new Boolean(car));
						obj.put("room", new Boolean(room));
						client.sendData(obj);

						System.out.println((String) client.receiveData());
						
				    }
				    catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    		    
				case 21:  //quit the client
				    if (arguments.size() != 1) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Quitting client.");

				    try {

					    JSONObject obj = new JSONObject();
						obj.put("method", "quit");
						client.sendData(obj);
						//System.out.println((String) client.receiveData());
					} catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
					}

				    quit = true;
				    break;
				    
				    
				case 22:  //new Customer given id
				    if (arguments.size() != 3) {
						client.wrongNumber();
						break;
				    }
				    System.out.println("Adding a new Customer using id:" + arguments.elementAt(1) + " and cid " + arguments.elementAt(2));
				    try {
						id = client.getInt(arguments.elementAt(1));
						cid = client.getInt(arguments.elementAt(2));

						JSONObject obj = new JSONObject();
						obj.put("method", "newCustomer");
						obj.put("id", new Integer(id));
						obj.put("cid", new Integer(cid));
						client.sendData(obj);

						System.out.println((String) client.receiveData());

				    } catch (Exception e) {
						System.out.println("EXCEPTION:");
						System.out.println(e.getMessage());
						e.printStackTrace();
				    }
				    break;
				    
				default:
				    System.out.println("The interface does not support this command.");
				    break;
			} //end switch
		} //end while
		
		try {
			closeEverything();
		} catch (Exception e) {
			e.printStackTrace();
		}

	} //end main

	public Vector parse(String command) {
		Vector arguments = new Vector();
		StringTokenizer tokenizer = new StringTokenizer(command,",");
		String argument ="";
		while (tokenizer.hasMoreTokens())
		    {
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		    }
		return arguments;
    }

    public int findChoice(String argument) {
		if (argument.compareToIgnoreCase("help")==0)
		    return 1;
		else if(argument.compareToIgnoreCase("newflight")==0)
		    return 2;
		else if(argument.compareToIgnoreCase("newcar")==0)
		    return 3;
		else if(argument.compareToIgnoreCase("newroom")==0)
		    return 4;
		else if(argument.compareToIgnoreCase("newcustomer")==0)
		    return 5;
		else if(argument.compareToIgnoreCase("deleteflight")==0)
		    return 6;
		else if(argument.compareToIgnoreCase("deletecar")==0)
		    return 7;
		else if(argument.compareToIgnoreCase("deleteroom")==0)
		    return 8;
		else if(argument.compareToIgnoreCase("deletecustomer")==0)
		    return 9;
		else if(argument.compareToIgnoreCase("queryflight")==0)
		    return 10;
		else if(argument.compareToIgnoreCase("querycar")==0)
		    return 11;
		else if(argument.compareToIgnoreCase("queryroom")==0)
		    return 12;
		else if(argument.compareToIgnoreCase("querycustomer")==0)
		    return 13;
		else if(argument.compareToIgnoreCase("queryflightprice")==0)
		    return 14;
		else if(argument.compareToIgnoreCase("querycarprice")==0)
		    return 15;
		else if(argument.compareToIgnoreCase("queryroomprice")==0)
		    return 16;
		else if(argument.compareToIgnoreCase("reserveflight")==0)
		    return 17;
		else if(argument.compareToIgnoreCase("reservecar")==0)
		    return 18;
		else if(argument.compareToIgnoreCase("reserveroom")==0)
		    return 19;
		else if(argument.compareToIgnoreCase("itinerary")==0)
		    return 20;
		else if (argument.compareToIgnoreCase("quit")==0)
		    return 21;
		else if (argument.compareToIgnoreCase("newcustomerid")==0)
		    return 22;
		else
		    return 666;
    }

    public void listCommands() {
		System.out.println("\nWelcome to the client interface provided to test your project.");
		System.out.println("Commands accepted by the interface are:");
		System.out.println("help");
		System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcustomerid\ndeleteflight\ndeletecar\ndeleteroom");
		System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
		System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
		System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
		System.out.println("quit");
		System.out.println("\ntype help, <commandname> for detailed info(NOTE the use of comma).");
    }


    public void listSpecific(String command) {
		System.out.print("Help on: ");
		switch (findChoice(command)) {
		    case 1:
			System.out.println("Help");
			System.out.println("\nTyping help on the prompt gives a list of all the commands available.");
			System.out.println("Typing help, <commandname> gives details on how to use the particular command.");
			break;

		    case 2:  //new flight
			System.out.println("Adding a new Flight.");
			System.out.println("Purpose:");
			System.out.println("\tAdd information about a new flight.");
			System.out.println("\nUsage:");
			System.out.println("\tnewflight,<id>,<flightnumber>,<flightSeats>,<flightprice>");
			break;
			
		    case 3:  //new Car
			System.out.println("Adding a new Car.");
			System.out.println("Purpose:");
			System.out.println("\tAdd information about a new car location.");
			System.out.println("\nUsage:");
			System.out.println("\tnewcar,<id>,<location>,<numberofcars>,<pricepercar>");
			break;
			
		    case 4:  //new Room
			System.out.println("Adding a new Room.");
			System.out.println("Purpose:");
			System.out.println("\tAdd information about a new room location.");
			System.out.println("\nUsage:");
			System.out.println("\tnewroom,<id>,<location>,<numberofrooms>,<priceperroom>");
			break;
			
		    case 5:  //new Customer
			System.out.println("Adding a new Customer.");
			System.out.println("Purpose:");
			System.out.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
			System.out.println("\nUsage:");
			System.out.println("\tnewcustomer,<id>");
			break;
			
			
		    case 6: //delete Flight
			System.out.println("Deleting a flight");
			System.out.println("Purpose:");
			System.out.println("\tDelete a flight's information.");
			System.out.println("\nUsage:");
			System.out.println("\tdeleteflight,<id>,<flightnumber>");
			break;
			
		    case 7: //delete Car
			System.out.println("Deleting a Car");
			System.out.println("Purpose:");
			System.out.println("\tDelete all cars from a location.");
			System.out.println("\nUsage:");
			System.out.println("\tdeletecar,<id>,<location>");
			break;
			
		    case 8: //delete Room
			System.out.println("Deleting a Room");
			System.out.println("\nPurpose:");
			System.out.println("\tDelete all rooms from a location.");
			System.out.println("Usage:");
			System.out.println("\tdeleteroom,<id>,<location>,<numRooms>");
			break;
			
		    case 9: //delete Customer
			System.out.println("Deleting a Customer");
			System.out.println("Purpose:");
			System.out.println("\tRemove a customer from the database.");
			System.out.println("\nUsage:");
			System.out.println("\tdeletecustomer,<id>,<customerid>");
			break;
			
		    case 10: //querying a flight
			System.out.println("Querying flight.");
			System.out.println("Purpose:");
			System.out.println("\tObtain Seat information about a certain flight.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryflight,<id>,<flightnumber>");
			break;
			
		    case 11: //querying a Car Location
			System.out.println("Querying a Car location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain number of cars at a certain car location.");
			System.out.println("\nUsage:");
			System.out.println("\tquerycar,<id>,<location>");		
			break;
			
		    case 12: //querying a Room location
			System.out.println("Querying a Room Location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain number of rooms at a certain room location.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryroom,<id>,<location>");		
			break;
			
		    case 13: //querying Customer Information
			System.out.println("Querying Customer Information.");
			System.out.println("Purpose:");
			System.out.println("\tObtain information about a customer.");
			System.out.println("\nUsage:");
			System.out.println("\tquerycustomer,<id>,<customerid>");
			break;		       
			
		    case 14: //querying a flight for price 
			System.out.println("Querying flight.");
			System.out.println("Purpose:");
			System.out.println("\tObtain price information about a certain flight.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryflightprice,<id>,<flightnumber>");
			break;
			
		    case 15: //querying a Car Location for price
			System.out.println("Querying a Car location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain price information about a certain car location.");
			System.out.println("\nUsage:");
			System.out.println("\tquerycarprice,<id>,<location>");		
			break;
			
		    case 16: //querying a Room location for price
			System.out.println("Querying a Room Location.");
			System.out.println("Purpose:");
			System.out.println("\tObtain price information about a certain room location.");
			System.out.println("\nUsage:");
			System.out.println("\tqueryroomprice,<id>,<location>");		
			break;

		    case 17: //reserve a flight
			System.out.println("Reserving a flight.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a flight for a customer.");
			System.out.println("\nUsage:");
			System.out.println("\treserveflight,<id>,<customerid>,<flightnumber>");
			break;
			
		    case 18:  //reserve a car
			System.out.println("Reserving a Car.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a car for a customer at a particular location.");
			System.out.println("\nUsage:");
			System.out.println("\treservecar,<id>,<customerid>,<location>");
			break;
			
		    case 19:  //reserve a room
			System.out.println("Reserving a Room.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
			System.out.println("\nUsage:");
			System.out.println("\treserveroom,<id>,<customerid>,<location>");
			break;
			
		    case 20:  //reserve an Itinerary
			System.out.println("Reserving an Itinerary.");
			System.out.println("Purpose:");
			System.out.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
			System.out.println("\nUsage:");
			System.out.println("\titinerary,<id>,<customerid>,<flightnumber1>....<flightnumberN>,<LocationToBookCarsOrRooms>,<NumberOfCars>,<NumberOfRoom>");
			break;
			

		    case 21:  //quit the client
			System.out.println("Quitting client.");
			System.out.println("Purpose:");
			System.out.println("\tExit the client application.");
			System.out.println("\nUsage:");
			System.out.println("\tquit");
			break;
			
		    case 22:  //new customer with id
			System.out.println("Create new customer providing an id");
			System.out.println("Purpose:");
			System.out.println("\tCreates a new customer with the id provided");
			System.out.println("\nUsage:");
			System.out.println("\tnewcustomerid, <id>, <customerid>");
			break;

		    default:
			System.out.println(command);
			System.out.println("The interface does not support this command.");
			break;
		}
    }
    
    public void wrongNumber() {
		System.out.println("The number of arguments provided in this command are wrong.");
		System.out.println("Type help, <commandname> to check usage of this command.");
    }



    public int getInt(Object temp) throws Exception {
		try {
			return (new Integer((String)temp)).intValue();
		} catch(Exception e) {
			throw e;
		}
    }
    
    public boolean getBoolean(Object temp) throws Exception {
    	try {
    		return (new Boolean((String)temp)).booleanValue();
    	} catch(Exception e) {
    		throw e;
    	}
    }

    public String getString(Object temp) throws Exception {
		try {	
			return (String)temp;
		} catch (Exception e) {
			throw e;
		}
    }
}