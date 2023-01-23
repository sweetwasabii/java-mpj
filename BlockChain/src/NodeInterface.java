import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface NodeInterface extends Remote {
    void addBlockToPool(String string) throws RemoteException;
    void broadcastBlock(String newData, NodeInterface masterNode) throws RemoteException;
    void processBlocks() throws RemoteException;
    void processTransactions() throws RemoteException, MalformedURLException, NotBoundException;
    boolean isChainValid() throws RemoteException;
    int getBlockCount() throws RemoteException;
    int getTxCount() throws RemoteException;
    String getBlockFromPool() throws RemoteException;
    ArrayList<Block> getBlockchain() throws RemoteException;
    String getHash() throws RemoteException;
    String getBlockHashById(int blockId) throws RemoteException;
    String getBlockDataById(int blockId) throws RemoteException;
    void sendTransaction(String data) throws RemoteException, MalformedURLException, NotBoundException;
    String getStatus() throws RemoteException;
    String getStatus(NodeInterface masterNode) throws RemoteException;
    String getStatusFromLookup() throws RemoteException, MalformedURLException, NotBoundException;
    int getBlockchainSize() throws RemoteException;
}