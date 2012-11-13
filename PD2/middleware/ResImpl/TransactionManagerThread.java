package ResImpl;

public class TransactionManagerThread extends Thread {

    private int ttl;
    private int refresh_time;
    private TransactionManager tm;
    private boolean running = true;

    public TransactionManagerThread(int ttl, int refresh_time, TransactionManager tm) {
        this.ttl = ttl;
        this.refresh_time = refresh_time;
        this.tm = tm;
    }

    public void run() {
        while(running) {
            long currentTime = System.currentTimeMillis();
            tm.clearExpiredTransactions(currentTime, ttl);
            try {
				Thread.sleep(refresh_time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
}