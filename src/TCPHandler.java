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


    TCPHandler(Socket connection) {

        this.ConnectionID = idCounter++;
        this.connection = connection;
        try {
            this.input = new DataInputStream(connection.getInputStream());
            this.out = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    TCPHandler(Socket connection, int routerID) {

        this.ConnectionID = routerID;
        this.connection = connection;
        try {

            this.input = new DataInputStream(connection.getInputStream());
            this.out = new DataOutputStream(connection.getOutputStream());

            //System.out.println("connection socket info from TCP handler " + this.ConnectionID + " side is: "+ connection.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.routerAddress = IPSub + (idCounter++);
        System.out.println("ip for router " + this.ConnectionID + " is: "+ routerAddress);
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
        return (this.routerAddress + "**" + "connectivity Table router " + this.getConnectionID());
    }

    @Override
    public void run() {

        try {

           // Synchronization.mutex.acquire();

//------------ start of TCP connection between one router and manager ----------------------------------------------
            FileWriter fileWriter = new FileWriter(Synchronization.managerOutput, true);
            fileWriter.write("Starting TCP connection with router " + this.ConnectionID + "\n");
            fileWriter.flush();

           //Synchronization.mutex.release();



            System.out.println("TCP connection " + getConnectionID());

            //** polling wait until data come from specified router process.

            Synchronization.pollingWait(input);    // it may cause infinite loop because the specified router closes its connection.

            String msgFromRouter = input.readUTF();
            String [] initpktFromRouter = msgFromRouter.split("--");
            System.out.println("TCP Handler "+ getConnectionID() + ": " +  msgFromRouter);

            // sending address and connectivity table to router.

            out.writeUTF(createInitMsgToRouter() + "\r\n");
            out.flush();

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