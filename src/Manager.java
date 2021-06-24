import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;


public class Manager {

    //int numRouters;
    List<Router> routers;
    List<TCPHandler> handlers;
    int managerId;
    static int idCounter = 0;
    int port;     // this is TCP port.
    String address;

    //public static final String fileAddress = "D:\\CodeProjects\\JavaProjects\\NetworkProject\\src\\managerOutput.txt";

    Manager() {

        //this.numRouters = 0;
        this.routers = new ArrayList<>();
        this.handlers = new Vector<>();

        this.managerId = idCounter++;
        this.port = 8080;
        this.address = "localhost";
    }

    public TopologyInfo configNetwork() throws FileNotFoundException {   // must change to non-static.

        File myObj = new File("C:\\Users\\alireza\\Desktop\\NetworkProject\\src\\config.txt");

        //System.out.println(myObj.exists());
        Scanner myReader = new Scanner(myObj);

        String routerConifg = myReader.nextLine();

        System.out.println(routerConifg.trim().replace(" ", ""));

        String[] routers = routerConifg.split(",");
        String topologyInfo = "";

        int lineCounter = 0;

        while (myReader.hasNextLine()) {   // reading topology 's info from file.

            topologyInfo += myReader.nextLine();
            lineCounter++;

            topologyInfo += ",";
        }

        myReader.close();

        System.out.println("topologyInfo: " + topologyInfo);
        System.out.println("topologyMatrix: ");

        return new TopologyInfo(routers, topologyInfo);

    }

    public void createRouters(TopologyInfo info) throws IOException {

        //File managerOut = new File(fileAddress);
        FileWriter fileWriter = new FileWriter(Synchronization.managerOutput);

        for (int i = 0; i < info.routers.length; i++) {

            routers.add(new Router(this.address, this.port, i));   // giving a default id to every router.

            fileWriter.write("Created router " + i + "\n");
            fileWriter.flush();
        }

        //** TODO must add some code (running threads and etc.)


    }

    public void startRouter(int id) {

        routers.get(id).start();
    }

    public static void main(String[] args) throws IOException {


        Manager manager = new Manager();

        TopologyInfo info = manager.configNetwork();
        manager.createRouters(info);

        // ------------------------- handling TCP connection between manager and routers------------------------
        try (ServerSocket server = new ServerSocket(manager.port)) {

            int i = 0;
            while (i < manager.routers.size()) {

                try {

                    manager.startRouter(i);   // start router process and wait 2 sec for every process.

                    Socket connection = server.accept();
                    Thread TCPConnection = new TCPHandler(connection, manager.routers.get(i).getRouterId());  // pass connection socket between manager and current router to TCP-handler.
                    manager.handlers.add((TCPHandler) TCPConnection);   // saving list of handlers.
                    TCPConnection.start();


                } catch (IOException ex) {

                    ex.printStackTrace();
                }

                i++;
            }

        } catch (IOException ex) {
            System.err.println("Couldn't start server");
        }


        // **** TODO continue of code manager.
    }

}
