package ResImpl;

import MidInterface.*;
import ResInterface.*;
import java.util.*;
import java.rmi.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TransactionManager {

	public static final int CAR = 0;
    public static final int FLIGHT = 1;
    public static final int ROOM = 2;
    public static final int CUSTOMER = 3;

    private static final int TIME_INDEX = 0;
    private static final int RM_INDEX = 1;

    static ResourceManager rmCar = null;
	static ResourceManager rmFlight = null;
	static ResourceManager rmHotel = null;

	private MiddlewareImpl middleware;
	private LockManager lockManager;

	private Hashtable<Integer, Object> transactionRecords = new Hashtable<Integer, Object>();
	private ArrayList<Integer> activeTransactions = new ArrayList<Integer>();
	
	public TransactionManager(String server, int port, MiddlewareImpl middleware, LockManager lockManager) throws RemoteException {
		Registry registryRM = LocateRegistry.getRegistry(server, port);
		//locate RMs
		try {
	     	rmCar = (ResourceManager) registryRM.lookup("HAL9001CarResourceManager");
	     	rmFlight = (ResourceManager) registryRM.lookup("HAL9001FlightResourceManager");
	     	rmHotel = (ResourceManager) registryRM.lookup("HAL9001RoomResourceManager");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

     	if (rmCar == null || rmFlight == null || rmHotel == null) {
     		System.out.println("RM lookups unsuccessful");
     	} else {
     		System.out.println("RM lookups successful");
     		System.out.println("Connected to RMs");
     	}
     	this.middleware = middleware;
     	this.lockManager = lockManager;

     	TransactionManagerThread TMThread = new TransactionManagerThread(10000, 1000, this);
     	TMThread.start();
	}

	public void enlist(int rm, int xid) {
		ArrayList<Object> rmInfo = (ArrayList<Object>) transactionRecords.get(new Integer(xid));
		boolean[] activeRMs = (boolean[])(rmInfo.get(RM_INDEX));
		if (!activeRMs[TransactionManager.CAR])
			activeRMs[TransactionManager.CAR] = true;
		if (!activeRMs[TransactionManager.FLIGHT])
			activeRMs[TransactionManager.FLIGHT] = true;
		if (!activeRMs[TransactionManager.ROOM])
			activeRMs[TransactionManager.ROOM] = true;
		if (!activeRMs[TransactionManager.CUSTOMER])
			activeRMs[TransactionManager.CUSTOMER] = true;
		rmInfo.set(TransactionManager.RM_INDEX, activeRMs);
		transactionRecords.put(new Integer(xid), rmInfo);
	}

	public int start() {
		Long currentTime = new Long(System.currentTimeMillis());
		boolean[] activeRMs = new boolean[4];
		for (int i = 0; i < activeRMs.length; i++) {
			activeRMs[i] = false;
		}
		ArrayList<Object> rmInfo = new ArrayList<Object>();
		rmInfo.add(currentTime);
		rmInfo.add(activeRMs);
		
		// Generate a globally unique ID for the new transaction
		int xid = Integer.parseInt( String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                    String.valueOf( Math.round( Math.random() * 100 + 1 )));
		transactionRecords.put(new Integer(xid), rmInfo);
		activeTransactions.add(xid);
        return xid;
	}

	public boolean commit(int xid) {
		try {
			boolean committedCar = true;
			boolean committedFlight = true;
			boolean committedRoom = true;
			boolean committedCustomer = true;

			boolean unlockVariables = lockManager.UnlockAll(xid);

			ArrayList<Object> rmInfo = (ArrayList<Object>) transactionRecords.get(new Integer(xid));
			boolean[] activeRMs = (boolean[])(rmInfo.get(RM_INDEX));
			if (activeRMs[TransactionManager.CAR])
				committedCar = rmCar.commit(xid);
			if (activeRMs[TransactionManager.FLIGHT])
				committedFlight = rmFlight.commit(xid);
			if (activeRMs[TransactionManager.ROOM])
				committedRoom = rmHotel.commit(xid);
			if (activeRMs[TransactionManager.CUSTOMER])
				committedCustomer = middleware.commitCustomers(xid);

			transactionRecords.remove(new Integer(xid));
			activeTransactions.remove(new Integer(xid));

			return unlockVariables && committedCar && committedFlight && committedRoom && committedCustomer;
		} catch (Exception e) {
			System.out.println("TM EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
		}
		return false;
	}

	public void abort(int xid) {
		try {
			ArrayList<Object> rmInfo = (ArrayList<Object>)(transactionRecords.get(new Integer(xid)));
			boolean[] activeRMs = (boolean[]) rmInfo.get(RM_INDEX);
			if (activeRMs[TransactionManager.CAR])
				rmCar.abort(xid);
			if (activeRMs[TransactionManager.FLIGHT])
				rmFlight.abort(xid);
			if (activeRMs[TransactionManager.ROOM])
				rmHotel.abort(xid);
			if (activeRMs[TransactionManager.CUSTOMER])
				middleware.abortCustomers(xid);

			transactionRecords.remove(new Integer(xid));
			activeTransactions.remove(new Integer(xid));
		} catch (Exception e) {
			System.out.println("TM EXCEPTION:");
            System.out.println(e.getMessage());
            e.printStackTrace();
		}
	}

	public void updateTTL(int xid) {
		ArrayList<Object> rmInfo = (ArrayList<Object>)(transactionRecords.get(new Integer(xid)));
		Long currentTime = new Long(System.currentTimeMillis());
		rmInfo.set(TransactionManager.TIME_INDEX, currentTime);
		transactionRecords.put(new Integer(xid), rmInfo);
	}

	public void clearExpiredTransactions(long newTime, int ttl) {
		for (Integer activeTransaction : activeTransactions) {
			ArrayList<Object> rmInfo = (ArrayList<Object>) transactionRecords.get(activeTransaction);
			long rmTime = ((Long)(rmInfo.get(TIME_INDEX))).longValue();
			if ((newTime - rmTime) > ttl) {
				abort(activeTransaction.intValue());
			}
		}
	}
}