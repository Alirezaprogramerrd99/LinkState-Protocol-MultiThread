public class TopologyInfo{
    String[] routers;
    String networkInfo;
    int [][] networkTopology;

    TopologyInfo(String[] routers, String networkInfo){

        this.routers = routers;
        this.networkInfo = networkInfo;
        this.networkTopology = new int[routers.length][routers.length];

        for (int i = 0; i < routers.length; i++) {
            for (int j = 0; j < routers.length; j++) {
                networkTopology[i][j] = Integer.MAX_VALUE;
            }
        }

        createTopologyMatrix();
        printTopologyMatrix();
    }

    public String[] getRouters() {
        return routers;
    }

    public String getNetworkInfo() {
        return networkInfo;
    }

    private void createTopologyMatrix(){    // saving the hole topology of the our network.

        String [] splitTopology = this.networkInfo.split(",");

        for (int i = 0; i < splitTopology.length-1; i++) {
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
}
