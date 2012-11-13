package MidInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;

import ResImpl.DeadlockException;
import ResImpl.InvalidTransactionException;
import ResImpl.TransactionAbortedException;

public interface Middleware extends Remote {
	
    /* Add seats to a flight.  In general this will be used to create a new
     * flight, but it should be possible to add seats to an existing flight.
     * Adding to an existing flight should overwrite the current price of the
     * available seats.
     *
     * @return success.
     */

    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice, int xid) throws RemoteException; 
    
    /* Add cars to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addCars(int id, int iid, String location, int numCars, int price, int xid) throws RemoteException; 
   
    /* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addRooms(int id, int iid, String location, int numRooms, int price, int xid) throws RemoteException; 			    

			    
    /* new customer just returns a unique customer identifier */
    public int newCustomer(int id, int xid) throws RemoteException, NumberFormatException; 
    
    /* new customer with providing id */
    public boolean newCustomer(int id, int cid, int xid) throws RemoteException;

    /**
     *   Delete the entire flight.
     *   deleteflight implies whole deletion of the flight.  
     *   all seats, all reservations.  If there is a reservation on the flight, 
     *   then the flight cannot be deleted
     *
     * @return success.
     */   
    public boolean deleteFlight(int id, int flightNum, int xid) throws RemoteException; 
    
    /* Delete all Cars from a location.
     * It may not succeed if there are reservations for this location
     *
     * @return success
     */		    
    public boolean deleteCars(int id, int iid, String location, int xid) throws RemoteException; 

    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public boolean deleteRooms(int id, int iid, String location, int xid) throws RemoteException; 
    
    /* deleteCustomer removes the customer and associated reservations */
    public boolean deleteCustomer(int id,int customer, int xid) throws RemoteException; 

    /* queryFlight returns the number of empty seats. */
    public int queryFlight(int id, int flightNumber, int xid) throws RemoteException; 

    /* return the number of cars available at a location */
    public int queryCars(int id, int iid, String location, int xid) throws RemoteException; 

    /* return the number of rooms available at a location */
    public int queryRooms(int id, int iid, String location, int xid) throws RemoteException; 

    /* return a bill */
    public String queryCustomerInfo(int id, int customer, int xid) throws RemoteException; 
    
    /* queryFlightPrice returns the price of a seat on this flight. */
    public int queryFlightPrice(int id, int flightNumber, int xid) throws RemoteException; 

    /* return the price of a car at a location */
    public int queryCarsPrice(int id, int iid, String location, int xid) throws RemoteException; 

    /* return the price of a room at a location */
    public int queryRoomsPrice(int id, int iid, String location, int xid) throws RemoteException; 

    /* Reserve a seat on this flight*/
    public boolean reserveFlight(int id, int customerID, int flightNumber, int xid) throws RemoteException; 

    /* reserve a car at this location */
    public boolean reserveCar(int id, int iid, int customerID, String location, int xid) throws RemoteException; 

    /* reserve a room certain at this location */
    public boolean reserveRoom(int id, int iid, int customerID, String location, int xid) throws RemoteException; 

    /* reserve an itinerary */
    public boolean itinerary(int id, int customerID, int iidCar, int iidRoom, Vector<Integer> flightNumbers, String location, boolean car, boolean room, int xid) throws RemoteException; 

    /* start a transaction */
    public int start() throws RemoteException;

    /* commit a transaction */
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException;

    /* abort a transaction */
    public void abort(int xid) throws RemoteException, InvalidTransactionException;

    /* shutdown all systems */
    public boolean shutdown() throws RemoteException;
}