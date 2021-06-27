import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Manager {

    //int numRouters;
    List<Router> netNodes;
    List<TCPHandler> handlers;
    int managerId;
    static int idCounter = 0;
    int port;     // this is TCP port.
    String address;
    String configFileAddress;
    String routingPathsFileAddress;

    Manager() {

        //this.numRouters = 0;
        this.netNodes = new ArrayList<>();
        this.handlers = new Vector<>();

        this.managerId = idCounter++;
        this.port = 8080;
        this.address = "localhost";
        this.configFileAddress = "C:\\Users\\alireza\\Desktop\\NetworkProject\\src\\config.txt";
        this.routingPathsFileAddress = "C:\\Users\\alireza\\Desktop\\NetworkProject\\src\\routingPaths.txt";
    }

    public TopologyInfo configNetwork() throws FileNotFoundException {   // must change to non-static.

        File myObj = new File(this.configFileAddress);

        //System.out.println(myObj.exists());
        Scanner myReader = new Scanner(myObj);

        String routerConifg = myReader.nextLine();

        System.out.println(routerConifg.trim().replace(" ", ""));

        String[] routers = routerConifg.split(",");
        String topologyInfo = "";

        while (myReader.hasNextLine()) {   // reading topology 's info from file.

            topologyInfo += myReader.nextLine();

            topologyInfo += ",";
        }

        myReader.close();
        System.out.println("Network topologyMatrix: ");

        return new TopologyInfo(routers.length, topologyInfo);

    }

    private void writeNetTopologyInFile(FileWriter fileWriter, TopologyInfo info) throws IOException{

        for (int i = 0; i < info.numRouters; i++) {
            for (int j = 0; j < info.numRouters; j++)
                fileWriter.write(info.networkTopology[i][j] + " ");
            fileWriter.write("\n");
        }
        fileWriter.write("\n");
        fileWriter.flush();
    }

    public void createRouters(TopologyInfo info) throws IOException {

        //File managerOut = new File(fileAddress);
        FileWriter fileWriter = new FileWriter(Synchronization.managerOutput);

        for (int i = 0; i < info.numRouters; i++) {

            netNodes.add(new Router(this.address, this.port, i));   // giving a default id to every router.

            fileWriter.write("Created router " + i + "\n");
            fileWriter.flush();
        }

        fileWriter.write("\nNetwork topology created by manager:\n");
        fileWriter.flush();
        writeNetTopologyInFile(fileWriter, info);

        info.setRouters(netNodes);
        Router.netTopology = info;
    }

    public void readRoutingPaths() throws IOException{

        File paths = new File(this.routingPathsFileAddress);

        Scanner myReader = new Scanner(paths);

        while (myReader.hasNextLine()) {   // reading topology 's info from file.

            String newPath = myReader.nextLine();
            String []splitStr = newPath.split(" ");
            TopologyInfo.testPathQueue.add(new PathInfo(Integer.parseInt(splitStr[0]), Integer.parseInt(splitStr[1])));
        }
    }

    public void startRouter(int id) {

        netNodes.get(id).start();
    }

    public static void main(String[] args) throws IOException {

        Manager manager = new Manager();

        TopologyInfo info = manager.configNetwork();
        manager.createRouters(info);
        manager.readRoutingPaths();

        // ------------------------- handling TCP connection between manager and routers------------------------
        try (ServerSocket server = new ServerSocket(manager.port)) {

            int i = 0;
            while (i < manager.netNodes.size()) {

                try {

                    manager.startRouter(i);   // start router process and wait 2 sec for every process.

                    Socket connection = server.accept();
                    TCPHandler TCPConnection = new TCPHandler(connection, manager.netNodes.get(i).getRouterId(), manager);  // pass connection socket between manager and current router to TCP-handler.
                    manager.handlers.add(TCPConnection);   // saving list of handlers.
                    TCPConnection.start();


                } catch (IOException ex) {

                    ex.printStackTrace();
                }

                i++;
            }

        } catch (IOException ex) {
            System.err.println("Couldn't start server");
        }

//        System.out.println("main manager blocked.");
//        while (!Synchronization.checkHandlerManagerVector());   // blocking mode until manager reads whole file.
////
//        System.out.println("releasing handlers");
//        System.out.println("main manager released!!");
//
//        for (TCPHandler connection: manager.handlers) {
//            connection.syncHandlerManger = 1;
//        }

    }

}
