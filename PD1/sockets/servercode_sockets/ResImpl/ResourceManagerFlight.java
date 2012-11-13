package ResImpl;

import java.util.*;
import java.io.*;
import java.net.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResourceManagerFlight {

	protected static RMHashtable m_itemHT = new RMHashtable();
	private static ServerSocket serverSocket;
	private int port = 5432;
	private static boolean listening = true;


	public ResourceManagerFlight() {

	}

	public int listen() throws IOException {

		try {
			System.out.println("Opening listening socket on port "+ port + ":");
			serverSocket = new ServerSocket(port);
			System.out.println(serverSocket.toString());

		} catch (Exception e) {
			System.err.println("Couldn't listen on port #" + port);
			e.printStackTrace();
		}
        
		while (listening) {
            ResourceManagerFlightThread resourceManagerFlightThread;
            System.out.println("Accepting on port " + port);
            Socket socket = serverSocket.accept();
            System.out.println("Middleware accepted!");

            //Start a new thread for each connection received
            resourceManagerFlightThread = new ResourceManagerFlightThread(socket);
            resourceManagerFlightThread.start();
        }
        serverSocket.close();
		return 0;
	}

	public static void main(String[] args) throws IOException {
		ResourceManagerFlight flightRM = new ResourceManagerFlight();
		flightRM.listen();
	}
}