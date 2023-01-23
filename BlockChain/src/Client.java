import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String reg_host = "172.20.10.7";
        int port = 1099;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            reg_host = args[0];
            port = Integer.parseInt(args[1]);
        }

        NodeInterface slaveNode;
        NodeInterface masterNode;

        try {
            Registry registry = LocateRegistry.getRegistry(port);

            slaveNode = (NodeInterface) registry.lookup("rmi://" + reg_host + ":" + port + "/SlaveNode");
            masterNode = (NodeInterface) registry.lookup("rmi://" + reg_host + ":" + port + "/MasterNode");

            cli(slaveNode, masterNode);
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public static void cli(NodeInterface slaveNode, NodeInterface masterNode) throws MalformedURLException, NotBoundException {
        while (true) {
            int choice;
            System.out.println("//-------------------------//");
            System.out.println("[1] Get first block");
            System.out.println("[2] Add data to blockchain");
            System.out.println("[3] Get block hash by id");
            System.out.println("[4] Is blockchain valid?");
            System.out.println("[5] Give me the blockchain");
            System.out.println("[6] What is the size of the blockchain?");
            System.out.println("[7] Get block data by id");
            System.out.println("//-------------------------//");
            System.out.println("Choose a number: ");

            Scanner scn = new Scanner(System.in);
            choice = scn.nextInt();
            switch (choice) {
                case 1 -> {
                    String firstBlockData;
                    try {
                        firstBlockData = masterNode.getBlockDataById(0);
                        System.out.println("The first block data: ");
                        System.out.println(firstBlockData);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                case 2 -> {
                    Scanner scn2 = new Scanner(System.in);
                    String data = scn2.next();
                    try {
                        slaveNode.sendTransaction(data);
                        System.out.println("Trying to send transaction processing pool " + data);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                case 3 -> {
                    Scanner scn3 = new Scanner(System.in);
                    int blockId = scn3.nextInt();
                    String blockHash;
                    try {
                        blockHash = masterNode.getBlockHashById(blockId);
                        System.out.println("The block " + blockId + " hash: ");
                        System.out.println(blockHash);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                case 4 -> {
                    boolean valid;
                    try {
                        valid = masterNode.isChainValid();
                        if (valid)
                            System.out.println("Yes");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                case 5 -> {
                    String blockchainJson;
                    try {
                        blockchainJson = StringUtil.getJson(masterNode.getBlockchain());
                        System.out.println("The blockchain");
                        System.out.println(blockchainJson);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                case 6 -> {
                    int size;
                    try {
                        size = masterNode.getBlockchainSize();
                        System.out.println("The blockchain size");
                        System.out.println(size);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                case 7 -> {
                    Scanner scn4 = new Scanner(System.in);
                    int blockId2 = scn4.nextInt();
                    String blockData;
                    try {
                        blockData = masterNode.getBlockDataById(blockId2);
                        System.out.println("The block " + blockId2 + " hash: ");
                        System.out.println(blockData);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}