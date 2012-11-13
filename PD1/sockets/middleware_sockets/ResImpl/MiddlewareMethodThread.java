package ResImpl;

import java.util.*;
import java.net.*;
import org.json.simple.*;
import java.io.*;

//public class MiddlewareMethodThread
public class MiddlewareMethodThread extends Thread {

    private JSONObject request = null;
    private MiddlewareMessageRouter router;
    String server = "localhost";

    //Constructor for MiddlewareMethodThread
    public MiddlewareMethodThread (JSONObject request, MiddlewareMessageRouter router, String server) {
	    super("MiddlewareMethodThread");
        this.request = request;
        this.router = router;
        this.server = server;
	}

    //Starts the thread
	public void run() {
         try {
            System.out.println("Middleware Method Thread Spawned");
            String methodName = (String)request.get("method");
            //Send different commands to different methods
            switch (findChoice(methodName)) {
                case 1: //addFlight
                    sendToFlight(request);
                    break;
                case 2: //addCars
                    sendToCar(request);
                    break;
                case 3: //addRooms
                    sendToRoom(request);
                    break;
                case 4: //newCustomer
                    newCustomer(request);
                    break;
                case 5: //deleteFlight
                    sendToFlight(request);
                    break;
                case 6: //deleteCar
                    sendToCar(request);
                    break;
                case 7: //deleteRoom
                    sendToRoom(request);
                    break;
                case 8: //deleteCustomer
                    deleteCustomer(request);
                    break;
                case 9: //queryFlight
                    sendToFlight(request);
                    break;
                case 10: //queryCar;
                    sendToCar(request);
                    break;
                case 11: //queryRoom
                    sendToRoom(request);
                    break;
                case 12: //queryCustomerInfo
                    queryCustomerInfo(request);
                    break;
                case 13: //queryFlightPrice
                    sendToFlight(request);
                    break;
                case 14: //queryCarPrice
                    sendToCar(request);
                    break;
                case 15: //queryRoomPrice
                    sendToRoom(request);
                    break;
                case 16: //reserveFlight
                    reserveFlight(request);
                    break;
                case 17: //reserveCar
                    reserveCar(request);
                    break;
                case 18: //reserveRoom
                    reserveRoom(request);
                    break;
                case 19: //itinerary
                    itinerary(request);
                    break;
            }
         } catch (IOException e) {

         	System.err.println("Middleware thread exception: " + e.toString());
         	e.printStackTrace();

         } catch (ClassNotFoundException e) {
            System.err.println("Bad Return from RM: " + e.toString());
            e.printStackTrace();
         }
         
	}

    // Reads a data item
    private RMItem readData(int id, String key)
    {
        synchronized(MiddlewareImpl.m_itemHT){
            return (RMItem) MiddlewareImpl.m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData(int id, String key, RMItem value)
    {
        synchronized(MiddlewareImpl.m_itemHT){
            MiddlewareImpl.m_itemHT.put(key, value);
        }
    }
    
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key){
        synchronized(MiddlewareImpl.m_itemHT){
            return (RMItem)MiddlewareImpl.m_itemHT.remove(key);
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

    //Forward complete JSONObject to Flight RM
    public void sendToFlight(JSONObject o)
    throws IOException, ClassNotFoundException {
        //Initialize connection
        Socket flightSocket = new Socket(server, 5432);
        System.out.println("Connected to Flight RM.");
        ObjectOutputStream flightOutStream = new ObjectOutputStream(flightSocket.getOutputStream());
        flightOutStream.flush();
        ObjectInputStream flightInStream = new ObjectInputStream(flightSocket.getInputStream());
        System.out.println("Sending request to Flight RM...");
        //Write data and flush
        flightOutStream.writeObject(o);
        flightOutStream.flush();
        //Read response
        Object rmReturn = flightInStream.readObject();
        System.out.println("Response received from Flight RM.");
        //Push Response to sending queue
        router.pushToQueue(rmReturn);
        //Close streams and socket
        flightOutStream.close();
        flightInStream.close();
        flightSocket.close();
    }

    //Forward complete JSONObject to Car RM
    public void sendToCar(JSONObject o)
    throws IOException, ClassNotFoundException {
        Socket carSocket = new Socket(server, 6543);
        System.out.println("Connected to Car RM.");
        ObjectOutputStream carOutStream = new ObjectOutputStream(carSocket.getOutputStream());
        carOutStream.flush();
        ObjectInputStream carInStream = new ObjectInputStream(carSocket.getInputStream());
        System.out.println("Sending request to Car RM...");
        carOutStream.writeObject(o);
        carOutStream.flush();
        Object rmReturn = carInStream.readObject();
        System.out.println("Response received from Car RM.");
        router.pushToQueue(rmReturn);
        carOutStream.close();
        carInStream.close();
        carSocket.close();
    }

    //Forward complete JSONObject to Hotel RM
    public void sendToRoom(JSONObject o)
    throws IOException, ClassNotFoundException {
        Socket roomSocket = new Socket(server, 4321);
        System.out.println("Connected to Hotel RM.");
        ObjectOutputStream roomOutStream = new ObjectOutputStream(roomSocket.getOutputStream());
        roomOutStream.flush();
        ObjectInputStream roomInStream = new ObjectInputStream(roomSocket.getInputStream());
        System.out.println("Sending request to Hotel RM...");
        roomOutStream.writeObject(o);
        roomOutStream.flush();
        Object rmReturn = roomInStream.readObject();
        System.out.println("Response received from Hotel RM.");
        router.pushToQueue(rmReturn);
        roomOutStream.close();
        roomInStream.close();
        roomSocket.close();
    }

    public void newCustomer(JSONObject o) 
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        Trace.info("INFO: RM::newCustomer(" + id + ") called" );
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                String.valueOf( Math.round( Math.random() * 100 + 1 )));
        Customer cust = new Customer(cid);
        writeData(id, cust.getKey(), cust);
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
        router.pushToQueue(new Integer(cid));
    } 

    public void deleteCustomer(JSONObject o) 
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int customerID = ((Integer)o.get("customerID")).intValue();
        removeData(id, Customer.getKey(customerID));
        router.pushToQueue(new String("Customer deleted."));
    }

    public void queryCustomerInfo(JSONObject o)
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int customerID = ((Integer)o.get("customerID")).intValue();
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));
        if(cust == null) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            router.pushToQueue(new String(""));
        } else {
                String s = cust.printBill();
                Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                System.out.println( s );
                router.pushToQueue(new String(s));
        }
    } 

    public void reserveFlight(JSONObject o) 
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int customerID = ((Integer)o.get("customerID")).intValue();
        int flightNum = ((Integer)o.get("flightNum")).intValue();
        
        Trace.info("RM::reserveFlight(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));
        boolean success = false;
        String s = "";
        if (cust == null) {
            Trace.warn("RM::reserveFlight(" + id + ", " + customerID + ") failed--customer doesn't exist" );
        } else {
            try {
                //Rebuild JSONObject and forward it to the RM
                JSONObject rmO = new JSONObject();
                rmO.put("method", "reserveFlight");
                rmO.put("id", new Integer(id));
                rmO.put("customer", cust);
                rmO.put("flightNum", new Integer(flightNum));

                Socket flightSocket = new Socket(server, 5432);
                System.out.println("Connected to Flight RM.");
                ObjectOutputStream flightOutStream = new ObjectOutputStream(flightSocket.getOutputStream());
                flightOutStream.flush();
                ObjectInputStream flightInStream = new ObjectInputStream(flightSocket.getInputStream());
                System.out.println("Reserving flight seats from Flight RM...");
                flightOutStream.writeObject(rmO);
                flightOutStream.flush();

                //Overwrite Customer in hash with returned Customer
                cust = (Customer)flightInStream.readObject();
                System.out.println("Response received from Flight RM.");
                flightOutStream.close();
                flightInStream.close();
                flightSocket.close();
                if (cust != null) {
                    writeData(id, cust.getKey(), cust);
                    success = true;
                } else {
                    System.out.println("Failed to reserve the flight.");
                }

            } catch (Exception e) {
                System.out.println("MW EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        if (success) {
            s = "Flight Reserved";
        } else {
            s = "Flight could not be reserved.";
        }
        router.pushToQueue(new String(s));
    } 


    public void reserveCar(JSONObject o) 
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int customerID = ((Integer)o.get("customerID")).intValue();
        String location = (String)o.get("location");
        Trace.info("RM::reserveCar(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));
        boolean success = false;
        String s = "";
        if (cust == null) {
            Trace.warn("RM::reserveCar(" + id + ", " + customerID + ") failed--customer doesn't exist" );
        } else {
            try {
                JSONObject rmO = new JSONObject();
                rmO.put("method", "reserveCar");
                rmO.put("id", new Integer(id));
                rmO.put("customer", cust);
                rmO.put("location", location);

                Socket carSocket = new Socket(server, 6543);
                System.out.println("Connected to Car RM.");
                ObjectOutputStream carOutStream = new ObjectOutputStream(carSocket.getOutputStream());
                carOutStream.flush();
                ObjectInputStream carInStream = new ObjectInputStream(carSocket.getInputStream());
                System.out.println("Reserving car from Car RM...");
                carOutStream.writeObject(rmO);
                carOutStream.flush();

                cust = (Customer)carInStream.readObject();
                System.out.println("Response received from Car RM.");
                carOutStream.close();
                carInStream.close();
                carSocket.close();
                if (cust != null) {
                    writeData(id, cust.getKey(), cust);
                    success = true;
                } else {
                    System.out.println("Failed to reserve the car.");
                }

            } catch (Exception e) {
                System.out.println("MW EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        if (success) {
            s = "Car Reserved";
        } else {
            s = "Car could not be reserved.";
        }
        router.pushToQueue(new String(s));
    } 


    public void reserveRoom(JSONObject o) 
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int customerID = ((Integer)o.get("customerID")).intValue();
        String location = (String)o.get("location");
        Trace.info("RM::reserveRoom(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));
        boolean success = false;
        String s = "";
        if (cust == null) {
            Trace.warn("RM::reserveRoom(" + id + ", " + customerID + ") failed--customer doesn't exist" );
        } else {
            try {
                JSONObject rmO = new JSONObject();
                rmO.put("method", "reserveRoom");
                rmO.put("id", new Integer(id));
                rmO.put("customer", cust);
                rmO.put("location", location);

                Socket roomSocket = new Socket(server, 4321);
                System.out.println("Connected to Hotel RM.");
                ObjectOutputStream roomOutStream = new ObjectOutputStream(roomSocket.getOutputStream());
                roomOutStream.flush();
                ObjectInputStream roomInStream = new ObjectInputStream(roomSocket.getInputStream());
                System.out.println("Reserving room from Hotel RM...");
                roomOutStream.writeObject(rmO);
                roomOutStream.flush();
                cust = (Customer)roomInStream.readObject();
                System.out.println("Response received from Hotel RM.");

                roomOutStream.close();
                roomInStream.close();
                roomSocket.close();
                if (cust != null) {
                    writeData(id, cust.getKey(), cust);
                    success = true;
                } else {
                    System.out.println("Failed to reserve the room.");
                }

            } catch (Exception e) {
                System.out.println("MW EXCEPTION:");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        if (success) {
            s = "Room Reserved";
        } else {
            s = "Room could not be reserved.";
        }
        router.pushToQueue(new String(s));
    } 

    public void itinerary(JSONObject o) 
    throws IOException {
        int id = ((Integer)o.get("id")).intValue();
        int customerID = ((Integer)o.get("customerID")).intValue();
        JSONArray flightNumbers = (JSONArray)o.get("flightNumbers");
        String location = (String)o.get("location");
        boolean car = ((Boolean)o.get("car")).booleanValue();
        boolean room = ((Boolean)o.get("room")).booleanValue();
        
        Trace.info("RM::reserveItinerary(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData(id, Customer.getKey(customerID));
        String s = "";
        boolean success = false;
        if (cust == null) {
            Trace.warn("RM::reserveItinerary(" + id + ", " + customerID + ") failed--customer doesn't exist" );
        } else {
                try {
                    //reserve flights
                    for (Object integer : flightNumbers) {
                        JSONObject rmOFlight = new JSONObject();
                        rmOFlight.put("method", "reserveFlight");
                        rmOFlight.put("id", new Integer(id));
                        rmOFlight.put("customer", cust);
                        rmOFlight.put("flightNum", integer);

                        Socket flightSocket = new Socket(server, 5432);
                        System.out.println("Connected to Flight RM.");
                        ObjectOutputStream flightOutStream = new ObjectOutputStream(flightSocket.getOutputStream());
                        flightOutStream.flush();
                        ObjectInputStream flightInStream = new ObjectInputStream(flightSocket.getInputStream());
                        System.out.println("Reserving flight seats from Flight RM...");
                        flightOutStream.writeObject(rmOFlight);
                        flightOutStream.flush();

                        cust = (Customer)flightInStream.readObject();
                        System.out.println("Response received from Flight RM.");
                        flightOutStream.close();
                        flightInStream.close();
                        flightSocket.close();
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to create the flights for the itinerary.");
                        }
                    }
                    //reserve a car
                    if (car) {
                        cust = (Customer) readData(id, Customer.getKey(customerID));

                        JSONObject rmOCar = new JSONObject();
                        rmOCar.put("method", "reserveCar");
                        rmOCar.put("id", new Integer(id));
                        rmOCar.put("customer", cust);
                        rmOCar.put("location", location);

                        Socket carSocket = new Socket(server, 6543);
                        System.out.println("Connected to Car RM.");
                        ObjectOutputStream carOutStream = new ObjectOutputStream(carSocket.getOutputStream());
                        carOutStream.flush();
                        ObjectInputStream carInStream = new ObjectInputStream(carSocket.getInputStream());
                        System.out.println("Reserving car from Car RM...");
                        carOutStream.writeObject(rmOCar);
                        carOutStream.flush();

                        cust = (Customer)carInStream.readObject();
                        System.out.println("Response received from Car RM.");
                        carOutStream.close();
                        carInStream.close();
                        carSocket.close();
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to reserve the car for the itinerary.");
                        }
                    }

                    //reserve a room
                    if (room) {
                        cust = (Customer) readData(id, Customer.getKey(customerID));


                        JSONObject rmORoom = new JSONObject();
                        rmORoom.put("method", "reserveRoom");
                        rmORoom.put("id", new Integer(id));
                        rmORoom.put("customer", cust);
                        rmORoom.put("location", location);

                        Socket roomSocket = new Socket(server, 4321);
                        System.out.println("Connected to Hotel RM.");
                        ObjectOutputStream roomOutStream = new ObjectOutputStream(roomSocket.getOutputStream());
                        roomOutStream.flush();
                        ObjectInputStream roomInStream = new ObjectInputStream(roomSocket.getInputStream());
                        System.out.println("Reserving room from Hotel RM...");
                        roomOutStream.writeObject(rmORoom);
                        roomOutStream.flush();
                        cust = (Customer)roomInStream.readObject();
                        System.out.println("Response received from Hotel RM.");

                        roomOutStream.close();
                        roomInStream.close();
                        roomSocket.close();
                        if (cust != null) {
                            writeData(id, cust.getKey(), cust);
                        } else {
                            System.out.println("Failed to reserve the room for the itinerary.");
                        }
                    }
                    success = true;

                } catch (Exception e) {
                    System.out.println("MW EXCEPTION:");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
        }
        if (success) {
            s = "Itinerary Booked.";
        } else {
            s = "Itinerary could not be booked.";
        }
        router.pushToQueue(new String(s));
    }

    public int findChoice(String argument) {
        if(argument.compareToIgnoreCase("addflight")==0)
            return 1;
        else if(argument.compareToIgnoreCase("addcars")==0)
            return 2;
        else if(argument.compareToIgnoreCase("addrooms")==0)
            return 3;
        else if(argument.compareToIgnoreCase("newcustomer")==0)
            return 4;
        else if(argument.compareToIgnoreCase("deleteflight")==0)
            return 5;
        else if(argument.compareToIgnoreCase("deletecars")==0)
            return 6;
        else if(argument.compareToIgnoreCase("deleterooms")==0)
            return 7;
        else if(argument.compareToIgnoreCase("deletecustomer")==0)
            return 8;
        else if(argument.compareToIgnoreCase("queryflight")==0)
            return 9;
        else if(argument.compareToIgnoreCase("querycars")==0)
            return 10;
        else if(argument.compareToIgnoreCase("queryrooms")==0)
            return 11;
        else if(argument.compareToIgnoreCase("querycustomerinfo")==0)
            return 12;
        else if(argument.compareToIgnoreCase("queryflightprice")==0)
            return 13;
        else if(argument.compareToIgnoreCase("querycarsprice")==0)
            return 14;
        else if(argument.compareToIgnoreCase("queryroomsprice")==0)
            return 15;
        else if(argument.compareToIgnoreCase("reserveflight")==0)
            return 16;
        else if(argument.compareToIgnoreCase("reservecar")==0)
            return 17;
        else if(argument.compareToIgnoreCase("reserveroom")==0)
            return 18;
        else if(argument.compareToIgnoreCase("itinerary")==0)
            return 19;
        else if (argument.compareToIgnoreCase("quit")==0)
            return 20;
        else
            return 666;
    }

}
