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
    static ResourceManager rmCar = null;
	static ResourceManager rmFlight = null;
	static ResourceManager rmHotel = null;

	public MiddlewareImpl() throws RemoteException {

	}

	public static void main(String[] args) {

		String server = "localhost";

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

         	Registry registryRM = LocateRegistry.getRegistry(server, 9876);
            Registry registryClient = LocateRegistry.getRegistry(8765);
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

	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) 
	throws RemoteException {

        try {
            if (rmFlight.addFlight(id, flightNum, flightSeats, flightPrice)) {
                System.out.println("Flight added");
                return true;
            } else {
                System.out.println("Flight not added");
                return false;
            }

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

	} 

    public boolean addCars(int id, String location, int numCars, int price) 
    throws RemoteException {

        try {
            if (rmCar.addCars(id, location, numCars, price)) {
                System.out.println("Car added");
                return true;
            } else {
                System.out.println("Car not added");
                return false;
            }

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    } 
   

    public boolean addRooms(int id, String location, int numRooms, int price) 
    throws RemoteException {

        try {
            if (rmHotel.addRooms(id, location, numRooms, price)) {
                System.out.println("Room added");
                return true;
            } else {
                System.out.println("Room not added");
                return false;
            }

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    } 			    
		

    public int newCustomer(int id) 
    throws RemoteException {
        
        Trace.info("INFO: RM::newCustomer(" + id + ") called" );
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                String.valueOf( Math.round( Math.random() * 100 + 1 )));
        Customer cust = new Customer(cid);
        writeData(id, cust.getKey(), cust);
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
        return cid;
    }
    

    public boolean newCustomer(int id, int cid) 
    throws RemoteException {
        
        Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(cid) );
        if( cust == null ) {
            cust = new Customer(cid);
            writeData( id, cust.getKey(), cust );
            Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") created a new customer" );
            return true;
        } else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + cid + ") failed--customer already exists");
            return false;
        }
    }


    public boolean deleteFlight(int id, int flightNum) 
    throws RemoteException {

        try {
            if (rmFlight.deleteFlight(id, flightNum)) {
                System.out.println("Flight deleted");
                return true;
            } else {
                System.out.println("Flight not deleted");
                return false;
            }

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    

    public boolean deleteCars(int id, String location) 
    throws RemoteException {

        try {
            if (rmCar.deleteCars(id, location)) {
                System.out.println("Flight deleted");
                return true;
            } else {
                System.out.println("Flight not deleted");
                return false;
            }

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    }

 
    public boolean deleteRooms(int id, String location) 
    throws RemoteException {

        try {
            if (rmHotel.deleteRooms(id, location)) {
                System.out.println("Flight deleted");
                return true;
            } else {
                System.out.println("Flight not deleted");
                return false;
            }

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

    }
    

    public boolean deleteCustomer(int id, int customerID) 
    throws RemoteException {

        RMItem item = removeData(id, Customer.getKey(customerID));
        return true;

    }


    public int queryFlight(int id, int flightNumber) 
    throws RemoteException {

        try { 
            int flightNum = rmFlight.queryFlight(id, flightNumber);
            return flightNum;

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public int queryCars(int id, String location) 
    throws RemoteException {

        try { 
            int numCars = rmCar.queryCars(id, location);
            return numCars;

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    }


    public int queryRooms(int id, String location) 
    throws RemoteException {

        try { 
            int numRooms = rmHotel.queryRooms(id, location);
            return numRooms;

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public String queryCustomerInfo(int id, int customerID) 
    throws RemoteException {
        
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));
        if(cust == null) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
        } else {
                String s = cust.printBill();
                Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                System.out.println( s );
                return s;
        }
    } 
    

    public int queryFlightPrice(int id, int flightNumber) 
    throws RemoteException {

        try { 
            int flightPrice = rmFlight.queryFlightPrice(id, flightNumber);
            return flightPrice;

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public int queryCarsPrice(int id, String location) 
    throws RemoteException {

        try { 
            int carsPrice = rmCar.queryCarsPrice(id, location);
            return carsPrice;

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public int queryRoomsPrice(int id, String location) 
    throws RemoteException {

        try { 
            int roomsPrice = rmHotel.queryRoomsPrice(id, location);
            return roomsPrice;

        } catch (Exception e) {
            System.out.println("MW EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;

    } 


    public boolean reserveFlight(int id, int customerID, int flightNumber) 
    throws RemoteException {

        Trace.info("RM::reserveFlight(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));

        if (cust == null) {
            Trace.warn("RM::reserveFlight(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
                try {
                    cust = rmFlight.reserveFlight(id, cust, flightNumber);
                    if (cust != null) {
                        writeData(id, cust.getKey(), cust);
                        return true;
                    } else {
                        System.out.println("Failed to reserve the flight.");
                        return false;
                    }

                } catch (Exception e) {
                    System.out.println("MW EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                return false;
        }

    } 


    public boolean reserveCar(int id, int customerID, String location) 
    throws RemoteException {

        Trace.info("RM::reserveCar(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));

        if (cust == null) {
            Trace.warn("RM::reserveCar(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
                try {
                    cust = rmCar.reserveCar(id, cust, location);
                    if (cust != null) {
                        writeData(id, cust.getKey(), cust);
                        return true;
                    } else {
                        System.out.println("Failed to reserve the car.");
                        return false;
                    }

                } catch (Exception e) {
                    System.out.println("MW EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                return false;
        }
        
    } 


    public boolean reserveRoom(int id, int customerID, String location) 
    throws RemoteException {

        Trace.info("RM::reserveRoom(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));

        if (cust == null) {
            Trace.warn("RM::reserveRoom(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
                try {
                    cust = rmHotel.reserveRoom(id, cust, location);
                    if (cust != null) {
                        writeData(id, cust.getKey(), cust);
                        return true;
                    } else {
                        System.out.println("Failed to reserve the room.");
                        return false;
                    }

                } catch (Exception e) {
                    System.out.println("MW EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                return false;
        }
        
    } 

    public boolean itinerary(int id, int customerID, Vector<Integer> flightNumbers, String location, boolean car, boolean room) 
    throws RemoteException {

        Trace.info("RM::reserveItinerary(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));

        if (cust == null) {
            Trace.warn("RM::reserveItinerary(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
                try {
                    //reserve flights
                    for (Integer integer : flightNumbers) {
                        cust = rmFlight.reserveFlight(id, cust, integer.intValue());
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to create the flights for the itinerary.");
                        }
                    }
                    //reserve a car
                    if (car) {
                        cust = (Customer) readData(id, Customer.getKey(customerID));
                        cust = rmCar.reserveCar(id, cust, location);
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to reserve the car for the itinerary.");
                        }
                    }

                    //reserve a room
                    if (room) {
                        cust = (Customer) readData(id, Customer.getKey(customerID));
                        cust = rmHotel.reserveRoom(id, cust, location);
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to reserve the room for the itinerary.");
                        }
                    }
                    return true;

                } catch (Exception e) {
                    System.out.println("MW EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                return false;
        }
    }

}
