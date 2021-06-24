
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Router extends Thread {

    private Socket socket = null;
    private final String address;
    private final int TCPPort;
    private DataInputStream input = null;   // for TCP
    private DataOutputStream out = null;    // for TCP
    private final int routerId;

    //------------------ UDP connection info -----------------------------------------------------
    ArrayList<EdgeInfo> adjRouters;  // from current node (this) to other nodes.
    public static int UDPPortCounter = 4005;   // initial value for all routers constructed from this class.
    private final int UDPPort;
    private final DatagramSocket UDPSocket = null;
    private String IPAddress;
    static TopologyInfo netTopology;

    Router(String address, int TCPPort, int routerId) {

        this.routerId = routerId;
        this.address = address;
        this.TCPPort = TCPPort;
        this.UDPPort = UDPPortCounter++;   // initializing the udp port number for constructed router.
        this.adjRouters = new ArrayList<>();

    }

    private void createConnectivityTable(){

        for (int i = 0; i < netTopology.numRouters; i++) {
            
        }

    }

    private String initMsgToManager(){
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

    public int getRouterId() {
        return routerId;
    }

    @Override
    public void run() {   // each router (node or process) task to done in network.

        Synchronization.addDelaySec(1);

        try {
            this.socket = new Socket(this.address, this.TCPPort);

            System.out.println("router " + this.routerId + " Connected to manager via TCP.");

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


            if (splitMsg[1].contains(("connectivity Table router " + this.routerId))){

                System.out.println("creating or updating table for router " + this.routerId);

                // update adj routers.

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        // close the connection
        while (true) ;    //**** must be fixed wait until data receives from me.
        //closeTCPConnection();  // its to early to close the socket for other threads.

    }
}