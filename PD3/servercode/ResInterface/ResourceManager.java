package ResInterface;

import ResImpl.Customer;
import ResImpl.TransactionAbortedException;
import ResImpl.InvalidTransactionException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.*;
/** 
 * Simplified version from CSE 593 Univ. of Washington
 *
 * Distributed  System in Java.
 * 
 * failure reporting is done using two pieces, exceptions and boolean 
 * return values.  Exceptions are used for systemy things. Return
 * values are used for operations that would affect the consistency
 * 
 * If there is a boolean return value and you're not sure how it 
 * would be used in your implementation, ignore it.  I used boolean
 * return values in the interface generously to allow flexibility in 
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 */

public interface ResourceManager extends Remote 
{
    /* Add seats to a flight.  In general this will be used to create a new
     * flight, but it should be possible to add seats to an existing flight.
     * Adding to an existing flight should overwrite the current price of the
     * available seats.
     *
     * @return success.
     */
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice, int xid) 
	throws RemoteException; 
    
    /* Add cars to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addCars(int id, int iid, String location, int numCars, int price, int xid) 
	throws RemoteException; 
   
    /* Add rooms to a location.  
     * This should look a lot like addFlight, only keyed on a string location
     * instead of a flight number.
     */
    public boolean addRooms(int id, int iid, String location, int numRooms, int price, int xid) 
	throws RemoteException; 			    

			    
    /* new customer just returns a unique customer identifier */
    public int newCustomer(int id, int xid) 
	throws RemoteException; 
    
    /* new customer with providing id */
    public boolean newCustomer(int id, int cid, int xid)
    throws RemoteException;

    /**
     *   Delete the entire flight.
     *   deleteflight implies whole deletion of the flight.  
     *   all seats, all reservations.  If there is a reservation on the flight, 
     *   then the flight cannot be deleted
     *
     * @return success.
     */   
    public boolean deleteFlight(int id, int flightNum, int xid) 
	throws RemoteException; 
    
    /* Delete all Cars from a location.
     * It may not succeed if there are reservations for this location
     *
     * @return success
     */		    
    public boolean deleteCars(int id, int iid, String location, int xid) 
	throws RemoteException; 

    /* Delete all Rooms from a location.
     * It may not succeed if there are reservations for this location.
     *
     * @return success
     */
    public boolean deleteRooms(int id, int iid, String location, int xid) 
	throws RemoteException; 
    
    /* deleteCustomer removes the customer and associated reservations */
    public boolean deleteCustomer(int id,int customer, int xid) 
	throws RemoteException; 

    /* queryFlight returns the number of empty seats. */
    public int queryFlight(int id, int flightNumber, int xid) 
	throws RemoteException; 

    /* return the number of cars available at a location */
    public int queryCars(int id, int iid, String location, int xid) 
	throws RemoteException; 

    /* return the number of rooms available at a location */
    public int queryRooms(int id, int iid, String location, int xid) 
	throws RemoteException; 

    /* return a bill */
    public String queryCustomerInfo(int id,int customer, int xid) 
	throws RemoteException; 
    
    /* queryFlightPrice returns the price of a seat on this flight. */
    public int queryFlightPrice(int id, int flightNumber, int xid) 
	throws RemoteException; 

    /* return the price of a car at a location */
    public int queryCarsPrice(int id, int iid, String location, int xid) 
	throws RemoteException; 

    /* return the price of a room at a location */
    public int queryRoomsPrice(int id, int iid, String location, int xid) 
	throws RemoteException; 

    /* Reserve a seat on this flight*/
    public Customer reserveFlight(int id, Customer cust, int flightNumber, int xid) 
	throws RemoteException; 

    /* reserve a car at this location */
    public Customer reserveCar(int id, int iid, Customer cust, String location, int xid) 
	throws RemoteException; 

    /* reserve a room certain at this location */
    public Customer reserveRoom(int id, int iid, Customer cust, String location, int xid) 
	throws RemoteException; 


    /* reserve an itinerary */
    public boolean itinerary(int id,int customer, int iidCar, int iidRoom, Vector flightNumbers,String location, boolean Car, boolean Room, int xid)
	throws RemoteException; 

    public void enlist(int xid) throws RemoteException;

    public boolean voteReq(int xid) throws RemoteException;

    public void clearExpiredTransactions(long newTime, int ttl);

    /* commit a transaction */
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException;

    /* abort a transaction */
    public void abort(int xid) throws RemoteException, InvalidTransactionException;

    /* shutdown all systems */
    public boolean shutdown() throws RemoteException;
}
