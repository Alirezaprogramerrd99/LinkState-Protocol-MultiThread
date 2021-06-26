
import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Router extends Thread {

    private Socket socket = null;
    private final String address;
    private final int TCPPort;
    private DataInputStream input = null;   // for TCP
    private DataOutputStream out = null;    // for TCP
    private final int routerId;
    private String routerFileName;
    private final String dirAddress;

    //------------------ UDP connection info -----------------------------------------------------
    ArrayList<EdgeInfo> adjRouters;  // from current node (this) to other nodes.
    ArrayList<Integer> adjIDs;
    public static int UDPPortCounter = 4005;   // initial value for all routers constructed from this class.
    private final int UDPPort;
    private final DatagramSocket UDPSocket = null;
    private String IPAddress;
    static TopologyInfo netTopology;
    private Map <Integer, Integer> forwardingTable;

    Router(String address, int TCPPort, int routerId) {

        this.routerId = routerId;
        this.address = address;
        this.TCPPort = TCPPort;
        this.UDPPort = UDPPortCounter++;   // initializing the udp port number for constructed router.
        this.adjRouters = new ArrayList<>();
        this.adjIDs = new ArrayList<>();
        this.forwardingTable = new HashMap<>();

        //*** -------------- router output files configurations ------------------------
        this.routerFileName = "router_" + routerId + "_Output.txt";
        this.dirAddress = "C:\\Users\\alireza\\Desktop\\NetworkProject\\src\\RoutersOutputFiles\\";

    }

    private void createConnectivityTable() {   //*****

        for (int i = 0; i < netTopology.numRouters; i++) {

            if (netTopology.networkTopology[this.routerId][i] > 0) {

                adjRouters.add(new EdgeInfo(netTopology.getRouters().get(i), netTopology.networkTopology[this.routerId][i]));
                adjIDs.add(netTopology.getRouters().get(i).getRouterId());
            }
        }
    }

    private String showConnectivityTable() {

        String msg = "";
        for (EdgeInfo info : adjRouters) {

            msg += "from router " + this.routerId + " to router " + info.getAdjRouter().routerId + " cost is: " + info.getWeight() + "\n";
            //System.out.println(msg);
        }
        //System.out.println();

        return msg;
    }

    private String initMsgToManager() {
        return (this.UDPPort + "--" + "request " + routerId);
    }

    public int getUDPPort() {
        return UDPPort;
    }

    private void closeTCPConnection() {

        try {
            System.out.println("TCP connection for router " + this.routerId + " closed.");
            input.close();
            out.close();
            socket.close();

        } catch (IOException i) {
            System.out.println(i);
        }
    }

    private void deleteFilesFromDir(File dirObj){

        for (File file: dirObj.listFiles()) {
            file.delete();
        }
    }

    private void sendSignal(String signalMsg) throws IOException{
        out.writeUTF(signalMsg);
    }

    private void writeToRouterFile(FileWriter writer, String msg) throws IOException{

        writer.write(msg);
        writer.flush();
    }

    private boolean hasMinCost(int destRouter, int [] parents){

        int indexInAdjlist = adjIDs.indexOf(destRouter);

        if (parents[destRouter] == this.routerId)
            return true;

        int parIndexInAdjList = adjIDs.indexOf(parents[destRouter]);

        if (indexInAdjlist == -1)
            return false;

        EdgeInfo node = adjRouters.get(indexInAdjlist);
        EdgeInfo parNode = adjRouters.get(parIndexInAdjList);

//        if (routerId == 0){
//            System.out.println("destRouter: " + destRouter);
//            System.out.println("parents[destRouter]: " + parents[destRouter]);
//            System.out.println("node weight: " + node.getWeight());
//        }

        if (parNode.getWeight() > node.getWeight())
            return true;
        else
            return false;
    }

    private void updateForwardingTable(Dijkstra dijkstra){

        for (int destRouter = 0; destRouter < netTopology.numRouters; destRouter++) {

            if (routerId == 0)
                System.out.println("in if destRouter is: " + destRouter + "   " + this.adjIDs.contains(destRouter));

            if (this.adjIDs.contains(destRouter) && hasMinCost(destRouter, dijkstra.parents)){
;
                this.forwardingTable.put(destRouter, destRouter);
            }

            else {

                int adjID = destRouter;

                if (routerId == 0){
                    System.out.println("before: " + adjID);
                    //System.out.println("here!!");
                }

                adjID = dijkstra.parents[adjID];

                if (routerId == 0){
                    System.out.println("after: " + adjID);
                    //System.out.println("here!!");
                }

                while (!this.adjIDs.contains(adjID)){

                    if (this.routerId == destRouter) {

                        adjID = -1;   // because is the same.
                        break;
                    }
                    adjID = dijkstra.parents[adjID];

                }
                this.forwardingTable.put(destRouter, adjID);   // dest, link from this router.
            }
        }
    }

    private void showFrowardingTable(FileWriter writer) throws IOException{

        String outMsg = "\nforwarding table for router " + this.routerId + ":";
        //System.out.println(outMsg);
        outMsg += "\n----dist--------link-----\n";

        for (int dest = 0; dest < this.forwardingTable.size(); dest++) {

            int link = forwardingTable.get(dest);
            outMsg += "\t " + dest + "     |     " + "(" + this.routerId + ", " + link + ")" + "\n";
        }
        writer.write(outMsg);
        writer.flush();
    }


    public int getRouterId() {
        return routerId;
    }

    @Override
    public void run() {   // each router (node or process) task to done in network.

        Synchronization.addDelaySec(1);
        File newRouterFile = new File(this.dirAddress + this.routerFileName);

        File dir = new File(this.dirAddress);
        deleteFilesFromDir(dir);

        String routerMsg = "";
        FileWriter fileWriter = null;

        try {

            this.socket = new Socket(this.address, this.TCPPort);   // connect to manager using TCP.

            if (newRouterFile.createNewFile() || newRouterFile.exists()) {

                System.out.println("router " + this.routerId + " output file created successfully.");
                fileWriter = new FileWriter(newRouterFile, false);
                fileWriter.write("Router " + this.routerId + " started to working." + "\n");
                fileWriter.flush();

            } else
                System.out.println("error to create router" + this.routerId + " output file.");


            routerMsg = "Router " + this.routerId + " Connected to manager via TCP.";
            System.out.println(routerMsg);

            fileWriter.write(routerMsg + "\n\n");
            fileWriter.flush();
            // takes input from terminal
            this.input = new DataInputStream(socket.getInputStream());

            //System.out.println("ROUTER " + this.routerId + " socket info is: " + socket.toString());

            // sends output to the socket
            this.out = new DataOutputStream(socket.getOutputStream());

        } catch (UnknownHostException u) {
            u.printStackTrace();

        } catch (IOException i) {
            i.printStackTrace();
        }

        // string to read message from input
        //String massageToManager = "Router " + this.routerId + " connected.";

        try {    // testing connections.

            this.out.writeUTF(initMsgToManager());   // sending request to manager (TCP handler)
            this.out.flush();

            System.out.println("router " + this.routerId + " msg send to manager.");

            //** polling wait until sender(manager) full this buffer.
            Synchronization.pollingWait(input);

            //------- recv the ip address and connectivity table permission from manager(handler)
            String msgFromManager = input.readUTF();

            String[] splitMsg = msgFromManager.split("--");
            System.out.println("from Handler " + routerId + ": " + msgFromManager);
            this.IPAddress = splitMsg[0];   // setting ip address of router.
            routerMsg = "router " + this.routerId + " ip address from manager: " + this.IPAddress;

            writeToRouterFile(fileWriter, routerMsg + "\n\n");

            if (splitMsg[1].contains(("connectivity Table router " + this.routerId))) {

                System.out.println("creating or updating table for router " + this.routerId);
                // update adj routers.
                createConnectivityTable();

            }

            routerMsg = "Connectivity table from manager for router " + routerId + ":\n";
            routerMsg += showConnectivityTable();   // print connectivity table for each router according to the manager.

            writeToRouterFile(fileWriter,routerMsg + "\n");

            String readySignal = "ready" + routerId;
            sendSignal(readySignal);

            routerMsg = "ready signal for router " + routerId + " sent to manager.\n";

            writeToRouterFile(fileWriter,routerMsg + "\n");

            Synchronization.pollingWait(input);       // wait until manager orders to continue.
            //-------------------------------------------------------------------------------------------
            msgFromManager = input.readUTF();       // ok from manager
            writeToRouterFile(fileWriter,"{ "+ msgFromManager+ " } " + "received from manager.\n");

            // ---------------------- applaying the dijkstra on router ----------------------------------------

            System.out.println("dijkstra algo is applaying on router " + this.routerId);
            Dijkstra dijkstra = new Dijkstra(this, this.adjRouters);
            dijkstra.algoDijkstra();    //** apply dijkstra algo to router.

            routerMsg = "\n\t\t<< Dijkstra algo is applying on router " + this.routerId + " >>\n";
            routerMsg += "\nSPT for router " + routerId + ":\n";

            for (int i = 0; i < dijkstra.dist.length; i++) {
                routerMsg += "src " + routerId + " to router " + i + " cost is: " + dijkstra.dist[i] + "\n";
            }
            writeToRouterFile(fileWriter, routerMsg + "\n");
            routerMsg = "predecessor paths in router " + this.routerId + ": \n";

            for (int i = 0; i < dijkstra.parents.length; i++)
                routerMsg += "parent of router " + i +  " is " + dijkstra.parents[i] + "\n";

            writeToRouterFile(fileWriter, routerMsg);


            // --------updating forwarding table..

            updateForwardingTable(dijkstra);
            showFrowardingTable(fileWriter);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // close the connection
        while (true) ;    //**** must be fixed wait until data receives from me.
        //closeTCPConnection();  // its to early to close the socket for other threads.

    }
}