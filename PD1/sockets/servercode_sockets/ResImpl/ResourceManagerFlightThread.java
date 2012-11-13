package ResImpl;

import java.util.*;
import java.net.*;
import org.json.simple.*;
import java.io.*;

//public class ResourceManagerFlightThread
public class ResourceManagerFlightThread extends Thread {

    private Socket middlewareSocket = null;
    ObjectInputStream inStream;
    ObjectOutputStream outStream;

    public ResourceManagerFlightThread (Socket socket) {
	    super("ResourceManagerFlightThread");
        this.middlewareSocket = socket;
	}

	public void run() {
         try {
            System.out.println( "Flight RM Thread spawned" );
            outStream = new ObjectOutputStream(middlewareSocket.getOutputStream());
            outStream.flush();
            inStream = new ObjectInputStream(middlewareSocket.getInputStream());
            try {
                JSONObject request = (JSONObject)inStream.readObject();
                String methodName = (String)request.get("method");
                int clientChoice = findChoice(methodName);
                switch (findChoice(methodName)) {
                    case 1: //addFlight
                        addFlight(request);
                        break;
                    case 2: //deleteFlight
                        deleteFlight(request);
                        break;
                    case 3: //queryFlight
                        queryFlight(request);
                        break;
                    case 4: //queryFlightPrice
                        queryFlightPrice(request);
                        break;
                    case 5: //reserveFlight
                        reserveFlight(request);
                        break;
                    case 6:
                        System.out.println("Middleware logged out.");
                    default:
                        System.out.println("Middleware message error!");
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Bad input!");
            } catch (EOFException e) {
                System.out.println("Middleware disconnected.");
            }
            outStream.close();
            inStream.close();
            middlewareSocket.close();
         } catch (IOException e) {

         	System.err.println("Flight RM thread exception: " + e.toString());
         	e.printStackTrace();

         }
	}

    // Reads a data item
    private RMItem readData(int id, String key)
    {
        synchronized(ResourceManagerCar.m_itemHT){
            return (RMItem) ResourceManagerCar.m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData(int id, String key, RMItem value)
    {
        synchronized(ResourceManagerCar.m_itemHT){
            ResourceManagerCar.m_itemHT.put(key, value);
        }
    }
    
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key){
        synchronized(ResourceManagerCar.m_itemHT){
            return (RMItem)ResourceManagerCar.m_itemHT.remove(key);
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

    // query the number of available seats/rooms/cars
    protected int queryNum(int id, String key) {
        
        Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0;  
        if(curObj != null) {
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

    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public void addFlight(JSONObject o)
    throws IOException 
    {
        int id = ((Integer)o.get("id")).intValue();
        int flightNum = ((Integer)o.get("flightNum")).intValue();
        int flightSeats = ((Integer)o.get("flightSeats")).intValue();
        int flightPrice = ((Integer)o.get("flightPrice")).intValue();
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
        Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
        if (curObj == null) {
            // doesn't exist...add it
            Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                    flightSeats + ", price=$" + flightPrice );
        } else {
            // add seats to existing flight and update the price...
            curObj.setCount( curObj.getCount() + flightSeats );
            if (flightPrice > 0) {
                curObj.setPrice( flightPrice );
            }
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
        }
        outStream.writeObject(new String("Flight added"));
        outStream.flush();
    }

    public void deleteFlight(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int flightNum = ((Integer)o.get("flightNum")).intValue();
        String s = "";
        boolean deleted = deleteItem(id, Flight.getKey(flightNum));
        if (deleted) {
            s = "Flight Deleted";
        } else {
            s = "Flight could not be deleted";
        }
        outStream.writeObject(new String(s));
        outStream.flush();
    }

    // Returns the number of empty seats on this flight
    public void queryFlight(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int flightNum = ((Integer)o.get("flightNum")).intValue();
        int value = queryNum(id, Flight.getKey(flightNum));
        outStream.writeObject(new Integer(value));
        outStream.flush();
    }

    // Returns price of this flight
    public void queryFlightPrice(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int flightNum = ((Integer)o.get("flightNum")).intValue();
        int price = queryPrice(id, Flight.getKey(flightNum));
        outStream.writeObject(new Integer(price));
        outStream.flush();
    }

    // Adds flight reservation to this customer.  
    public void reserveFlight(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        Customer cust = (Customer)o.get("customer");
        int flightNum = ((Integer)o.get("flightNum")).intValue();
        cust = reserveItem(id, cust, Flight.getKey(flightNum), String.valueOf(flightNum));
        outStream.writeObject(cust);
        outStream.flush();
    }

    public int findChoice(String argument) {
        if(argument.compareToIgnoreCase("addflight")==0)
            return 1;
        else if(argument.compareToIgnoreCase("deleteflight")==0)
            return 2;
        else if(argument.compareToIgnoreCase("queryflight")==0)
            return 3;
        else if(argument.compareToIgnoreCase("queryflightprice")==0)
            return 4;
        else if(argument.compareToIgnoreCase("reserveflight")==0)
            return 5;
        else if (argument.compareToIgnoreCase("quit")==0)
            return 6;
        else
            return 666;
    }

}
