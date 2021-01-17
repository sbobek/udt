import java.util.LinkedList;

/**
 * Created by sbk on 14.09.15.
 */
public class TreeNode {
    private String att;
    private LinkedList<TreeEdge> edges;
    private AttStats stats;

    public TreeNode(String attName, AttStats stats){
        att = attName;
        this.stats = stats;
        edges = new LinkedList<TreeEdge>();
    }

    public void addEdge(TreeEdge te){
        edges.add(te);
    }

    public boolean isLeaf(){
        return edges.isEmpty();
    }

    public AttStats getStats() {
        return stats;
    }

    public void setStats(AttStats stats) {
        this.stats = stats;
    }

    public String getAtt() {
        return att;
    }

    public void setAtt(String att) {
        this.att = att;
    }

    public LinkedList<TreeEdge> getEdges() {
        return edges;
    }

    public void setEdges(LinkedList<TreeEdge> edges) {
        this.edges = edges;
    }

    @Override
    public String toString() {
       return getAtt()+" : "+stats;
    }
}
