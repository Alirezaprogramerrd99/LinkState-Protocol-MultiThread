import java.util.Comparator;
import java.util.Objects;

public class EdgeInfo implements Comparator<EdgeInfo> {   // one edge between one router(node) and one of its adj routers.(represents each entry in connectivity)

    private Router adjRouter;
    private int weight;

    EdgeInfo(Router adjRouter, int weight) {
        this.adjRouter = adjRouter;
        this.weight = weight;
    }

    EdgeInfo(){}

    EdgeInfo(Router router){
        this.adjRouter = router;
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

    @Override
    public int compare(EdgeInfo node1, EdgeInfo node2)
    {
        if (node1.weight < node2.weight)
            return -1;
        if (node1.weight > node2.weight)
            return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return this.adjRouter.getRouterId() == ((Router)o).getRouterId();
    }

}
