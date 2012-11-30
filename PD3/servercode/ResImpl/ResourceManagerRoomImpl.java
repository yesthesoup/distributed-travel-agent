// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package ResImpl;

import ResInterface.*;

import java.util.*;
import java.rmi.*;
import java.io.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

//public class ResourceManagerImpl extends java.rmi.server.UnicastRemoteObject
public class ResourceManagerRoomImpl
	implements ResourceManager {
	
	protected RMHashtable m_itemHT = new RMHashtable();
	Hashtable<Integer, ArrayList<Object>> roomHistories = new Hashtable<Integer, ArrayList<Object>>();
	Hashtable<Integer, Long> transactionRecords = new Hashtable<Integer, Long>();
	private static String historyFilepath = "roomHistories.txt";
	private static String masterFilepath = "roomMaster.txt";
	private static String configFilepath = "configHotel.txt";
	private static boolean crashAfterVoteRequest = false;
	private static boolean crashAfterDecision = false;

	public static void main(String args[]) {
        // Figure out where server is running
        String server = "localhost";

         if (args.length == 1) {
             server = server + ":" + args[0];
         } else if (args.length != 0 &&  args.length != 1) {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
             System.exit(1);
         }
	 
	 try 
	     {
		 // create a new Server object
		 ResourceManagerRoomImpl obj = new ResourceManagerRoomImpl();
		 // dynamically generate the stub (client proxy)
		 ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
		 
		 // Bind the remote object's stub in the registry
		 Registry registry = LocateRegistry.getRegistry(9876);
		 registry.rebind("HAL9001RoomResourceManager", rm);


		 try {
     		FileInputStream cStream = new FileInputStream(configFilepath);
     		DataInputStream cData = new DataInputStream(cStream);
     		BufferedReader cReader = new BufferedReader(new InputStreamReader(cData));
     		String line;
     		String delims = "[:]";
     		String[] tokens;
     		while ((line = cReader.readLine()) != null) {
     			tokens = line.split(delims);
     			if (tokens[0] == "crashAfterVoteRequest") {
     				if (tokens[1] == "1") {
     					crashAfterVoteRequest = true;
     				}
     			} else if (tokens[0] == "crashAfterDecision") {
     				if (tokens[1] == "1") {
     					crashAfterDecision = true;
     				}
     			}
     		}
     		cData.close();
     	} catch (Exception e) {
     		e.printStackTrace();
     	}

		 try {
			FileInputStream masterFileIn = new FileInputStream(masterFilepath);
			ObjectInputStream masterDataIn = new ObjectInputStream(masterFileIn);
			obj.m_itemHT = (RMHashtable)masterDataIn.readObject();
			masterDataIn.close();
			masterFileIn.close();

			FileInputStream historyFileIn = new FileInputStream(historyFilepath);
			ObjectInputStream historyDataIn = new ObjectInputStream(historyFileIn);
			obj.roomHistories = (Hashtable<Integer, ArrayList<Object>>)historyDataIn.readObject();
			historyDataIn.close();
			historyFileIn.close();
			System.out.println("Recovered state from shadow files.");
		} catch (FileNotFoundException e) {
			System.out.println("No recovery data found. Creating fresh RM");
		}
		 
		 System.err.println("Server ready");
	     } 
	 catch (Exception e) 
	     {
		 System.err.println("Server exception: " + e.toString());
		 e.printStackTrace();
	     }
	 /*
         // Create and install a security manager
         if (System.getSecurityManager() == null) {
	     System.setSecurityManager(new RMISecurityManager());
         }*/
	} 
	 
	 public ResourceManagerRoomImpl() throws RemoteException {
	 	TransactionExpiryThread TEThread = new TransactionExpiryThread(300000, 1000, this);
     	TEThread.start();
	 }
	 
	private void shadowHistory() {
		try {
			FileOutputStream historyFileOut = new FileOutputStream(historyFilepath);
			ObjectOutputStream historyDataOut = new ObjectOutputStream(historyFileOut);
			historyDataOut.writeObject(roomHistories);
			historyDataOut.close();
			historyFileOut.close();
			System.out.println("History shadow file written.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void shadowMaster() {
		try {
			FileOutputStream masterFileOut = new FileOutputStream(masterFilepath);
			ObjectOutputStream masterDataOut = new ObjectOutputStream(masterFileOut);
			masterDataOut.writeObject(m_itemHT);
			masterDataOut.close();
			masterFileOut.close();
			System.out.println("Master shadow file written.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void shadowData() {
		shadowMaster();
		shadowHistory();
	}

	// Reads a data item
	private RMItem readData( int id, String key )
	{
		synchronized(m_itemHT){
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	private void writeData( int id, String key, RMItem value )
	{
		synchronized(m_itemHT){
			m_itemHT.put(key, value);
		}
	}
	
	// Remove the item out of storage
	protected RMItem removeData(int id, String key){
		synchronized(m_itemHT){
			return (RMItem)m_itemHT.remove(key);
		}
	}
	
	
	// deletes the entire item
	protected boolean deleteItem(int id, String key)
	{
		Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key );
		// Check if there is such an item in the storage
		if( curObj == null ) {
			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
			return false;
		} else {
			if(curObj.getReserved()==0){
				removeData(id, curObj.getKey());
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
				return true;
			}
			else{
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
				return false;
			}
		} // if
	}
	

	// query the number of available seats/rooms/cars
	protected int queryNum(int id, String key) {
		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0;  
		if( curObj != null ) {
			value = curObj.getCount();
		} // else
		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
		return value;
	}	
	
	// query the price of an item
	protected int queryPrice(int id, String key){
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0; 
		if( curObj != null ) {
			value = curObj.getPrice();
		} // else
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;		
	}
	
	// reserve an item
	protected synchronized Customer reserveItem(int id, Customer cust, String key, String location){
		int customerID = -1;
		if( cust == null ) {
			
			Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return null;
		} 
		customerID = cust.getID();
		// check if the item is available
		ReservableItem item = (ReservableItem)readData(id, key);
		if(item==null){
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
			return null;
		}else if(item.getCount()==0){
			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
			return null;
		}else{			
			cust.reserve( key, location, item.getPrice());
			
			// decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved()+1);
			
			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			return cust;
		}		
	}
	
	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice, int xid)
		throws RemoteException
	{
		return false;
	}


	
	public boolean deleteFlight(int id, int flightNum, int xid)
		throws RemoteException
	{
		return false;
	}



	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, int iid, String location, int count, int price, int xid)
		throws RemoteException
	{
		updateTTL(xid);
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
		Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
		if( curObj == null ) {
			// doesn't exist...add it
			Hotel newObj = new Hotel( location, count, price );
			writeData( id, newObj.getKey(), newObj );
			ArrayList<Object> roomHistory = roomHistories.get(new Integer(xid));
            roomHistory.add(location);
            roomHistories.put(new Integer(xid), roomHistory);
			Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
		} else {
			ArrayList<Object> roomHistory = roomHistories.get(new Integer(xid));
            roomHistory.add(curObj.clone());
            roomHistories.put(new Integer(xid), roomHistory);
			// add count to existing object and update price...
			curObj.setCount( curObj.getCount() + count );
			if( price > 0 ) {
				curObj.setPrice( price );
			} // if
			writeData( id, curObj.getKey(), curObj );
			Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
		} // else
		shadowData();
		return(true);
	}

	// Delete rooms from a location
	public boolean deleteRooms(int id, int iid, String location, int xid)
		throws RemoteException
	{
		updateTTL(xid);
		ArrayList<Object> roomHistory = roomHistories.get(new Integer(xid));
        roomHistory.add(((Hotel)readData( id, Hotel.getKey(location) )).clone());
        roomHistories.put(new Integer(xid), roomHistory);
		boolean del = deleteItem(id, Hotel.getKey(location));
		shadowData();
		return del;
		
	}

	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, int iid, String location, int count, int price, int xid)
		throws RemoteException
	{
		return false;
	}


	// Delete cars from a location
	public boolean deleteCars(int id, int iid, String location, int xid)
		throws RemoteException
	{
		return false;
	}



	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum, int xid)
		throws RemoteException
	{
		return 0;
	}

	// Returns the number of reservations for this flight. 
//	public int queryFlightReservations(int id, int flightNum)
//		throws RemoteException
//	{
//		Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
//		RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
//		if( numReservations == null ) {
//			numReservations = new RMInteger(0);
//		} // if
//		Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
//		return numReservations.getValue();
//	}


	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum, int xid)
		throws RemoteException
	{
		return 0;
	}


	// Returns the number of rooms available at a location
	public int queryRooms(int id, int iid, String location, int xid)
		throws RemoteException
	{
		updateTTL(xid);
		return queryNum(id, Hotel.getKey(location));
	}


	
	
	// Returns room price at this location
	public int queryRoomsPrice(int id, int iid, String location, int xid)
		throws RemoteException
	{
		updateTTL(xid);
		return queryPrice(id, Hotel.getKey(location));
	}


	// Returns the number of cars available at a location
	public int queryCars(int id, int iid, String location, int xid)
		throws RemoteException
	{
		return 0;
	}


	// Returns price of cars at this location
	public int queryCarsPrice(int id, int iid, String location, int xid)
		throws RemoteException
	{
		return 0;
	}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
		throws RemoteException
	{
		Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return null;
		} else {
			return cust.getReservations();
		} // if
	}

	// return a bill
	public String queryCustomerInfo(int id, int customerID, int xid)
		throws RemoteException
	{
		return "";
	}

  // customer functions
  // new customer just returns a unique customer identifier
	
  public int newCustomer(int id, int xid)
		throws RemoteException
	{
		return 0;
	}

	// I opted to pass in customerID instead. This makes testing easier
  public boolean newCustomer(int id, int customerID, int xid)
		throws RemoteException
	{
		return false;
	}


	// Deletes customer from the database. 
	public boolean deleteCustomer(int id, int customerID, int xid)
			throws RemoteException
	{
		return false;
	}




	// Frees flight reservation record. Flight reservation records help us make sure we
	//  don't delete a flight if one or more customers are holding reservations
//	public boolean freeFlightReservation(int id, int flightNum)
//		throws RemoteException
//	{
//		Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
//		RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
//		if( numReservations != null ) {
//			numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
//		} // if
//		writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
//		Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
//				+ numReservations + " reservations" );
//		return true;
//	}
//	

	
	// Adds car reservation to this customer. 
	public Customer reserveCar(int id, int iid, Customer cust, String location, int xid)
		throws RemoteException
	{
		return null;
	}


	// Adds room reservation to this customer. 
	public Customer reserveRoom(int id, int iid, Customer cust, String location, int xid)
		throws RemoteException
	{
		updateTTL(xid);
		ArrayList<Object> roomHistory = roomHistories.get(new Integer(xid));
        roomHistory.add(((Hotel)readData( id, Hotel.getKey(location) )).clone());
        roomHistories.put(new Integer(xid), roomHistory);
		Customer res = reserveItem(id, cust, Hotel.getKey(location), location);
		shadowData();
		return res;
	}
	// Adds flight reservation to this customer.  
	public Customer reserveFlight(int id, Customer cust, int flightNum, int xid)
		throws RemoteException
	{
		return null;
	}
	
	/* reserve an itinerary */
    public boolean itinerary(int id,int customer, int iidCar, int iidRoom, Vector flightNumbers,String location,boolean Car,boolean Room, int xid)
	throws RemoteException {
    	return false;
    }

    public boolean prepare(int xid)
    throws RemoteException {
    	if (crashAfterVoteRequest) {
    		selfDestruct();
    	}
    	if (transactionRecords.containsKey(new Integer(xid))) {
    		System.out.println("Vote YES for " + xid);
    		return true;
    	}
    	try {
    		abort(xid);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	System.out.println("Votes NO for " + xid);
    	return false;
    }

    public void enlist(int xid)
    throws RemoteException {
    	Long currentTime = new Long(System.currentTimeMillis());
		transactionRecords.put(new Integer(xid), currentTime);
		roomHistories.put(new Integer(xid), new ArrayList<Object>());
    }

    public void updateTTL(int xid) {
		Long currentTime = new Long(System.currentTimeMillis());
		transactionRecords.put(new Integer(xid), currentTime);
	}

	public synchronized void clearExpiredTransactions(long newTime, int ttl)
	throws RemoteException {
		for (Integer xid : transactionRecords.keySet()) {
			long rmTime = ((Long) transactionRecords.get(xid)).longValue();
			if ((newTime - rmTime) > ttl) {
				transactionRecords.remove(xid);
				System.out.println("Transaction " + xid.intValue() + " expirted. Aborting.");
				try {
					abort(xid.intValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

    /* commit a transaction */
    public boolean commit(int xid)
    throws RemoteException, TransactionAbortedException, InvalidTransactionException {
    	if (crashAfterDecision) {
    		selfDestruct();
    	}
    	if (roomHistories.containsKey(new Integer(xid))) {
            roomHistories.remove(new Integer(xid));
            shadowHistory();
        }
        System.out.println("Successfully committed " + xid + ".");
        return true;
    }

    /* abort a transaction */
    public void abort(int xid)
    throws RemoteException, InvalidTransactionException {
    	if (crashAfterDecision) {
    		selfDestruct();
    	}
    	if (roomHistories.containsKey(new Integer(xid))) {
            ArrayList<Object> roomHistory = roomHistories.remove(new Integer(xid));
            for (int i = roomHistory.size() - 1; i >= 0; i--) {
                if (roomHistory.get(i) instanceof String) {
                    String addedLocation = (String)roomHistory.get(i);
                    removeData(0, Hotel.getKey(addedLocation));
                } else {
                    Hotel oldRoom = (Hotel)(roomHistory.get(i));
                    writeData(0, oldRoom.getKey(), oldRoom);
                }
            }
            shadowData();
            System.out.println("Successfully aborted " + xid + ".");
        }
    }

    /* shutdown all systems */
    public boolean shutdown()
    throws RemoteException {
    	System.exit(0);
    	return true;
    }

    public void selfDestruct()
    throws RemoteException {
    	System.exit(1);
    }

}
