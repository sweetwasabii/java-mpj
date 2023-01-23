import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Node implements NodeInterface {
    private ArrayList<Block> blockchain;
    private PriorityQueue<String> memPool;
    private int blockCount = 1;
    private int txCount = 1;
    private PriorityQueue<String> transactionPool;
    private String status = "alive";
    private NodeInterface masterNode;

    protected Node(String type, int port) throws MalformedURLException, RemoteException, NotBoundException {
        if (type.equals("master")) {
            blockchain = new ArrayList<>();
            blockchain.add(new Block("genesis block",""));
            memPool = new PriorityQueue<String>();
            this.status = type.concat(" is ").concat(status);
        } else {
            this.status = type.concat(" is ").concat(status);
            transactionPool = new PriorityQueue<>();
            Registry registry = LocateRegistry.getRegistry(port);
            this.masterNode = (NodeInterface) registry.lookup("rmi://172.20.10.7:" + port + "/MasterNode");
        }
    }

    public void processBlocks() throws RemoteException {
        while (!memPool.isEmpty()) {
            String tx = getBlockFromPool();
            Block newBlock = new Block(tx, getHash());
            if (isChainValid(newBlock)) {
                System.out.println("Trying to mine block " + blockCount);
                blockchain.add(newBlock);
                ++blockCount;
                int difficulty = 5;
                blockchain.get(blockCount - 1).mineBlock(difficulty);
            }
        }
    }

    public String getHash() throws RemoteException  {
        return blockchain.get(blockchain.size() - 1).getHash();
    }

    public String getBlockHashById(int blockId) throws RemoteException  {
        return blockchain.get(blockId).getHash();
    }

    public int getBlockchainSize() throws RemoteException  {
        return blockchain.size();
    }

    public String getBlockDataById(int blockId) throws RemoteException  {
        return blockchain.get(blockId).getData();
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<Block> getBlockchain() throws RemoteException {
        return blockchain;
    }

    public String getBlockFromPool() throws RemoteException {
        return memPool.remove();
    }

    public int getBlockCount() throws RemoteException  {
        return blockCount;
    }

    public int getTxCount() throws RemoteException  {
        return txCount;
    }

    public void addBlockToPool(String tx) throws RemoteException {
        memPool.add(tx);
    }

    public boolean isChainValid() throws RemoteException {
        Block currentBlock;
        Block previousBlock;

        int difficulty = 5;

        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        for (int i = 1; i < blockchain.size(); ++i) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Current hash not equal");
                return false;
            }

            if (!previousBlock.getHash().equals(previousBlock.calculateHash())) {
                System.out.println("Previous hash not equal");
                return false;
            }

            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println(currentBlock.getHash().substring(0, difficulty));
                System.out.println(hashTarget);
                System.out.println("This block hasn't been mined");
                return false;
            }
        }

        return true;
    }

    public Boolean isChainValid(Block newBlock) throws RemoteException {
        Block currentBlock;
        Block previousBlock;

        for (int i = 1; i < blockchain.size(); ++i) {
            currentBlock = newBlock;
            previousBlock = blockchain.get(blockchain.size() - 1);

            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Current Hash not equal");
                return false;
            }

            if (!previousBlock.getHash().equals(previousBlock.calculateHash())) {
                System.out.println("Previous hash not equal");
                return false;
            }
        }

        return true;
    }

    public void processTransactions() throws RemoteException, MalformedURLException, NotBoundException {
        while (!transactionPool.isEmpty() && !getTransactionFromPool().isEmpty()) {
            String t = getTransactionFromPool();
            sendTransaction(t);
        }
    }

    public void sendTransaction(String data) throws RemoteException, MalformedURLException, NotBoundException {
        if ((!data.isEmpty())) {
            broadcastBlock(data, this.masterNode);
            ++txCount;
        }
    }

    private String getTransactionFromPool() {
        return transactionPool.remove();
    }

    public void broadcastBlock(String newData, NodeInterface m) throws RemoteException {
        m.addBlockToPool(newData);
    }

    public String getStatus(NodeInterface m) throws RemoteException {
        return m.getStatus();
    }

    public String getStatusFromLookup() throws RemoteException, MalformedURLException, NotBoundException {
        return this.masterNode.getStatus();
    }
}