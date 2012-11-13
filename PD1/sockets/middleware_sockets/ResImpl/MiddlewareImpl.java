package ResImpl;

import java.util.*;
import java.net.*;
import org.json.simple.JSONObject;
import java.io.*;

//public class MiddlewareImpl
public class MiddlewareImpl {

    
	protected static RMHashtable m_itemHT = new RMHashtable();
    ServerSocket serverSocket = null;
    static boolean runThreads = true;
    static String server = "localhost";

	public MiddlewareImpl() throws SocketException {
	}

	public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            server = args[0];
        } else if (args.length > 1) {
            System.out.println("Usage: java MiddlewareImpl [server]");
            System.exit(1);
        }
        MiddlewareImpl mid = new MiddlewareImpl();
        mid.listen();
	}

    public int listen() throws IOException {
        try {
            System.out.println("Opening listening socket on port "+ 7654 + ":");
            serverSocket = new ServerSocket( 7654 );
            System.out.println(serverSocket.toString());
        } catch (IOException e) {
            System.err.println("Could not use lisening socket on port " + 7654 + "." );
            System.exit(1);
        }
        
        while (runThreads) {
            MiddlewareThread midThread;
            MiddlewareMessageRouter midRouter;
            System.out.println( "Listening for a new client on port "+ 7654 + ":" );
            Socket socket = serverSocket.accept();
            System.out.println("Client accepted!");
            // Start message router for client
            midRouter = new MiddlewareMessageRouter(socket);
            midRouter.start();
            // Start middleware thread for client
            midThread = new MiddlewareThread(socket, midRouter, server);
            midThread.start();
        }
        serverSocket.close();
        return 0;
    }
}
