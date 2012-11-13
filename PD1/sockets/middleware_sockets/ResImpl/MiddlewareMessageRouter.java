package ResImpl;

import java.util.*;
import java.net.*;
import org.json.simple.*;
import java.io.*;

//public class MiddlewareMessageRouter
public class MiddlewareMessageRouter extends Thread {

    private Socket clientSocket = null;
    ObjectOutputStream outStream = null;
    boolean runQueue = true;
    //Stores all messages to be forwarded to the client
    private LinkedList messageQueue;

    public MiddlewareMessageRouter (Socket socket) {
	    super("MiddlewareMessageRouter");
        this.clientSocket = socket;
	}

    public synchronized void pushToQueue(Object o) {
        messageQueue.offer(o);
    }

	public void run() {
         try {
            messageQueue = new LinkedList();
            System.out.println("Message Queue created");
            outStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outStream.flush();
            while(runQueue) {
                try {
                    //Constantly push all messages out of queue
                    while (messageQueue.size() > 0) {
                        outStream.writeObject(messageQueue.poll());
                    }
                    
                } catch (EOFException e) {
                    System.out.println("EOF!");
                    //break;
                }
            }
            outStream.close();
            clientSocket.close();
         } catch (IOException e) {

         	System.err.println("Middleware thread exception: " + e.toString());
         	e.printStackTrace();

         }
	}

}
