import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {    // we will run this for each router.

    PriorityQueue<EdgeInfo> pq;
    HashSet<Integer> visited;
    int []dist;
    Router srcRouter;
    int routersInNetwork;
    List<EdgeInfo> routerAdjList;

    Dijkstra(Router srcRouter, List<EdgeInfo> routerAdjList){

        this.pq = new PriorityQueue<>();
        this.visited = new HashSet<>();
        this.routerAdjList = routerAdjList;    // recv from each router.
        this.srcRouter = srcRouter;
        this.routersInNetwork = Router.UDPPortCounter - 4005;
        dist = new int[routersInNetwork];
    }

    public void algoDijkstra()
    {

        for (int i = 0; i < routersInNetwork; i++)
            dist[i] = Integer.MAX_VALUE;

        // first add source vertex to PriorityQueue
        pq.add(new EdgeInfo(srcRouter, 0));

        // Distance to the source from itself is 0
        dist[srcRouter.getRouterId()] = 0;

        while (visited.size() != routersInNetwork) {

            //when the priority queue is empty, return

            if (pq.isEmpty())
                return;

            // u is removed from PriorityQueue and has min distance
            int u = pq.remove().getAdjRouter().getRouterId();

            // add node to finalized list (visited)
            visited.add(u);
            graph_adjacentNodes(u);
        }
    }

    private void graph_adjacentNodes(int u)   {
        int edgeDistance;
        int newDistance;

        // process all neighbouring nodes of u
        for (int i = 0; i < routerAdjList.size(); i++) {
            EdgeInfo v = routerAdjList.get(i);

            //  proceed only if current node is not in 'visited'
            if (!visited.contains(v.getAdjRouter().getRouterId())) {
                edgeDistance = v.getWeight();
                newDistance = dist[u] + edgeDistance;

                // compare distances
                if (newDistance < dist[v.getAdjRouter().getRouterId()])
                    dist[v.getAdjRouter().getRouterId()] = newDistance;

                // Add the current vertex to the PriorityQueue
                pq.add(new EdgeInfo(v.getAdjRouter(), dist[v.getAdjRouter().getRouterId()]));
            }
        }
    }

}
