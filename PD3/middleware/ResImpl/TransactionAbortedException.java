package ResImpl;

/*
    The transaction is aborted.
*/

public class TransactionAbortedException extends Exception
{
    private int xid = 0;
    
    public TransactionAbortedException (int xid)
    {
        super("The transaction " + xid + " has already been aborted.");
        this.xid = xid;
    }
    
    int GetXId()
    {
        return xid;
    }
}
