public class TopologyInfo{
    String[] routers;
    String networkInfo;

    TopologyInfo(String[] routers, String networkInfo){
        this.routers = routers;
        this.networkInfo = networkInfo;
    }

    public String[] getRouters() {
        return routers;
    }

    public String getNetworkInfo() {
        return networkInfo;
    }
}
