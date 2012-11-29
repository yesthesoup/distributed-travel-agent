package clientsrc;

import java.rmi.*;
//import ResInterface.*;
import MidInterface.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.*;
import java.io.*;


public class Client
{
    static String message = "blank";
    static Middleware mw = null;

    public static void main(String args[])
	{
	    Client client = new Client();
	    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	    String command = "";
	    Vector arguments  = new Vector();
	    int Id, Cid, Tid, iid, iidCar, iidRoom;
	    int flightNum;
	    int flightPrice;
	    int flightSeats;
	    boolean room;
	    boolean car;
	    int price;
	    int numRooms;
	    int numCars;
	    String location;


	    String server = "localhost";
	    if (args.length == 1) 
			server = args[0]; 
	    else if (args.length != 0 &&  args.length != 1) {
			System.out.println ("Usage: java client [rmihost]"); 
			System.exit(1); 
		}
		
	    try {
		    // get a reference to the rmiregistry
		    Registry registry = LocateRegistry.getRegistry(server, 8765);
		    // get the proxy and the remote reference by rmiregistry lookup
		    mw = (Middleware) registry.lookup("HAL9001Middleware");
		    
		    if (mw != null) {
			    System.out.println("Successful");
			    System.out.println("Connected to Middleware");
			} else {
			    System.out.println("Unsuccessful");
			}
		    // make call on remote method
		} catch (Exception e) {	
		    System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		}
	    
	    /*
	    if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
	    }
	    */

	    System.out.println("\n\n\tClient Interface");
	    System.out.println("Type \"help\" for list of supported commands");
	    while (true) {

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
			switch (client.findChoice((String)arguments.elementAt(0))) {
			case 1: //help section
			    if(arguments.size()==1)   //command was "help"
				client.listCommands();
			    else if (arguments.size()==2)  //command was "help <commandname>"
				client.listSpecific((String)arguments.elementAt(1));
			    else  //wrong use of help command
				System.out.println("Improper use of help command. Type help or help, <commandname>");
			    break;
			    
			case 2:  //new flight
			    if (arguments.size() != 6) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Adding a new Flight using id: "+arguments.elementAt(1));
			    System.out.println("Flight number: "+arguments.elementAt(2));
			    System.out.println("Add Flight Seats: "+arguments.elementAt(3));
			    System.out.println("Set Flight Price: "+arguments.elementAt(4));
			    System.out.println("Transaction ID: "+arguments.elementAt(5));
			    
			    try {
					Id = client.getInt(arguments.elementAt(1));
					flightNum = client.getInt(arguments.elementAt(2));
					flightSeats = client.getInt(arguments.elementAt(3));
					flightPrice = client.getInt(arguments.elementAt(4));
					Tid = client.getInt(arguments.elementAt(5));
					long tOld = System.currentTimeMillis();
					if (mw.addFlight(Id, flightNum, flightSeats, flightPrice, Tid)) {
					    System.out.println("Flight added");
					} else {
					    System.out.println("Flight could not be added");
					}
					System.out.println("TIME: " + (System.currentTimeMillis() - tOld));
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 3:  //new Car
			    if (arguments.size() != 7) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Adding a new Car using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Car Location: "+arguments.elementAt(3));
			    System.out.println("Add Number of Cars: "+arguments.elementAt(4));
			    System.out.println("Set Price: "+arguments.elementAt(5));
			    System.out.println("Transaction ID: "+arguments.elementAt(6));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					numCars = client.getInt(arguments.elementAt(4));
					price = client.getInt(arguments.elementAt(5));
					Tid = client.getInt(arguments.elementAt(6));
					long tOld = System.currentTimeMillis();
					if(mw.addCars(Id,iid,location,numCars,price,Tid))
					    System.out.println("Cars added");
					else
					    System.out.println("Cars could not be added");
					System.out.println("TIME: " + (System.currentTimeMillis() - tOld));
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 4:  //new Room
			    if(arguments.size() != 7){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Adding a new Room using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Room Location: "+arguments.elementAt(3));
			    System.out.println("Add Number of Rooms: "+arguments.elementAt(4));
			    System.out.println("Set Price: "+arguments.elementAt(5));
			    System.out.println("Transaction ID: "+arguments.elementAt(6));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					numRooms = client.getInt(arguments.elementAt(4));
					price = client.getInt(arguments.elementAt(5));
					Tid = client.getInt(arguments.elementAt(6));
					long tOld = System.currentTimeMillis();
					if(mw.addRooms(Id,iid,location,numRooms,price,Tid))
					    System.out.println("Rooms added");
					else
					    System.out.println("Rooms could not be added");
					System.out.println("TIME: " + (System.currentTimeMillis() - tOld));
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 5:  //new Customer
			    if (arguments.size() != 3){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Adding a new Customer using id:"+arguments.elementAt(1));
			    System.out.println("Transaction ID: "+arguments.elementAt(2));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					Tid = client.getInt(arguments.elementAt(2));
					int customer = mw.newCustomer(Id, Tid);
					System.out.println("new customer id:" + customer);
				} catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 6: //delete Flight
			    if (arguments.size() != 4) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Deleting a flight using id: "+arguments.elementAt(1));
			    System.out.println("Flight Number: "+arguments.elementAt(2));
			    System.out.println("Transaction ID: "+arguments.elementAt(3));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					flightNum = client.getInt(arguments.elementAt(2));
					Tid = client.getInt(arguments.elementAt(3));
					if (mw.deleteFlight(Id,flightNum,Tid))
					    System.out.println("Flight Deleted");
					else
					    System.out.println("Flight could not be deleted");
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 7: //delete Car
			    if(arguments.size() != 5) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Deleting the cars from a particular location  using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Car Location: " + arguments.elementAt(3));
			    System.out.println("Transaction ID: "+arguments.elementAt(4));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					Tid = client.getInt(arguments.elementAt(4));
					if(mw.deleteCars(Id,iid,location,Tid))
					    System.out.println("Cars Deleted");
					else
					    System.out.println("Cars could not be deleted");
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 8: //delete Room
			    if (arguments.size() != 5) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Deleting all rooms from a particular location  using id: " + arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Room Location: " + arguments.elementAt(3));
			    System.out.println("Transaction ID: "+arguments.elementAt(4));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					Tid = client.getInt(arguments.elementAt(4));
					if(mw.deleteRooms(Id, iid, location, Tid))
					    System.out.println("Rooms Deleted");
					else
					    System.out.println("Rooms could not be deleted");
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 9: //delete Customer
			    if (arguments.size() != 4) { 
					client.wrongNumber();
					break;
			    }
			    System.out.println("Deleting a customer from the database using id: " + arguments.elementAt(1));
			    System.out.println("Customer id: " + arguments.elementAt(2));
			    System.out.println("Transaction ID: "+arguments.elementAt(3));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					int customer = client.getInt(arguments.elementAt(2));
					Tid = client.getInt(arguments.elementAt(3));
					if (mw.deleteCustomer(Id, customer, Tid))
					    System.out.println("Customer Deleted");
					else
					    System.out.println("Customer could not be deleted");
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 10: //querying a flight
			    if (arguments.size() != 4) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Querying a flight using id: "+arguments.elementAt(1));
			    System.out.println("Flight number: " + arguments.elementAt(2));
			    System.out.println("Transaction ID: "+arguments.elementAt(3));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					flightNum = client.getInt(arguments.elementAt(2));
					Tid = client.getInt(arguments.elementAt(3));
					int seats = mw.queryFlight(Id, flightNum, Tid);
					System.out.println("Number of seats available:"+seats);
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 11: //querying a Car Location
			    if (arguments.size() != 5) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Querying a car location using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Car location: "+arguments.elementAt(3));
			    System.out.println("Transaction ID: "+arguments.elementAt(4));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					Tid = client.getInt(arguments.elementAt(4));
					numCars = mw.queryCars(Id, iid, location, Tid);
					System.out.println("number of Cars at this location:"+numCars);
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 12: //querying a Room location
			    if (arguments.size() != 5) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Querying a room location using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Room location: "+arguments.elementAt(3));
			    System.out.println("Transaction ID: "+arguments.elementAt(4));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					Tid = client.getInt(arguments.elementAt(4));
					numRooms = mw.queryRooms(Id, iid, location, Tid);
					System.out.println("number of Rooms at this location:"+numRooms);
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 13: //querying Customer Information
			    if (arguments.size() != 4) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Querying Customer information using id: "+arguments.elementAt(1));
			    System.out.println("Customer id: "+arguments.elementAt(2));
			    System.out.println("Transaction ID: "+arguments.elementAt(3));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					int customer = client.getInt(arguments.elementAt(2));
					Tid = client.getInt(arguments.elementAt(3));
					String bill = mw.queryCustomerInfo(Id, customer, Tid);
					System.out.println("Customer info:" + bill);
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;		       
			    
			case 14: //querying a flight Price
			    if (arguments.size() != 4) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Querying a flight Price using id: "+arguments.elementAt(1));
			    System.out.println("Flight number: "+arguments.elementAt(2));
			    System.out.println("Transaction ID: "+arguments.elementAt(3));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					flightNum = client.getInt(arguments.elementAt(2));
					Tid = client.getInt(arguments.elementAt(3));
					price = mw.queryFlightPrice(Id, flightNum, Tid);
					System.out.println("Price of a seat:" + price);
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 15: //querying a Car Price
			    if (arguments.size() != 5) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Querying a car price using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Car location: "+arguments.elementAt(3));
			    System.out.println("Transaction ID: "+arguments.elementAt(4));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					Tid = client.getInt(arguments.elementAt(4));
					price=mw.queryCarsPrice(Id,iid,location,Tid);
					System.out.println("Price of a car at this location:"+price);
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }			    
			    break;

			case 16: //querying a Room price
			    if (arguments.size() != 5) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Querying a room price using id: " + arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Room Location: " + arguments.elementAt(3));
			    System.out.println("Transaction ID: "+arguments.elementAt(4));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					location = client.getString(arguments.elementAt(3));
					Tid = client.getInt(arguments.elementAt(4));
					price=mw.queryRoomsPrice(Id,iid,location,Tid);
					System.out.println("Price of Rooms at this location:"+price);
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 17:  //reserve a flight
			    if(arguments.size() != 5){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Reserving a seat on a flight using id: "+arguments.elementAt(1));
			    System.out.println("Customer id: "+arguments.elementAt(2));
			    System.out.println("Flight number: "+arguments.elementAt(3));
			    System.out.println("Transaction ID: "+arguments.elementAt(4));
			    try{
					Id = client.getInt(arguments.elementAt(1));
					int customer = client.getInt(arguments.elementAt(2));
					flightNum = client.getInt(arguments.elementAt(3));
					Tid = client.getInt(arguments.elementAt(4));
					if(mw.reserveFlight(Id,customer,flightNum,Tid))
				    	System.out.println("Flight Reserved");
					else
				    	System.out.println("Flight could not be reserved.");
			    }
			    catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 18:  //reserve a car
			    if (arguments.size() != 6) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Reserving a car at a location using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Customer id: "+arguments.elementAt(3));
			    System.out.println("Location: "+arguments.elementAt(4));
			    System.out.println("Transaction ID: "+arguments.elementAt(5));
			    try {
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					int customer = client.getInt(arguments.elementAt(3));
					location = client.getString(arguments.elementAt(4));
					Tid = client.getInt(arguments.elementAt(5));
					if (mw.reserveCar(Id,iid,customer,location,Tid))
					    System.out.println("Car Reserved");
					else
					    System.out.println("Car could not be reserved.");
			    } catch (Exception e) {
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 19:  //reserve a room
			    if(arguments.size() != 6){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Reserving a room at a location using id: "+arguments.elementAt(1));
			    System.out.println("IID: " + arguments.elementAt(2));
			    System.out.println("Customer id: "+arguments.elementAt(3));
			    System.out.println("Location: "+arguments.elementAt(4));
			    System.out.println("Transaction ID: "+arguments.elementAt(5));
			    try{
					Id = client.getInt(arguments.elementAt(1));
					iid = client.getInt(arguments.elementAt(2));
					int customer = client.getInt(arguments.elementAt(3));
					location = client.getString(arguments.elementAt(4));
					Tid = client.getInt(arguments.elementAt(5));
				if(mw.reserveRoom(Id,iid,customer,location,Tid))
				    System.out.println("Room Reserved");
				else
				    System.out.println("Room could not be reserved.");
			    }
			    catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			case 20:  //reserve an Itinerary
			    if (arguments.size() < 10) {
					client.wrongNumber();
					break;
			    }
			    System.out.println("Reserving an Itinerary using id:" + arguments.elementAt(1));
			    System.out.println("Customer id:" + arguments.elementAt(2));
			    System.out.println("IID of Car:" + arguments.elementAt(3));
			    System.out.println("IID of Room:" + arguments.elementAt(4));

			    
			    for (int i=0 ; i < arguments.size() - 7; i++) {
					System.out.println("Flight number" + arguments.elementAt(5+i));
				}
				
			    System.out.println("Location for Car/Room booking:" + arguments.elementAt(arguments.size()-4));
			    System.out.println("Car to book?:" + arguments.elementAt(arguments.size()-3));
			    System.out.println("Room to book?:" + arguments.elementAt(arguments.size()-2));
			    System.out.println("Transaction ID: "+arguments.elementAt(arguments.size()-1));

			    try {
					Id = client.getInt(arguments.elementAt(1));
					int customer = client.getInt(arguments.elementAt(2));
					iidCar = client.getInt(arguments.elementAt(3));
					iidRoom = client.getInt(arguments.elementAt(4));
					Vector<Integer> flightNumbers = new Vector<Integer>();
					for (int i = 0; i < arguments.size() - 7; i++) {
					    flightNumbers.addElement(new Integer(client.getInt(arguments.elementAt(5+i))));
					}
					
					location = client.getString(arguments.elementAt(arguments.size() - 4));
					car = client.getBoolean(arguments.elementAt(arguments.size() - 3));
					room = client.getBoolean(arguments.elementAt(arguments.size() - 2));
					Tid = client.getInt(arguments.elementAt(arguments.size()-1));

					if (mw.itinerary(Id, customer, iidCar, iidRoom, flightNumbers, location, car, room, Tid))
					    System.out.println("Itinerary Reserved");
					else
					    System.out.println("Itinerary could not be reserved.");
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
			    System.exit(1);
			    
			    
			case 22:  //new Customer given id
			    if(arguments.size()!=4){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Adding a new Customer using id:"+arguments.elementAt(1) + " and cid " +arguments.elementAt(2) + " in transaction " + arguments.elementAt(3));
			    try{
					Id = client.getInt(arguments.elementAt(1));
					Cid = client.getInt(arguments.elementAt(2));
					Tid = client.getInt(arguments.elementAt(3));
					boolean customer=mw.newCustomer(Id,Cid,Tid);
					System.out.println("new customer id:"+Cid);
			    }
			    catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;

			case 23:  //Start
			    if(arguments.size() != 1){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Starting a new transaction...");
			    try{
					Tid=mw.start();
					System.out.println("New transaction ID: "+ Tid);
			    }
			    catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;

			case 24:  //Commit
			    if(arguments.size() != 2){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Commiting transaction with id: " + arguments.elementAt(1));
			    try{
			    	Tid = client.getInt(arguments.elementAt(1));

					boolean successful = mw.commit(Tid);
					if (successful) {
						System.out.println("Transaction successfully committed.");
					} else {
						System.out.println("Problems committing transaction.");
					}
			    }
			    catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;

			case 25:  //Abort
			    if(arguments.size() != 2){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Aborting transaction with id: " + arguments.elementAt(1));
			    try{
			    	Tid = client.getInt(arguments.elementAt(1));

					mw.abort(Tid);
					System.out.println("Transaction successfully aborted.");
			    }
			    catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;

			case 26:  //Shutdown
			    if(arguments.size() != 1){
					client.wrongNumber();
					break;
			    }
			    System.out.println("Shutting down all systems...");
			    try{
					boolean successful = mw.shutdown();
					
					if (successful) {
						System.out.println("Successfully shut down all systems.");
						System.out.println("Quitting client.");
						System.exit(0);
					}
			    }
			    catch(Exception e){
					System.out.println("EXCEPTION:");
					System.out.println(e.getMessage());
					e.printStackTrace();
			    }
			    break;
			    
			default:
			    System.out.println("The interface does not support this command.");
			    break;
			}//end of switch
	    }//end of while(true)
	}
	    
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
		else if (argument.compareToIgnoreCase("start")==0)
		    return 23;
		else if (argument.compareToIgnoreCase("commit")==0)
		    return 24;
		else if (argument.compareToIgnoreCase("abort")==0)
		    return 25;
		else if (argument.compareToIgnoreCase("shutdown")==0)
		    return 26;
		else
		    return 666;
    }

    public void listCommands() {
		System.out.println("\nWelcome to the client interface provided to test your project.");
		System.out.println("Commands accepted by the interface are:");
		System.out.println("help");
		System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcusomterid\ndeleteflight\ndeletecar\ndeleteroom");
		System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
		System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
		System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
		System.out.println("nquit");
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
			System.out.println("\tdeletecar,<id>,<location>,<numCars>");
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

		    case 17:  //reserve a flight
			System.out.println("Reserving a flight.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a flight for a customer.");
			System.out.println("\nUsage:");
			System.out.println("\treserveflight,<id>,<customerid>,<flightnumber>");
			break;
			
		    case 18:  //reserve a car
			System.out.println("Reserving a Car.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a given number of cars for a customer at a particular location.");
			System.out.println("\nUsage:");
			System.out.println("\treservecar,<id>,<customerid>,<location>,<nummberofCars>");
			break;
			
		    case 19:  //reserve a room
			System.out.println("Reserving a Room.");
			System.out.println("Purpose:");
			System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
			System.out.println("\nUsage:");
			System.out.println("\treserveroom,<id>,<customerid>,<location>,<nummberofRooms>");
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

			case 23:  //start
			System.out.println("Start a new transaction.");
			System.out.println("Purpose:");
			System.out.println("\tStarts a new transaction");
			System.out.println("\nUsage:");
			System.out.println("\tstart");
			break;

			case 24:  //commit
			System.out.println("Commit a transaction providing an id.");
			System.out.println("Purpose:");
			System.out.println("\tCommits the transaction with the id provided.");
			System.out.println("\nUsage:");
			System.out.println("\tcommit, <id>");
			break;

			case 25:  //abort
			System.out.println("Abort a transaction providing an id.");
			System.out.println("Purpose:");
			System.out.println("\tAborts the transaction with the id provided.");
			System.out.println("\nUsage:");
			System.out.println("\tabort, <id>");
			break;

			case 26:  //shutdown
			System.out.println("Shut down all systems");
			System.out.println("Purpose:");
			System.out.println("\tShuts down all RMs, Middleware, and client.");
			System.out.println("\nUsage:");
			System.out.println("\tshutdown");
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
