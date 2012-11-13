package ResImpl;

import java.util.*;
import java.net.*;
import org.json.simple.*;
import java.io.*;

//public class MiddlewareThread
public class MiddlewareThread extends Thread {

    private Socket clientSocket = null;
    private MiddlewareMessageRouter router;
    ObjectInputStream inStream;
    Socket carSocket;
    Socket flightSocket;
    Socket roomSocket;
    String server = "localhost";

    public MiddlewareThread (Socket socket, MiddlewareMessageRouter router, String server) {
	    super("MiddlewareThread");
        this.clientSocket = socket;
        this.router = router;
        this.server = server;
	}

	public void run() {
         try {
            System.out.println( "Middleware Thread spawned" );
            inStream = new ObjectInputStream(clientSocket.getInputStream());
            while(true) {
                try {
                    MiddlewareMethodThread midMethodThread;
                    JSONObject clientInput = (JSONObject)inStream.readObject();
                    String methodName = (String)clientInput.get("method");
                    int clientChoice = findChoice(methodName);
                    if (clientChoice == 20) {
                        System.out.println("Client logged out.");
                        break;
                    } else if (clientChoice == 666) {
                        System.out.println("Client message error!");
                    }
                    // Start a new method thread for each command invoked
                    midMethodThread = new MiddlewareMethodThread(clientInput, router, server);
                    midMethodThread.start();
                } catch (ClassNotFoundException e) {
                    System.out.println("Bad input!");
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break;
                }
            }
            inStream.close();
            clientSocket.close();
         } catch (IOException e) {

         	System.err.println("Middleware thread exception: " + e.toString());
         	e.printStackTrace();

         }
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
