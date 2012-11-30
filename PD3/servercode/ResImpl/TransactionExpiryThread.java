package ResImpl;

import ResInterface.*;
import java.rmi.RemoteException;

public class TransactionExpiryThread extends Thread {

    private int ttl;
    private int refresh_time;
    private ResourceManager rm;
    private boolean running = true;

    public TransactionExpiryThread(int ttl, int refresh_time, ResourceManager rm) {
        this.ttl = ttl;
        this.refresh_time = refresh_time;
        this.rm = rm;
    }

    public void run() {
        while(running) {
            long currentTime = System.currentTimeMillis();
            try {
                rm.clearExpiredTransactions(currentTime, ttl);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
				Thread.sleep(refresh_time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
}