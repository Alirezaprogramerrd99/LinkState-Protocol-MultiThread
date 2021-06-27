import java.io.*;
import java.net.Socket;

public class TCPHandler extends Thread {

    private final int ConnectionID;
    private final Socket connection;   // connection socket from each router that accpeted from (manager.)
    private DataInputStream input = null;
    private DataOutputStream out = null;
    static int idCounter = 0;
    static String IPSub = "192.0.0.";
    private String routerAddress;
    Manager manager;
    int syncHandlerManger;
    int managerSleepTime;

    TCPHandler(Socket connection, Manager manager) {

        this.ConnectionID = idCounter++;
        this.connection = connection;
        this.manager = manager;

        try {
            this.input = new DataInputStream(connection.getInputStream());
            this.out = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    TCPHandler(Socket connection, int routerID, Manager manager) {

        this.ConnectionID = routerID;
        this.connection = connection;
        this.manager = manager;

        try {

            this.input = new DataInputStream(connection.getInputStream());
            this.out = new DataOutputStream(connection.getOutputStream());

            //System.out.println("connection socket info from TCP handler " + this.ConnectionID + " side is: "+ connection.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.routerAddress = IPSub + (idCounter++);
        //System.out.println("ip for router " + this.ConnectionID + " is: "+ routerAddress);
        this.syncHandlerManger = 0;
    }

    public int getConnectionID() {
        return ConnectionID;
    }

    public Socket getConnection() {
        return connection;
    }

    public DataInputStream getInput() {
        return input;
    }

    public DataOutputStream getOut() {
        return out;
    }

    private String createInitMsgToRouter(){
        return (this.routerAddress + "--" + "connectivity Table router " + this.getConnectionID());
    }

    private void sendSignal(String signalMsg) throws IOException{
        out.writeUTF(signalMsg);
    }

    @Override
    public void run() {

        try {

           // Synchronization.mutex.acquire();

//------------ start of TCP connection between one router and manager ----------------------------------------------
            FileWriter fileWriter = new FileWriter(Synchronization.managerOutput, true);
            String handlerMsg = "Starting TCP connection with router ";
            fileWriter.write(handlerMsg + this.ConnectionID + "\n");
            fileWriter.flush();

           //Synchronization.mutex.release();



            System.out.println("TCP connection " + getConnectionID());

            //** polling wait until data come from specified router process.

            Synchronization.pollingWait(input);    // it may cause infinite loop because the specified router closes its connection.

            String msgFromRouter = input.readUTF();
            fileWriter.write("TCP request packet(UDP-port -- request#) from router " + this.ConnectionID + ": " + "{ " + msgFromRouter + " }" +"\n");
            fileWriter.flush();

            String [] initpktFromRouter = msgFromRouter.split("--");
            System.out.println("TCP Handler "+ getConnectionID() + ": " +  msgFromRouter);

            // sending address and connectivity table to router.

            out.writeUTF(createInitMsgToRouter() + "\r\n");
            out.flush();
            handlerMsg = "connectivity table sent to router " + this.ConnectionID;

            fileWriter.write(handlerMsg + "\n");
            fileWriter.flush();
            //-----------------------------------------------------------------------------------

            Synchronization.pollingWait(input);    // wait until you receive ready signal from router.

            String recvSignal = input.readUTF();
            handlerMsg = "{ "+ recvSignal + " }" + " signal received from router " + this.ConnectionID + "\n";

            if (recvSignal.equals("ready"+this.ConnectionID)){

                Synchronization.syncronizationVector[this.ConnectionID] = true;
            }

            fileWriter.write( handlerMsg + "\n");
            fileWriter.flush();

            while (!Synchronization.checkSyncronizationVector());  // wait until all routers to be ready.


            sendSignal("Safe " + this.ConnectionID);
            Synchronization.addDelaySec(1);

            handlerMsg = "\nSafe massage for router " + this.ConnectionID + " sent.\n";
            fileWriter.write(handlerMsg);
            fileWriter.flush();

            handlerMsg = "\nrouter " + this.ConnectionID + " released from waiting mode.\n";
            fileWriter.write(handlerMsg);
            fileWriter.flush();

            // reset syncronization-Vector..
            Synchronization.syncronizationVector[this.ConnectionID] = false;


            Synchronization.pollingWait(input);   // wait until router says that all if its routres sent ACK...

            msgFromRouter = input.readUTF();
            fileWriter.write("\nreceived from router "+ +this.ConnectionID + ": " + "{ "  + msgFromRouter + " }" + " \n");
            fileWriter.flush();

            if (msgFromRouter.equals("router " + this.ConnectionID + " is ready for routing.")){
                Synchronization.syncronizationVector[this.ConnectionID] = true;
            }

            while (!Synchronization.checkSyncronizationVector());

            sendSignal("Network is ready to route.");
            Synchronization.addDelaySec(1);

            fileWriter.write("\nNetwork ready to use routing signal sent to routers.\n");
            fileWriter.flush();

            Synchronization.syncronizationVector[this.ConnectionID] = false;

          //  this.managerSleepTime = TopologyInfo.testPathQueue.size();

            Synchronization.mutex.acquire();

            while (!TopologyInfo.testPathQueue.isEmpty()) {    // one random thread will execute this block of code.

                PathInfo newPath = TopologyInfo.testPathQueue.remove();

                //System.out.println("in thread " + this.ConnectionID + " newPath src is: " + newPath.getSrc() + " queue size: " + TopologyInfo.testPathQueue.size());
                manager.netNodes.get(newPath.getSrc()).routerTestPaths.add(newPath);
                Synchronization.addDelaySec(1);
            }

            Synchronization.mutex.release();

            fileWriter.write("\nTest routes sent to router " + this.ConnectionID + "\n");
            fileWriter.flush();

            sendSignal("Command Route" + this.ConnectionID);
            Synchronization.addDelaySec(1);

            //Synchronization.handlerManagerVector[this.ConnectionID] = true;

//            fileWriter.write("TCP handler " + this.ConnectionID + " released by manager.");
//            fileWriter.flush();

            while (true);

        } catch (Exception ex) {
            System.err.println(ex);


        } //finally {
            try {
                connection.close();
            } catch (IOException e) {
            }
        //}
    }

}