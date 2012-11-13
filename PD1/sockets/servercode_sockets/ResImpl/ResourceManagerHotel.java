package ResImpl;

import java.util.*;
import java.io.*;
import java.net.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResourceManagerHotel {

	protected static RMHashtable m_itemHT = new RMHashtable();
	private static ServerSocket serverSocket;
	private int port = 4321;
	private static boolean listening = true;


	public ResourceManagerHotel() {

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
            ResourceManagerHotelThread resourceManagerHotelThread;
            System.out.println("Accepting on port " + port);
            Socket socket = serverSocket.accept();
            System.out.println("Middleware accepted!");

            // Start a new connection for each connection received
            resourceManagerHotelThread = new ResourceManagerHotelThread(socket);
            resourceManagerHotelThread.start();
        }
        serverSocket.close();
		return 0;
	}

	public static void main(String[] args) throws IOException {
		ResourceManagerHotel hotelRM = new ResourceManagerHotel();
		hotelRM.listen();
	}
}