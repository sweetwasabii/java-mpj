import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MasterServer {
    public MasterServer(int port) throws NotBoundException {
        try {
            Node n = new Node("master", port);
            NodeInterface node = (NodeInterface) UnicastRemoteObject.exportObject(n, 0);

            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("rmi://172.20.10.7:" + port + "/MasterNode", node);

            System.out.println("MasterNode is starting...");
            System.out.println(node.getStatus());
            System.out.println("MasterNode started");
            System.out.println("Waiting for blocks from slave node...");

            while (true) {
                n.processBlocks();
                System.out.println(n.getBlockCount() + " block processed in MasterNode");
                Thread.sleep(60000);
            }
        } catch (MalformedURLException murle) {
            System.out.println();
            System.out.println("MalformedURLException");
            System.out.println(murle);
        } catch (RemoteException re) {
            System.out.println();
            System.out.println("RemoteException");
            System.out.println(re);
        } catch (InterruptedException ie) {
            System.out.println();
            System.out.println("InterruptedException");
            System.out.println(ie);
        }
    }

    public static void main(String[] args) throws NotBoundException {
        int p = 1099;
        if (args.length == 1) {
            p = Integer.parseInt(args[0]);
        }

        MasterServer s = new MasterServer(p);
    }
}