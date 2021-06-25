import java.net.Socket;
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
//    List<EdgeInfo> routerAdjList;

    Dijkstra(Router srcRouter, List<EdgeInfo> routerAdjList){


        this.visited = new HashSet<>();
       // this.routerAdjList = routerAdjList;    // recv from each router.
        this.srcRouter = srcRouter;
        this.routersInNetwork = Router.UDPPortCounter - 4005;
        dist = new int[routersInNetwork];
        this.pq = new PriorityQueue<EdgeInfo>(routersInNetwork, new EdgeInfo());
    }

    public void algoDijkstra()
    {
        int c = 0;

        for (int i = 0; i < routersInNetwork; i++)
            dist[i] = Integer.MAX_VALUE;

        // first add source vertex to PriorityQueue
        pq.add(new EdgeInfo(srcRouter, 0));

        // Distance to the source from itself is 0
        dist[srcRouter.getRouterId()] = 0;

        while (visited.size() != routersInNetwork) {

            //when the priority queue is empty, return

            if (pq.isEmpty()) {
                //System.out.println("counter: " + c);
                return;
            }

//            if (srcRouter.getRouterId() == 0) {
//
//                System.out.println("routers in queue:");
//                for (EdgeInfo r : pq) {
//                    System.out.println(r.getAdjRouter().getRouterId());
//                }
//            }

            // u is removed from PriorityQueue and has min distance
            Router u = pq.remove().getAdjRouter();

            c++;
            // add node to finalized list (visited)
            visited.add(u.getRouterId());
            graph_adjacentNodes(u);
        }
       // System.out.println("counter: " + c);

    }

    private void graph_adjacentNodes(Router u)   {
        int edgeDistance = -1;
        int newDistance = -1;

//        System.out.println("u: " + u.getRouterId() + " u adjList size  " + u.adjRouters.size());

        // process all neighbouring nodes of u
        for (int i = 0; i < u.adjRouters.size(); i++) {
            EdgeInfo v = u.adjRouters.get(i);

            //  proceed only if current node is not in 'visited'
            if (!visited.contains(v.getAdjRouter().getRouterId())) {

                edgeDistance = v.getWeight();
                newDistance = dist[u.getRouterId()] + edgeDistance;

//                if (srcRouter.getRouterId() == 0){
//
//                    System.out.println("dist[v.getAdjRouter().getRouterId()] = " + dist[v.getAdjRouter().getRouterId()]);
//                    System.out.println("dist[u.getRouterId()] = " + dist[u.getRouterId()]);
//                    System.out.println("router id: " + v.getAdjRouter().getRouterId());
//                    System.out.println("newDistance is " + newDistance);
//                    System.out.println("edgeDistance is " + edgeDistance);
//                }

                // compare distances
                if (newDistance < dist[v.getAdjRouter().getRouterId()]) {

                    dist[v.getAdjRouter().getRouterId()] = newDistance;

                }

                // Add the current vertex to the PriorityQueue
                pq.add(new EdgeInfo(v.getAdjRouter(), dist[v.getAdjRouter().getRouterId()]));
            }
        }
    }

}
