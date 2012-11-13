package ResImpl;

import java.util.*;
import java.io.*;
import java.net.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResourceManagerCar {

	protected static RMHashtable m_itemHT = new RMHashtable();
	private static ServerSocket serverSocket;
	private int port = 6543;
	private static boolean listening = true;


	public ResourceManagerCar() {

	}

	public int listen() throws IOException {

		try {
			System.out.println("Opening listening socket on port "+ 6543 + ":");
			serverSocket = new ServerSocket(port);
			System.out.println(serverSocket.toString());

		} catch (Exception e) {
			System.err.println("Couldn't listen on port #" + port);
			e.printStackTrace();
		}
        
		while (listening) {
            ResourceManagerCarThread resourceManagerCarThread;
            System.out.println("Accepting on port 6543");
            Socket socket = serverSocket.accept();
            System.out.println("Middleware accepted!");

            //Start a new thread for each connection receieved
            resourceManagerCarThread = new ResourceManagerCarThread(socket);
            resourceManagerCarThread.start();
        }
        serverSocket.close();
		return 0;
	}

	public static void main(String[] args) throws IOException {
		ResourceManagerCar carRM = new ResourceManagerCar();
		carRM.listen();
	}
}