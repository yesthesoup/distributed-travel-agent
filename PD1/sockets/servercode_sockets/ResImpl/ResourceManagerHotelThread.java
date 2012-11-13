package ResImpl;

import java.util.*;
import java.net.*;
import org.json.simple.*;
import java.io.*;

//public class ResourceManagerHotelThread
public class ResourceManagerHotelThread extends Thread {

    private Socket middlewareSocket = null;
    ObjectInputStream inStream;
    ObjectOutputStream outStream;

    public ResourceManagerHotelThread (Socket socket) {
	    super("ResourceManagerHotelThread");
        this.middlewareSocket = socket;
	}

	public void run() {
         try {
            System.out.println( "Hotel RM Thread spawned" );
            outStream = new ObjectOutputStream(middlewareSocket.getOutputStream());
            outStream.flush();
            inStream = new ObjectInputStream(middlewareSocket.getInputStream());
            try {
                JSONObject request = (JSONObject)inStream.readObject();
                String methodName = (String)request.get("method");
                int clientChoice = findChoice(methodName);
                switch (findChoice(methodName)) {
                    case 1: //addRooms
                        addRooms(request);
                        break;
                    case 2: //deleteRooms
                        deleteRooms(request);
                        break;
                    case 3: //queryRooms
                        queryRooms(request);
                        break;
                    case 4: //queryRoomsPrice
                        queryRoomsPrice(request);
                        break;
                    case 5: //reserveRoom
                        reserveRoom(request);
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

         	System.err.println("Hotel RM thread exception: " + e.toString());
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

    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public void addRooms(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        String location = (String)o.get("location");
        int count = ((Integer)o.get("numRooms")).intValue();
        int price = ((Integer)o.get("price")).intValue();
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        if( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing object and update price...
            curObj.setCount( curObj.getCount() + count );
            if( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        outStream.writeObject(new String("Rooms added"));
        outStream.flush();
    }

    // Delete rooms from a location
    public void deleteRooms(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        String location = (String)o.get("location");
        String s = "";
        boolean deleted = deleteItem(id, Hotel.getKey(location));
        if (deleted) {
            s = "Rooms Deleted";
        } else {
            s = "Rooms could not be deleted";
        }
        outStream.writeObject(new String(s));
        outStream.flush();
    }

    // Returns the number of rooms available at a location
    public void queryRooms(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        String location = (String)o.get("location");
        int value = queryNum(id, Hotel.getKey(location));
        outStream.writeObject(new Integer(value));
        outStream.flush();
    }

    // Returns room price at this location
    public void queryRoomsPrice(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        String location = (String)o.get("location");
        int price = queryPrice(id, Hotel.getKey(location));
        outStream.writeObject(new Integer(price));
        outStream.flush();
    }

    // Adds room reservation to this customer. 
    public void reserveRoom(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        Customer cust = (Customer)o.get("customer");
        String location = (String)o.get("location");
        cust = reserveItem(id, cust, Hotel.getKey(location), location);
        outStream.writeObject(cust);
        outStream.flush();
    }

    public int findChoice(String argument) {
        if(argument.compareToIgnoreCase("addrooms")==0)
            return 1;
        else if(argument.compareToIgnoreCase("deleterooms")==0)
            return 2;
        else if(argument.compareToIgnoreCase("queryrooms")==0)
            return 3;
        else if(argument.compareToIgnoreCase("queryroomsprice")==0)
            return 4;
        else if(argument.compareToIgnoreCase("reserveroom")==0)
            return 5;
        else if (argument.compareToIgnoreCase("quit")==0)
            return 6;
        else
            return 666;
    }

}
