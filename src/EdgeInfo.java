public class EdgeInfo {   // one edge between one router(node) and one of its adj routers.(represents each entry in connectivity)

    private Router adjRouter;
    private int weight;

    EdgeInfo(Router adjRouter, int weight) {
        this.adjRouter = adjRouter;
        this.weight = weight;
    }

    public Router getAdjRouter() {
        return adjRouter;
    }

    public void setAdjRouter(Router adjRouter) {
        this.adjRouter = adjRouter;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}