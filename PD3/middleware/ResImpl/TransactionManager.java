package ResImpl;

import MidInterface.*;
import ResInterface.*;
import java.util.*;
import java.rmi.*;
import java.io.*;

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

    static Registry registryRM = null;

    static ResourceManager rmCar = null;
	static ResourceManager rmFlight = null;
	static ResourceManager rmHotel = null;

	private MiddlewareImpl middleware;
	private LockManager lockManager;

	private Hashtable<Integer, Object> transactionRecords = new Hashtable<Integer, Object>();
	private ArrayList<Integer> activeTransactions = new ArrayList<Integer>();

	private static boolean crashCarAfterPrepare = false;
	private static boolean crashFlightAfterPrepare = false;
	private static boolean crashHotelAfterPrepare = false;

	private static String configFilepath = "configCrashes.txt";
	
	public TransactionManager(String server, int port, MiddlewareImpl middleware, LockManager lockManager) throws RemoteException {
		registryRM = LocateRegistry.getRegistry(server, port);
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

     	TransactionManagerThread TMThread = new TransactionManagerThread(300000, 1000, this);
     	TMThread.start();

     	try {
     		FileInputStream cStream = new FileInputStream(configFilepath);
     		DataInputStream cData = new DataInputStream(cStream);
     		BufferedReader cReader = new BufferedReader(new InputStreamReader(cData));
     		String line;
     		String delims = "[:]";
     		String[] tokens;
     		while ((line = cReader.readLine()) != null) {
     			tokens = line.split(delims);
     			if (tokens[0] == "crashCarAfterPrepare") {
     				if (tokens[1] == "1") {
     					crashCarAfterPrepare = true;
     				}
     			} else if (tokens[0] == "crashFlightAfterPrepare") {
     				if (tokens[1] == "1") {
     					crashFlightAfterPrepare = true;
     				}
     			} else if (tokens[0] == "crashHotelAfterPrepare") {
     				if (tokens[1] == "1") {
     					crashHotelAfterPrepare = true;
     				}
     			}
     		}
     		cData.close();
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
	}

	public void enlist(int rm, int xid) {
		try {
			ArrayList<Object> rmInfo = (ArrayList<Object>) transactionRecords.get(new Integer(xid));
			if (rmInfo == null) {
				System.out.println("Invalid XID: " + xid);
			}
			boolean[] activeRMs = (boolean[])(rmInfo.get(RM_INDEX));
			if (rm == TransactionManager.CAR) {
				if (!activeRMs[TransactionManager.CAR]) {
					activeRMs[TransactionManager.CAR] = true;
					try {
						rmCar.enlist(xid);
					} catch (RemoteException r) {
						System.out.println("Remote Exception. Attempting to reconnect...");
	                    rmCar = (ResourceManager) registryRM.lookup("HAL9001CarResourceManager");
	                    if (rmCar != null) {
	                        System.out.println("Successfully reconnected.");
	                        rmCar.enlist(xid);
	                    }
					}
				}
			} else if (rm == TransactionManager.FLIGHT) {
				if (!activeRMs[TransactionManager.FLIGHT]) {
					activeRMs[TransactionManager.FLIGHT] = true;
					try {
						rmFlight.enlist(xid);
					} catch (RemoteException r) {
						System.out.println("Remote Exception. Attempting to reconnect...");
	                    rmFlight = (ResourceManager) registryRM.lookup("HAL9001FlightResourceManager");
	                    if (rmFlight != null) {
	                        System.out.println("Successfully reconnected.");
	                        rmFlight.enlist(xid);
	                    }
					}
				}
			} else if (rm == TransactionManager.ROOM) {
				if (!activeRMs[TransactionManager.ROOM]) {
					activeRMs[TransactionManager.ROOM] = true;
					try {
						rmHotel.enlist(xid);
					} catch (RemoteException r) {
						System.out.println("Remote Exception. Attempting to reconnect...");
	                    rmHotel = (ResourceManager) registryRM.lookup("HAL9001RoomResourceManager");
	                    if (rmHotel != null) {
	                        System.out.println("Successfully reconnected.");
	                        rmHotel.enlist(xid);
	                    }
					}
				}
			} else if (rm == TransactionManager.CUSTOMER) {
				if (!activeRMs[TransactionManager.CUSTOMER])
					activeRMs[TransactionManager.CUSTOMER] = true;
			}
			rmInfo.set(TransactionManager.RM_INDEX, activeRMs);
			transactionRecords.put(new Integer(xid), rmInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		System.out.println("NEW Transaction: " + xid);
        return xid;
	}

	public boolean commit(int xid) {
		try {
			boolean voteCar = true;
			boolean voteFlight = true;
			boolean voteRoom = true;
			boolean committedCar = true;
			boolean committedFlight = true;
			boolean committedRoom = true;
			boolean committedCustomer = true;

			ArrayList<Object> rmInfo = (ArrayList<Object>) transactionRecords.get(new Integer(xid));
			boolean[] activeRMs = (boolean[])(rmInfo.get(RM_INDEX));

			if (activeRMs[TransactionManager.CAR]) {
				voteCar = false;
				try {
					voteCar = rmCar.prepare(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmCar = (ResourceManager) registryRM.lookup("HAL9001CarResourceManager");
                    if (rmCar != null) {
                        System.out.println("Successfully reconnected.");
                        try {
                        	voteCar = rmCar.prepare(xid);
                        } catch (Exception e) {
                        	voteCar = false;
                        }
                    }
				}
			}
			if (activeRMs[TransactionManager.FLIGHT]) {
				voteFlight = false;
				try {
					voteFlight = rmFlight.prepare(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmFlight = (ResourceManager) registryRM.lookup("HAL9001FlightResourceManager");
                    if (rmFlight != null) {
                        System.out.println("Successfully reconnected.");
                        try {
                        	voteFlight = rmFlight.prepare(xid);
                        } catch (Exception e) {
                        	voteFlight = false;
                        }
                    }
				}
			}
			if (activeRMs[TransactionManager.ROOM]) {
				voteRoom = false;
				try {
					voteRoom = rmHotel.prepare(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmHotel = (ResourceManager) registryRM.lookup("HAL9001RoomResourceManager");
                    if (rmHotel != null) {
                        System.out.println("Successfully reconnected.");
                        try {
                        	voteRoom = rmHotel.prepare(xid);
                        } catch (Exception e) {
                        	voteRoom = false;
                        }
                    }
				}
			}

			if (crashCarAfterPrepare) {
				middleware.crash("car");
			}
			if (crashFlightAfterPrepare) {
				middleware.crash("flight");
			}
			if (crashHotelAfterPrepare) {
				middleware.crash("hotel");
			}

			if (!(voteCar && voteFlight && voteRoom)) {
				System.out.println("Not all transactions voted YES. Aborting.");
				abort(xid);
				return false;
			}

			System.out.println("All Transactions voted YES. Committing.");

			boolean unlockVariables = lockManager.UnlockAll(xid);

			if (activeRMs[TransactionManager.CAR]) {
				committedCar = false;
				try {
					committedCar = rmCar.commit(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmCar = (ResourceManager) registryRM.lookup("HAL9001CarResourceManager");
                    if (rmCar != null) {
                        System.out.println("Successfully reconnected.");
                        committedCar = rmCar.commit(xid);
                    }
				}
			}
			if (activeRMs[TransactionManager.FLIGHT]) {
				committedFlight = false;
				try {
					committedFlight = rmFlight.commit(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmFlight = (ResourceManager) registryRM.lookup("HAL9001FlightResourceManager");
                    if (rmFlight != null) {
                        System.out.println("Successfully reconnected.");
                        committedFlight = rmFlight.commit(xid);
                    }
				}
			}
			if (activeRMs[TransactionManager.ROOM]) {
				committedRoom = false;
				try {
					committedRoom = rmHotel.commit(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmHotel = (ResourceManager) registryRM.lookup("HAL9001RoomResourceManager");
                    if (rmHotel != null) {
                        System.out.println("Successfully reconnected.");
                        committedRoom = rmHotel.commit(xid);
                    }
				}
			}
			if (activeRMs[TransactionManager.CUSTOMER])
				committedCustomer = middleware.commitCustomers(xid);

			transactionRecords.remove(new Integer(xid));
			activeTransactions.remove(new Integer(xid));
			if (unlockVariables && committedCar && committedFlight && committedRoom && committedCustomer) {
				System.out.println("Successfully committed!");
			} else {
				System.out.println("Commit failed.");
			}
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
			if (activeRMs[TransactionManager.CAR]) {
				try {
					rmCar.abort(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmCar = (ResourceManager) registryRM.lookup("HAL9001CarResourceManager");
                    if (rmCar != null) {
                        System.out.println("Successfully reconnected.");
                        try {
                        	rmCar.abort(xid);
                        } catch (Exception e) {
                        	System.out.println("System already down.");
                        }
                    }
				}
			}
			if (activeRMs[TransactionManager.FLIGHT]) {
				try {
					rmFlight.abort(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmFlight = (ResourceManager) registryRM.lookup("HAL9001FlightResourceManager");
                    if (rmFlight != null) {
                        System.out.println("Successfully reconnected.");
                        try {
                        	rmFlight.abort(xid);
                        } catch (Exception e) {
                        	System.out.println("System already down.");
                        }
                    }
				}
			}
				
			if (activeRMs[TransactionManager.ROOM]) {
				try {
					rmHotel.abort(xid);
				} catch (RemoteException r) {
					System.out.println("Remote Exception. Attempting to reconnect...");
                    rmHotel = (ResourceManager) registryRM.lookup("HAL9001RoomResourceManager");
                    if (rmHotel != null) {
                        System.out.println("Successfully reconnected.");
                        try {
                        	rmHotel.abort(xid);
                        } catch (Exception e) {
                        	System.out.println("System already down.");
                        }
                    }
				}
			}
			if (activeRMs[TransactionManager.CUSTOMER])
				middleware.abortCustomers(xid);

			transactionRecords.remove(new Integer(xid));
			activeTransactions.remove(new Integer(xid));
			System.out.println("Successfully aborted!");
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