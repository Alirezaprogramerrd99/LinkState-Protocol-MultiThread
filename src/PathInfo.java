public class PathInfo implements Comparable<PathInfo>{

    private final int src;
    private final int dest;

    PathInfo(int src, int dest){

        this.src = src;
        this.dest = dest;
    }

    public int getSrc() {
        return src;
    }

    public int getDest() {
        return dest;
    }

    @Override
    public String toString() {
        return "PathInfo{" +
                "src=" + src +
                ", dest=" + dest +
                '}';
    }

    @Override
    public int compareTo(PathInfo o) {
        return 0;
    }
}
