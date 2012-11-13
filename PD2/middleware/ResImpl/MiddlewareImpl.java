package ResImpl;

import MidInterface.*;
import ResInterface.*;
import java.util.*;
import java.rmi.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

//public class MiddlewareImpl extends java.rmi.server.UnicastRemoteObject
public class MiddlewareImpl implements Middleware {

	protected RMHashtable m_itemHT = new RMHashtable();
    static TransactionManager transactionManager = null;
    static LockManager lockManager = null;
    static ResourceManager rmCar = null;
	static ResourceManager rmFlight = null;
	static ResourceManager rmHotel = null;
    Hashtable<Integer, ArrayList<Object>> customerHistories = new Hashtable<Integer, ArrayList<Object>>();

	public MiddlewareImpl() throws RemoteException {

	}

	public static void main(String[] args) {

		String server = "localhost";
        int portRM = 9876;
        int portClient = 8765;

		if (args.length == 1) {
             server = args[0];
         } else if (args.length != 0 &&  args.length != 1) {
             System.err.println ("Wrong usage");
             System.out.println("Usage: java MidImpl.MiddlewareImpl [port]");
             System.exit(1);
         }

         try {
         	//create a new Middleware object
         	MiddlewareImpl mwObj = new MiddlewareImpl();
         	//dynamically generate stub
         	Middleware mw = (Middleware) UnicastRemoteObject.exportObject(mwObj, 0);

         	Registry registryRM = LocateRegistry.getRegistry(server, portRM);
            Registry registryClient = LocateRegistry.getRegistry(portClient);
         	//bind middleware
         	registryClient.rebind("HAL9001Middleware", mw);
            System.out.println("Middleware ready");
            
            
         	//locate RMs
         	rmCar = (ResourceManager) registryRM.lookup("HAL9001CarResourceManager");
         	rmFlight = (ResourceManager) registryRM.lookup("HAL9001FlightResourceManager");
         	rmHotel = (ResourceManager) registryRM.lookup("HAL9001RoomResourceManager");

         	if (rmCar == null || rmFlight == null || rmHotel == null) {
         		System.out.println("RM lookups unsuccessful");
         	} else {
         		System.out.println("RM lookups successful");
         		System.out.println("Connected to RMs");
         	}

            lockManager = new LockManager();
            transactionManager = new TransactionManager(server, portRM, mwObj, lockManager);
            

         } catch (Exception e) {

         	System.err.println("Middleware exception: " + e.toString());
         	e.printStackTrace();

         }

	}

    // Reads a data item
    private RMItem readData(int id, String key)
    {
        synchronized(m_itemHT){
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData(int id, String key, RMItem value)
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
        }
    }

	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice, int xid) 
	throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(flightNum), LockManager.WRITE)) {
                transactionManager.enlist(TransactionManager.FLIGHT, xid);
                if (rmFlight.addFlight(id, flightNum, flightSeats, flightPrice, xid)) {
                    System.out.println("Flight added");
                    return true;
                } else {
                    System.out.println("Flight not added");
                    return false;
                }
            } else {
                System.out.println("Flight not added, lock already held");
                return false;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

	} 

    public boolean addCars(int id, int iid, String location, int numCars, int price, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.WRITE)) {
                transactionManager.enlist(TransactionManager.CAR, xid);
                if (rmCar.addCars(id, iid, location, numCars, price, xid)) {
                    transactionManager.updateTTL(xid);
                    System.out.println("Car added");
                    return true;
                } else {
                    System.out.println("Car not added");
                    return false;
                }
            } else {
                System.out.println("Cars not added, lock already held");
                return false;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    } 
   

    public boolean addRooms(int id, int iid, String location, int numRooms, int price, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.WRITE)) {
                transactionManager.enlist(TransactionManager.ROOM, xid);
                if (rmHotel.addRooms(id, iid, location, numRooms, price, xid)) {
                    transactionManager.updateTTL(xid);
                    System.out.println("Room added");
                    return true;
                } else {
                    System.out.println("Room not added");
                    return false;
                }
            } else {
                System.out.println("Rooms not added, lock already held");
                return false;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    } 			    
		

    public int newCustomer(int id, int xid) 
    throws RemoteException, NumberFormatException {
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                                    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                    String.valueOf( Math.round( Math.random() * 100 + 1 )));
        
        boolean lockValue = false;
		try {
			lockValue = lockManager.Lock(id, String.valueOf(cid), LockManager.WRITE);
		} catch (DeadlockException e) {
			e.printStackTrace();
		}
        
        if (lockValue) {
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            Trace.info("INFO: RM::newCustomer(" + id + ") called" );
            Customer cust = new Customer(cid);
            writeData(id, cust.getKey(), cust);
            transactionManager.updateTTL(xid);
            ArrayList<Object> customerHistory = customerHistories.get(new Integer(xid));
            customerHistory.add(new Integer(cid));
            customerHistories.put(new Integer(xid), customerHistory);
            Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
            return cid;
        } else {
            System.out.println("Customer not added, lock already held.");
            return 0;
        }
    }
    

    public boolean newCustomer(int id, int cid, int xid) 
    throws RemoteException {
    	
    	boolean lockValue = false;
		try {
			lockValue = lockManager.Lock(id, String.valueOf(cid), LockManager.WRITE);
		} catch (DeadlockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        if (lockValue) {
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") called" );
            Customer cust = (Customer) readData( id, Customer.getKey(cid) );
            if( cust == null ) {
                cust = new Customer(cid);
                writeData( id, cust.getKey(), cust );
                transactionManager.updateTTL(xid);
                ArrayList<Object> customerHistory = customerHistories.get(new Integer(xid));
                customerHistory.add(new Integer(cid));
                customerHistories.put(new Integer(xid), customerHistory);
                Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") created a new customer" );
                return true;
            } else {
                Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") failed--customer already exists");
                return false;
            }
        } else {
            System.out.println("Customer not added, lock already held.");
            return false;
        }
    }


    public boolean deleteFlight(int id, int flightNum, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(flightNum), LockManager.WRITE)) {
                transactionManager.enlist(TransactionManager.FLIGHT, xid);
                if (rmFlight.deleteFlight(id, flightNum, xid)) {
                    transactionManager.updateTTL(xid);
                    System.out.println("Flight deleted");
                    return true;
                } else {
                    System.out.println("Flight not deleted");
                    return false;
                }
            } else {
                System.out.println("Flight not deleted, lock already held.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    

    public boolean deleteCars(int id, int iid, String location, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.WRITE)) {
                transactionManager.enlist(TransactionManager.CAR, xid);
                if (rmCar.deleteCars(id, iid, location, xid)) {
                    transactionManager.updateTTL(xid);
                    System.out.println("Cars deleted");
                    return true;
                } else {
                    System.out.println("Cars not deleted");
                    return false;
                }
            } else {
                System.out.println("Cars not deleted, lock already held.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    }

 
    public boolean deleteRooms(int id, int iid, String location, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.WRITE)) {
                transactionManager.enlist(TransactionManager.ROOM, xid);
                if (rmHotel.deleteRooms(id, iid, location, xid)) {
                    transactionManager.updateTTL(xid);
                    System.out.println("Rooms deleted");
                    return true;
                } else {
                    System.out.println("Rooms not deleted");
                    return false;
                }
            } else {
                System.out.println("Rooms not deleted, lock already held.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    }
    

    public boolean deleteCustomer(int id, int customerID, int xid) 
    throws RemoteException {
    	
    	boolean lockValue = false;
		try {
			lockValue = lockManager.Lock(id, String.valueOf(customerID), LockManager.WRITE);
		} catch (DeadlockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        if (lockValue) {
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            RMItem item = removeData(id, Customer.getKey(customerID));
            transactionManager.updateTTL(xid);
            ArrayList<Object> customerHistory = customerHistories.get(new Integer(xid));
            customerHistory.add((Customer)item);
            customerHistories.put(new Integer(xid), customerHistory);
            return true;
        } else {
            System.out.println("Customer not deleted, lock already held.");
            return false;
        }
    }


    public int queryFlight(int id, int flightNumber, int xid) 
    throws RemoteException {

        try { 
            if (lockManager.Lock(id, String.valueOf(flightNumber), LockManager.READ)) {
                transactionManager.enlist(TransactionManager.FLIGHT, xid);
                int flightNum = rmFlight.queryFlight(id, flightNumber, xid);
                transactionManager.updateTTL(xid);
                return flightNum;
            } else {
                System.out.println("Flight not queried, write lock already held.");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public int queryCars(int id, int iid, String location, int xid) 
    throws RemoteException {

        try { 
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.READ)) {
                transactionManager.enlist(TransactionManager.CAR, xid);
                int numCars = rmCar.queryCars(id, iid, location, xid);
                transactionManager.updateTTL(xid);
                return numCars;
            } else {
                System.out.println("Cars not queried, write lock already held.");
                return 0;
            }

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    }


    public int queryRooms(int id, int iid, String location, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.READ)) {
                transactionManager.enlist(TransactionManager.ROOM, xid);
                int numRooms = rmHotel.queryRooms(id, iid, location, xid);
                transactionManager.updateTTL(xid);
                return numRooms;
            } else {
                System.out.println("Rooms not queried, write lock already held.");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public String queryCustomerInfo(int id, int customerID, int xid) 
    throws RemoteException {
    	
    	boolean lockValue = false;
		try {
			lockValue = lockManager.Lock(id, String.valueOf(customerID), LockManager.READ);
		} catch (DeadlockException e) {
			e.printStackTrace();
		}
    	
        if (lockValue) {
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
            Customer cust = (Customer) readData(id, Customer.getKey(customerID));
            transactionManager.updateTTL(xid);
            if(cust == null) {
                Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
            } else {
                    String s = cust.printBill();
                    Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                    System.out.println( s );
                    return s;
            }
        } else {
            System.out.println("Customer not queried, write lock already held.");
            return "";
        }
    } 
    

    public int queryFlightPrice(int id, int flightNumber, int xid) 
    throws RemoteException {

        try { 
            if (lockManager.Lock(id, String.valueOf(flightNumber), LockManager.READ)) {
                transactionManager.enlist(TransactionManager.FLIGHT, xid);
                int flightPrice = rmFlight.queryFlightPrice(id, flightNumber, xid);
                transactionManager.updateTTL(xid);
                return flightPrice;
            } else {
                System.out.println("Flight price not queried, write lock already held.");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public int queryCarsPrice(int id, int iid, String location, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.READ)) {
                transactionManager.enlist(TransactionManager.CAR, xid);
                int carsPrice = rmCar.queryCarsPrice(id, iid, location, xid);
                transactionManager.updateTTL(xid);
                return carsPrice;
            } else {
                System.out.println("Cars price not queried, write lock already held.");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public int queryRoomsPrice(int id, int iid, String location, int xid) 
    throws RemoteException {

        try {
            if (lockManager.Lock(id, String.valueOf(iid), LockManager.READ)) {
                transactionManager.enlist(TransactionManager.ROOM, xid);
                int roomsPrice = rmHotel.queryRoomsPrice(id, iid, location, xid);
                transactionManager.updateTTL(xid);
                return roomsPrice;
            } else {
                System.out.println("Rooms price not queried, write lock already held.");
                return 0;
            }
        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public boolean reserveFlight(int id, int customerID, int flightNumber, int xid) 
    throws RemoteException {
        boolean lockedCustomer = false;
        boolean lockedFlight = false;
        try {
            lockedCustomer = lockManager.Lock(id, String.valueOf(customerID), LockManager.WRITE);
            lockedFlight = lockManager.Lock(id, String.valueOf(flightNumber), LockManager.WRITE);
        } catch (DeadlockException e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        if (lockedCustomer) {
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            Trace.info("RM::reserveFlight(" + id + ", " + customerID + ") called" );
            Customer cust = (Customer) readData(id, Customer.getKey(customerID));

            if (cust == null) {
                Trace.warn("RM::reserveFlight(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return false;
            } else {
                    try {
                        if (lockedFlight) {
                            transactionManager.enlist(TransactionManager.FLIGHT, xid);
                            Customer newCust = rmFlight.reserveFlight(id, cust, flightNumber, xid);
                            transactionManager.updateTTL(xid);
                            if (newCust != null) {
                                writeData(id, newCust.getKey(), newCust);
                                ArrayList<Object> customerHistory = customerHistories.get(new Integer(xid));
                                customerHistory.add(cust);
                                customerHistories.put(new Integer(xid), customerHistory);
                                return true;
                            } else {
                                System.out.println("Failed to reserve the flight.");
                                return false;
                            }
                        } else {
                            System.out.println("Flight not reserved, lock already held on flight.");
                            return false;
                        }
                    } catch (Exception e) {
                        System.out.println("MW EXCEPTION:");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    return false;
            }
        } else {
            System.out.println("Flight not reserved, lock already held on customer.");
            return false;
        }
    } 


    public boolean reserveCar(int id, int iid, int customerID, String location, int xid) 
    throws RemoteException {
        boolean lockedCustomer = false;
        boolean lockedCar = false;
        try {
            lockedCustomer = lockManager.Lock(id, String.valueOf(customerID), LockManager.WRITE);
            lockedCar = lockManager.Lock(id, String.valueOf(iid), LockManager.WRITE);
        } catch (DeadlockException e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        if (lockedCustomer) {
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            Trace.info("RM::reserveCar(" + id + ", " + customerID + ") called" );
            Customer cust = (Customer) readData(id, Customer.getKey(customerID));

            if (cust == null) {
                Trace.warn("RM::reserveCar(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return false;
            } else {
                    try {
                        if (lockedCar) {
                            transactionManager.enlist(TransactionManager.CAR, xid);
                            Customer newCust = rmCar.reserveCar(id, iid, cust, location, xid);
                            transactionManager.updateTTL(xid);
                            if (newCust != null) {
                                writeData(id, newCust.getKey(), newCust);
                                ArrayList<Object> customerHistory = customerHistories.get(new Integer(xid));
                                customerHistory.add(cust);
                                customerHistories.put(new Integer(xid), customerHistory);
                                return true;
                            } else {
                                System.out.println("Failed to reserve the car.");
                                return false;
                            }
                        } else {
                            System.out.println("Flight not reserved, lock already held on car.");
                            return false;
                        }
                    } catch (Exception e) {
                        System.out.println("MW EXCEPTION:");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    return false;
            }
        } else {
            System.out.println("Car not reserved, lock already held on customer.");
            return false;
        }
    } 


    public boolean reserveRoom(int id, int iid, int customerID, String location, int xid) 
    throws RemoteException {
        boolean lockedCustomer = false;
        boolean lockedRoom = false;
        try {
            lockedCustomer = lockManager.Lock(id, String.valueOf(customerID), LockManager.WRITE);
            lockedRoom = lockManager.Lock(id, String.valueOf(iid), LockManager.WRITE);
        } catch (DeadlockException e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        if (lockedCustomer) {
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            Trace.info("RM::reserveRoom(" + id + ", " + customerID + ") called" );
            Customer cust = (Customer) readData(id, Customer.getKey(customerID));

            if (cust == null) {
                Trace.warn("RM::reserveRoom(" + id + ", " + customerID + ") failed--customer doesn't exist" );
                return false;
            } else {
                    try {
                        if (lockedRoom) {
                            transactionManager.enlist(TransactionManager.ROOM, xid);
                            Customer newCust = rmHotel.reserveRoom(id, iid, cust, location, xid);
                            transactionManager.updateTTL(xid);
                            if (newCust != null) {
                                writeData(id, newCust.getKey(), newCust);
                                ArrayList<Object> customerHistory = customerHistories.get(new Integer(xid));
                                customerHistory.add(cust);
                                customerHistories.put(new Integer(xid), customerHistory);
                                return true;
                            } else {
                                System.out.println("Failed to reserve the room.");
                                return false;
                            }
                        } else {
                            System.out.println("Flight not reserved, lock already held on car.");
                            return false;
                        }
                    } catch (Exception e) {
                        System.out.println("MW EXCEPTION:");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    return false;
            }
        } else {
            System.out.println("Room not reserved, lock already held on customer.");
            return false;
        }
    } 

    public boolean itinerary(int id, int customerID, int iidCar, int iidRoom, Vector<Integer> flightNumbers, String location, boolean car, boolean room, int xid) 
    throws RemoteException {
        try {
            if (!lockManager.Lock(id, String.valueOf(customerID), LockManager.WRITE)) {
                System.out.println("Itinerary not reserved, lock already held on customer.");
                return false;
            }
            transactionManager.enlist(TransactionManager.CUSTOMER, xid);
            if (!lockManager.Lock(id, String.valueOf(iidCar), LockManager.WRITE)) {
                System.out.println("Itinerary not reserved, lock already held on car.");
                return false;
            }
            transactionManager.enlist(TransactionManager.CAR, xid);
            if (!lockManager.Lock(id, String.valueOf(iidRoom), LockManager.WRITE)) {
                System.out.println("Itinerary not reserved, lock already held on room.");
                return false;
            }
            transactionManager.enlist(TransactionManager.ROOM, xid);
            for (Integer integer : flightNumbers) {
                if (!lockManager.Lock(id, String.valueOf(integer), LockManager.WRITE)) {
                    System.out.println("Itinerary not reserved, lock already held on flight " + String.valueOf(integer) + ".");
                    return false;
                }
                transactionManager.enlist(TransactionManager.FLIGHT, xid);
            }
        } catch (DeadlockException e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        

        Trace.info("RM::reserveItinerary(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));
        Customer originalCust = (Customer) readData(id, Customer.getKey(customerID));
        if (cust == null) {
            Trace.warn("RM::reserveItinerary(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
                try {
                    //reserve flights
                    for (Integer integer : flightNumbers) {
                        cust = rmFlight.reserveFlight(id, cust, integer.intValue(), xid);
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to create the flights for the itinerary.");
                        }
                    }
                    //reserve a car
                    if (car) {
                        cust = (Customer) readData(id, Customer.getKey(customerID));
                        cust = rmCar.reserveCar(id, iidCar, cust, location, xid);
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to reserve the car for the itinerary.");
                        }
                    }

                    //reserve a room
                    if (room) {
                        cust = (Customer) readData(id, Customer.getKey(customerID));
                        cust = rmHotel.reserveRoom(id, iidRoom, cust, location, xid);
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to reserve the room for the itinerary.");
                        }
                    }
                    transactionManager.updateTTL(xid);
                    ArrayList<Object> customerHistory = customerHistories.get(new Integer(xid));
                    customerHistory.add(originalCust);
                    customerHistories.put(new Integer(xid), customerHistory);
                    return true;

                } catch (Exception e) {
                    System.out.println("MW EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                return false;
        }
    }

    public int start() throws RemoteException {
        return transactionManager.start();
    }

    public boolean commit(int xid)
    throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return transactionManager.commit(xid);
    }

    public void abort(int xid)
    throws RemoteException, InvalidTransactionException {
        transactionManager.abort(xid);
    }

    public boolean shutdown()
    throws RemoteException {
        boolean successful = rmCar.shutdown() && rmHotel.shutdown() && rmFlight.shutdown();
        System.exit(1);
        return successful;
    }

    public boolean commitCustomers(int xid) {
        if (customerHistories.containsKey(new Integer(xid))) {
            customerHistories.remove(new Integer(xid));
        }
        return true;
    }

    public void abortCustomers(int xid) {
        if (customerHistories.containsKey(new Integer(xid))) {
            ArrayList<Object> customerHistory = customerHistories.remove(new Integer(xid));
            for (int i = customerHistory.size() - 1; i >= 0; i--) {
                if (customerHistory.get(i) instanceof Integer) {
                    int addedCustomer = ((Integer) customerHistory.get(i)).intValue();
                    removeData(0, String.valueOf(addedCustomer));
                } else {
                    Customer oldCustomer = (Customer)(customerHistory.get(i));
                    writeData(0, oldCustomer.getKey(), oldCustomer);
                }
            }
        }
    }
}
