import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class SlaveServer {
    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        String reg_host = "172.20.10.7";
        int port = 1099;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            reg_host = args[0];
            port = Integer.parseInt(args[1]);
        }

        NodeInterface masterNode;
        NodeInterface slaveNode;

        Registry registry = LocateRegistry.getRegistry(port);
        NodeInterface m = (NodeInterface) registry.lookup("rmi://" + reg_host + ":" + port + "/MasterNode");
        masterNode = (NodeInterface) UnicastRemoteObject.exportObject(m, 0);

        Node s = new Node("slave", port);
        slaveNode = (NodeInterface) UnicastRemoteObject.exportObject(s, 0);
        registry.rebind("rmi://172.20.10.7:" + port + "/SlaveNode", slaveNode);

        System.out.println("SlaveNode is starting...");
        System.out.println(masterNode.getStatus() + " from MasterNode registry");
        System.out.println(slaveNode.getStatus() + " from SlaveNode registry");
        System.out.println(slaveNode.getStatus(masterNode) + " from SlaveNode Registry");
        System.out.println(slaveNode.getStatusFromLookup() + " from SlaveNode Registry without passing the object instance");

        while (true) {
            slaveNode.processTransactions();
            System.out.println(slaveNode.getTxCount() + " transaction processed in SlaveNode");
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ie) {
                System.out.println();
                System.out.println("InterruptedException");
                System.out.println(ie);
            }
        }
    }
}