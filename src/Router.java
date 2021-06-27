
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    private DatagramSocket UDPSocket = null;
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

    private void connectToAdjacentRouters(FileWriter writer) throws IOException {   // this method connects routers using UDP ports.

        String msg = "\nConnecting router "+ this.routerId +" to Adjacent routers via UDP.\n";

        for (EdgeInfo router : adjRouters) {
            msg += "router " + this.routerId + " connected to router " + router.getAdjRouter().routerId + " via UDP.\n";
        }
        writer.write(msg);
        writer.flush();
    }

    //-------------------------------------- UDP functions ----------------------------------------------------------------
    private void createUDPSocket(FileWriter writer) throws IOException {   // creating UDP socket for router
        writer.write("\ncreated UDP socket for router " + this.routerId + "\n");
        writer.flush();
        this.UDPSocket = new DatagramSocket(this.UDPPort);
    }

    private String encapsulatePacket(String payload, boolean isControllPkt){

        String[] splitStr = payload.split("--");   // spliting dest and payload of packet.

        if (isControllPkt)
            return "src: router " + this.routerId + "--" + "IP-address: " + this.IPAddress
                    + "--" + "payload: " + splitStr[0] + "--"+ splitStr[1];

        else return "";   //** TODO
    }

    private DatagramPacket createUDPPacket(String payload, int UDPPort) throws IOException{

        byte [] pktBuffer = payload.getBytes();
        return new DatagramPacket(pktBuffer, pktBuffer.length, InetAddress.getLocalHost(), UDPPort);
    }

    private void sendUDPPacket(DatagramPacket packet, FileWriter writer) throws IOException{

        String pktData = new String(packet.getData(), StandardCharsets.UTF_8);

        writer.write("\nnew packet created from router " + this.routerId + " with payload of " + "{ " + pktData
                +" }" +" sent to port " + packet.getPort() + "\n");

        writer.flush();
        UDPSocket.send(packet);
    }

    private void receiveUDPPacket(FileWriter writer) throws IOException{

        byte [] recvBuffer = new byte[400];

        DatagramPacket DpReceive = new DatagramPacket(recvBuffer, recvBuffer.length);
        this.UDPSocket.receive(DpReceive);

        String pktRecv = new String(recvBuffer, StandardCharsets.UTF_8);
        pktRecv = pktRecv.replace("\0", "");

        writer.write("\nnew pkt received for router " + this.routerId + " is " + "{ " + pktRecv + " }" + "\n");
        writer.flush();
    }

    // we must write function that sends special req packets to adj routers.

    private void sendReqToAdjRouters(FileWriter writer) throws IOException{

        for (EdgeInfo node: this.adjRouters) {

            String newReq = encapsulatePacket("Req" + this.routerId +"--" +"dest: " + node.getAdjRouter().IPAddress,  true);

            DatagramPacket packet = createUDPPacket(newReq, node.getAdjRouter().UDPPort);

            sendUDPPacket(packet, writer);

            receiveUDPPacket(writer);  // waiting until other req packets receive from adj routers.

            String newAck = encapsulatePacket("Ack" + this.routerId + "--"+ "dest: " + node.getAdjRouter().IPAddress,  true);

            DatagramPacket ackPacket = createUDPPacket(newAck, node.getAdjRouter().UDPPort);
            sendUDPPacket(ackPacket, writer);

            receiveUDPPacket(writer);   // wating for ack.

        }
    }

    //----------------------------------------------------------------------------------------------------------

    private String showConnectivityTable() {

        String msg = "";
        for (EdgeInfo info : adjRouters) {

            msg += "from router " + this.routerId + " to router " + info.getAdjRouter().routerId + " cost is: " + info.getWeight() + "\n";
        }
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

        return parNode.getWeight() > node.getWeight();
    }

    private void updateForwardingTable(Dijkstra dijkstra){

        for (int destRouter = 0; destRouter < netTopology.numRouters; destRouter++) {

            if (this.adjIDs.contains(destRouter) && hasMinCost(destRouter, dijkstra.parents)){
                this.forwardingTable.put(destRouter, destRouter);
            }

            else {

                int adjID = destRouter;

                adjID = dijkstra.parents[adjID];

                if (this.routerId == destRouter)
                    adjID = -1;

                while (!this.adjIDs.contains(adjID)){

                    if (this.routerId == destRouter)
                        break;

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
            writeToRouterFile(fileWriter,"{ "+ msgFromManager+ " } " + "received from manager.\n");   //recv safe massage for router from manager.

            /* send request to other routers.*/

            createUDPSocket(fileWriter);
            connectToAdjacentRouters(fileWriter);
            System.out.println("router " + this.routerId + " is exchanging control packets with its adjacent routers");
            sendReqToAdjRouters(fileWriter);

            // send ready for routing signal to manager.

            routerMsg = "\n"+"Ack received from all of adjacent of router " + this.routerId + "\n";
            routerMsg += "Sending ready for routing signal to manager...\n";

            writeToRouterFile(fileWriter, routerMsg);

            sendSignal("router " + this.routerId + " is ready for routing.");


            // ---------------------- applaying the dijkstra on router ----------------------------------------

            System.out.println("dijkstra algo is applying on router " + this.routerId);
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