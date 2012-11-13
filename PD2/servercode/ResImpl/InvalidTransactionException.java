package ResImpl;

/*
    The transaction is invalid.
*/

public class InvalidTransactionException extends Exception
{
    private int xid = 0;
    
    public InvalidTransactionException (int xid)
    {
        super("The transaction " + xid + " is invalid.");
        this.xid = xid;
    }
    
    int GetXId()
    {
        return xid;
    }
}
