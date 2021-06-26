import java.util.List;

public class TopologyInfo{

    private List<Router> routers;
    final int numRouters;
    String networkInfo;
    int [][] networkTopology;

    TopologyInfo(int numRouters, String networkInfo){

        this.numRouters = numRouters;
        this.networkInfo = networkInfo;
        this.networkTopology = new int[this.numRouters][this.numRouters];

        for (int i = 0; i < this.numRouters; i++) {
            for (int j = 0; j < this.numRouters; j++) {
                this.networkTopology[i][j] = 0;
            }
        }

        createTopologyMatrix();
        printTopologyMatrix();

        Synchronization.syncronizationVector = new boolean[this.numRouters];

        System.out.println();
    }


    public String getNetworkInfo() {
        return networkInfo;
    }

    private void createTopologyMatrix(){    // saving the hole topology of the our network.

        String [] splitTopology = this.networkInfo.split(",");

        for (int i = 0; i < splitTopology.length; i++) {

            int fromIndex = splitTopology[i].charAt(0) - 48;
            int toIndex = splitTopology[i].charAt(2) - 48;
            int weight = splitTopology[i].charAt(4) - 48;

            this.networkTopology[fromIndex][toIndex] = weight;
            this.networkTopology[toIndex][fromIndex] = weight;
        }
    }

    public int[][] getNetworkTopology() {
        return networkTopology;
    }

    public void printTopologyMatrix(){

        for (int i = 0; i < this.networkTopology.length; i++) {

            for (int j = 0; j < this.networkTopology.length; j++) {
                System.out.print(this.networkTopology[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void setRouters(List<Router> routers) {
        this.routers = routers;
    }

    public List<Router> getRouters() {
        return routers;
    }
}
